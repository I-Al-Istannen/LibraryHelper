package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.query.MultipleBookQuery;
import me.ialistannen.libraryhelper.logic.query.Query.QueryCallback;
import me.ialistannen.libraryhelper.logic.query.Query.SearchType;
import me.ialistannen.libraryhelper.logic.query.QueryTarget;
import me.ialistannen.libraryhelper.logic.server.ServerResponseErrorType;
import me.ialistannen.libraryhelper.util.EnumUtil;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.view.booklist.DisplayBookListFragment;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

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
    Function<SearchType, String> transformation = SearchType
        .transformToDisplayName(getFragmentHolderActivity());

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
      performQuery(searchType, queryInput.getText().toString(), getDefaultCallback());
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
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
    performQuery(SearchType.ISBN, isbnString, getDefaultCallback());
  }

  private QueryCallback<List<LoanableBook>> getDefaultCallback() {
    return new QueryCallback<List<LoanableBook>>() {
      @Override
      public void onError(IOException exception, String error, ServerResponseErrorType type) {
        if (!isAdded()) {
          return;
        }

        final String message;
        if (type == ServerResponseErrorType.IO) {
          message = exception.getLocalizedMessage();
        } else {
          message = error;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            executeButton.setEnabled(true);

            new Builder(getFragmentHolderActivity())
                .setTitle(getString(R.string.query_fragment_error_querying_server_title))
                .setMessage(message)
                .create()
                .show();
          }
        });
      }

      @Override
      public void onSuccess(final List<LoanableBook> books) {
        if (!isAdded()) {
          return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            executeButton.setEnabled(true);

            DisplayBookListFragment bookListFragment = new DisplayBookListFragment();
            bookListFragment.setBooks(books);
            getFragmentHolderActivity().switchToFragmentPushBack(bookListFragment);
          }
        });

      }
    };
  }

  private void performQuery(SearchType searchType, String argument,
      QueryCallback<List<LoanableBook>> callback) {
    QueryTarget queryTarget = HttpUtil.getTargetFromSettings(getFragmentHolderActivity());

    new MultipleBookQuery(searchType, argument)
        .executeQuery(queryTarget, HttpUtil.getClient(), callback);
    executeButton.setEnabled(false);
  }
}
