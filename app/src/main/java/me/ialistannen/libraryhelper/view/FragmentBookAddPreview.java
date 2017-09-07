package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.base.Function;
import com.google.gson.JsonObject;
import java.util.Locale;
import me.ialistannen.isbnlookuplib.book.Book;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.lookup.IsbnLookupProvider;
import me.ialistannen.isbnlookuplib.lookup.providers.amazon.AmazonIsbnLookupProvider;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelper.util.JsonExtractingServerCallback;
import me.ialistannen.libraryhelper.view.booklist.BookDetailLayout;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A fragment with a detail preview for a book that allows adding it to the server.
 */
public class FragmentBookAddPreview extends FragmentBase {

  @SuppressWarnings("WeakerAccess")
  @BindView(R.id.book_detail_layout)
  BookDetailLayout detailLayout;

  private Isbn isbn;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_book_add_preview, container, false);

    ButterKnife.bind(this, view);

    detailLayout.showPlaceholder(true);
    detailLayout.setCoverUrlProvider(new Function<LoanableBook, String>() {
      @Override
      public String apply(LoanableBook book) {
        return book.getData(StandardBookDataKeys.COVER_IMAGE_URL);
      }
    });

    return view;
  }

  public void setIsbn(Isbn isbn) {
    this.isbn = isbn;

    final IsbnLookupProvider lookupProvider = new AmazonIsbnLookupProvider(
        Locale.GERMAN, new IsbnConverter()
    );

    new AsyncTask<Isbn, Void, LoanableBook>() {

      @Override
      protected LoanableBook doInBackground(Isbn... isbns) {
        Optional<Book> bookOptional = lookupProvider.lookup(isbns[0]);
        if (bookOptional.isPresent()) {
          return new LoanableBook(bookOptional.get());
        }
        return null;
      }

      @Override
      protected void onPostExecute(final LoanableBook book) {
        if (!isAdded()) {
          return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            if (book == null) {
              new AlertDialog.Builder(getFragmentHolderActivity())
                  .setTitle(R.string.add_fragment_error_adding_book_title)
                  .setMessage(R.string.add_fragment_isbn_lookup_error)
                  .create()
                  .show();
              detailLayout.setError();
              return;
            }

            detailLayout.setBook(book);
          }
        });
      }
    }.execute(isbn);
  }

  @OnClick(R.id.add_button)
  void onAddBook() {
    addBookToServer();
  }

  private void addBookToServer() {
    showWaitingSpinner(true);

    HttpUrl url = HttpUtil.getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.ADD);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("isbn", isbn.getDigitsAsString());
    String json = Json.toJson(jsonObject);

    RequestBody requestBody = RequestBody.create(HttpUtil.JSON_MEDIATYPE, json);

    final Request request = new Request.Builder()
        .url(url)
        .put(requestBody)
        .build();

    HttpUtil.getClient().newCall(request).enqueue(
        new JsonExtractingServerCallback(
            this, "acknowledged",
            R.string.query_fragment_error_querying_server_title,
            R.string.add_fragment_added_book_toast,
            R.string.add_fragment_server_refused
        ) {
          @Override
          protected void onPostExecute() {
            showWaitingSpinner(false);
          }
        }
    );
  }
}
