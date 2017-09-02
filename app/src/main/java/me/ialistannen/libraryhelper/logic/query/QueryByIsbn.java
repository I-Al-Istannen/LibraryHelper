package me.ialistannen.libraryhelper.logic.query;

import java.util.List;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Queries by {@link Isbn}.
 */
public class QueryByIsbn extends Query<Optional<LoanableBook>> {

  private String isbn;

  public QueryByIsbn(String isbn) {
    this.isbn = isbn;
  }

  @Override
  public void executeQuery(QueryTarget target, OkHttpClient client,
      final Consumer<Optional<LoanableBook>> callback) {

    Request request = getRequestForQuery(target, SearchType.ISBN, isbn);

    client.newCall(request).enqueue(
        new DefaultCallback<Optional<LoanableBook>>(
            callback, Optional.<LoanableBook>empty()
        ) {
          @Override
          protected void withBooks(List<LoanableBook> books) {
            callback.accept(
                books.isEmpty()
                    ? Optional.<LoanableBook>empty()
                    : Optional.of(books.get(0))
            );
          }
        }
    );
  }
}
