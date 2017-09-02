package me.ialistannen.libraryhelper.logic.query;

import java.util.Collections;
import java.util.List;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * A Query that returns multiple books.
 */
public class MultipleBookQuery extends Query<List<LoanableBook>> {

  private String query;
  private SearchType searchType;

  public MultipleBookQuery(SearchType searchType, String query) {
    this.searchType = searchType;
    this.query = query;
  }

  @Override
  public void executeQuery(QueryTarget target, OkHttpClient client,
      final Consumer<List<LoanableBook>> callback) {

    Request request = getRequestForQuery(target, searchType, query);

    client
        .newCall(request)
        .enqueue(
            new DefaultCallback<List<LoanableBook>>(callback,
                Collections.<LoanableBook>emptyList()) {
              @Override
              protected void withBooks(List<LoanableBook> books) {
                callback.accept(books);
              }
            }
        );
  }
}
