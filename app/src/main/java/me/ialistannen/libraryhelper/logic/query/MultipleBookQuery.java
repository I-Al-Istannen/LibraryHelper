package me.ialistannen.libraryhelper.logic.query;

import java.util.Collections;
import java.util.List;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * A Query that returns multiple books.
 */
public class MultipleBookQuery extends Query<List<LoanableBook>> {

  public static final List<LoanableBook> ERROR_RESPONSE = Collections.emptyList();

  private String query;
  private SearchType searchType;

  public MultipleBookQuery(SearchType searchType, String query) {
    this.searchType = searchType;
    this.query = query;
  }

  @Override
  public void executeQuery(QueryTarget target, OkHttpClient client,
      final QueryCallback<List<LoanableBook>> callback) {

    Request request = getRequestForQuery(target, searchType, query);

    client
        .newCall(request)
        .enqueue(new DefaultCallback(callback));
  }
}
