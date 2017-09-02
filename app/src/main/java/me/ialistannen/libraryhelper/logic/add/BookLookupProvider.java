package me.ialistannen.libraryhelper.logic.add;

import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import java.util.Locale;
import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.lookup.IsbnLookupProvider;
import me.ialistannen.isbnlookuplib.lookup.providers.amazon.AmazonIsbnLookupProvider;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * Looks up books!
 */
public class BookLookupProvider {

  private static IsbnLookupProvider lookupProvider;

  /**
   * Looks up an ISBN.
   *
   * @param isbn The {@link Isbn} too look up
   * @param callback The callback to notify
   */
  public void lookup(final Isbn isbn, final Consumer<Optional<LoanableBook>> callback) {
    new AsyncTask<Isbn, Void, LoanableBook>() {

      @Override
      protected LoanableBook doInBackground(Isbn... strings) {
        Optional<Book> lookup = getLookupProvider().lookup(isbn);
        if (!lookup.isPresent()) {
          return null;
        }
        return new LoanableBook(lookup.get());
      }

      @Override
      protected void onPostExecute(LoanableBook loanableBook) {
        callback.accept(Optional.ofNullable(loanableBook));
      }
    }.execute(isbn);
  }

  private static synchronized IsbnLookupProvider getLookupProvider() {
    if (lookupProvider == null) {
      lookupProvider = new AmazonIsbnLookupProvider(Locale.GERMAN, new IsbnConverter());
    }
    return lookupProvider;
  }

  /**
   * Releases all static references.
   */
  public void dispose() {
    lookupProvider = null;
  }
}
