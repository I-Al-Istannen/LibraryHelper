package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
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
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.query.MultipleBookQuery;
import me.ialistannen.libraryhelper.logic.query.Query.SearchType;
import me.ialistannen.libraryhelper.logic.query.QueryTarget;
import me.ialistannen.libraryhelper.util.EnumUtil;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A {@link android.app.Fragment} that queries the server.
 */
public class QueryFragment extends FragmentBase {

  @BindView(R.id.query_type)
  Spinner queryType;

  @BindView(R.id.query_input)
  EditText queryInput;

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

  private void setupSpinner() {
    Function<SearchType, String> transformation = SearchType
        .transformToDisplayName(getAppCompatActivity());

    searchTypeMapping = EnumUtil.getReverseMapping(SearchType.class, transformation);

    List<String> items = EnumUtil.transformEnum(SearchType.class, transformation);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        getAppCompatActivity(),
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

    performQuery(SearchType.ISBN, isbnString, getDefaultCallback());
  }

  private Consumer<List<LoanableBook>> getDefaultCallback() {
    return new Consumer<List<LoanableBook>>() {
      @Override
      public void accept(final List<LoanableBook> loanableBooks) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            new AlertDialog.Builder(getAppCompatActivity())
                .setMessage("GOTCHA: " + loanableBooks)
                .create()
                .show();
          }
        });
      }
    };
  }

  private void performQuery(SearchType searchType, String argument,
      Consumer<List<LoanableBook>> callback) {
    QueryTarget queryTarget = HttpUtil.getTargetFromSettings(getAppCompatActivity());

    new MultipleBookQuery(searchType, argument)
        .executeQuery(queryTarget, HttpUtil.getClient(), callback);
  }
}
