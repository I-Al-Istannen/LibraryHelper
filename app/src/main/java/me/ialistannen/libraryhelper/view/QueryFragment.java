package me.ialistannen.libraryhelper.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.base.Function;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.List;
import java.util.Map;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.query.BookExtractorServerCallback;
import me.ialistannen.libraryhelper.logic.query.QueryTarget;
import me.ialistannen.libraryhelper.logic.query.SearchType;
import me.ialistannen.libraryhelper.util.EnumUtil;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.view.booklist.DisplayBookListFragment;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * A {@link android.app.Fragment} that queries the server.
 */
public class QueryFragment extends FragmentBase {

  @BindView(R.id.query_type)
  Spinner queryType;

  @BindView(R.id.query_input)
  EditText queryInput;

  @BindView(R.id.execute_query_button)
  Button executeButton;

  private Map<String, SearchType> searchTypeMapping;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_query, container, false);

    ButterKnife.bind(this, view);

    setHasOptionsMenu(true);

    setupSpinner();

    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    getFragmentHolderActivity().setActionbarUpPopsFragment(true);
  }

  private void setupSpinner() {
    Function<SearchType, String> transformation = SearchType.transformToDisplayName(
        getFragmentHolderActivity()
    );

    searchTypeMapping = EnumUtil.getReverseMapping(SearchType.class, transformation);

    List<String> items = EnumUtil.transformEnum(SearchType.class, transformation);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        getFragmentHolderActivity(),
        android.R.layout.simple_spinner_item,
        items
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    queryType.setAdapter(adapter);
  }


  @OnClick(R.id.execute_query_button)
  void onExecuteQuery() {
    String selectedItem = (String) queryType.getSelectedItem();
    if (selectedItem == null) {
      return;
    }
    SearchType searchType = searchTypeMapping.get(selectedItem);

    if (searchType != null) {
      performQuery(searchType, queryInput.getText().toString());
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // Is used here to provide the scan icon
    inflater.inflate(R.menu.fragment_isbn_input_action_bar, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.fragment_isbn_input_action_bar_scan) {
      IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(this);
      intentIntegrator.initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

    if (scanResult == null) {
      return;
    }

    String isbnString = scanResult.getContents();
    if (isbnString == null || isbnString.isEmpty()) {
      return;
    }

    queryType.setSelection(SearchType.ISBN.ordinal());
    queryInput.setText(isbnString);
    performQuery(SearchType.ISBN, isbnString);
  }

  private void performQuery(SearchType searchType, String argument) {
    showWaitingSpinner(true);

    QueryTarget queryTarget = HttpUtil.getTargetFromSettings(getFragmentHolderActivity());

    Request request = getRequestForQuery(queryTarget, searchType, argument);

    HttpUtil.makeCall(request, getActivity(),
        new BookExtractorServerCallback(
            this, R.string.query_fragment_error_querying_server_title
        ) {
          @Override
          protected void onReceiveBooks(final List<LoanableBook> books) {
            doSyncIfAdded(new Runnable() {
              @Override
              public void run() {
                executeButton.setEnabled(true);

                DisplayBookListFragment bookListFragment = new DisplayBookListFragment();
                bookListFragment.setBooks(books);
                getFragmentHolderActivity().switchToFragmentPushBack(bookListFragment);
              }
            });
          }

          @Override
          protected void onPostExecute() {
            showWaitingSpinner(false);
            doSyncIfAdded(new Runnable() {
              @Override
              public void run() {
                executeButton.setEnabled(true);
              }
            });
          }
        }
    );

    executeButton.setEnabled(false);
  }

  /**
   * @param target The target of the query
   * @param searchType The search type
   * @param data The data to send
   * @return The {@link HttpUrl} for it.
   */
  private Request getRequestForQuery(QueryTarget target, SearchType searchType, String data) {
    HttpUrl url = target.getUrl().newBuilder()
        .setQueryParameter("search_type", searchType.getValue())
        .setQueryParameter("query", data)
        .build();

    return new Request.Builder()
        .url(url)
        .get()
        .build();
  }
}
