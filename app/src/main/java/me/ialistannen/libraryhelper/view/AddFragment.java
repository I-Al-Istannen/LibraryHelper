package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import java.io.IOException;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.add.BookAdder;
import me.ialistannen.libraryhelper.logic.add.BookAdder.BookAddCallback;
import me.ialistannen.libraryhelper.logic.server.ServerResponseErrorType;
import me.ialistannen.libraryhelper.util.HttpUtil;

/**
 * A fragment that allows for adding new books.
 */
public class AddFragment extends IsbnInputFragment {

  private BookAdder bookAdder;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bookAdder = new BookAdder();
  }

  @Override
  public void onDestroy() {
    showWaitingSpinner(false);
    super.onDestroy();
  }

  @Override
  protected boolean onGotIsbnRequest(String isbnString) {
    showWaitingSpinner(true);
    return true;
  }

  @Override
  protected void consumeIsbn(Isbn isbn) {
    bookAdder.addBook(
        getFragmentHolderActivity(),
        HttpUtil.getClient(),
        new BookAddCallback() {

          private final Context context = getFragmentHolderActivity();

          @Override
          public void onFailure(@Nullable final IOException e, @Nullable final String error,
              @NonNull final ServerResponseErrorType type) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                hideSpinner();

                String message;

                if (type == ServerResponseErrorType.IO) {
                  assert e != null;
                  message = e.getLocalizedMessage();
                } else {
                  assert error != null;
                  message = error;
                }
                new AlertDialog.Builder(context)
                    .setTitle(R.string.add_fragment_error_adding_book_title)
                    .setMessage(message)
                    .create()
                    .show();
              }
            });
          }

          @Override
          public void onSuccess() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
              @Override
              public void run() {
                hideSpinner();

                Toast.makeText(
                    context,
                    context.getString(R.string.add_fragment_added_book_toast),
                    Toast.LENGTH_SHORT
                ).show();
              }
            });
          }

          private void hideSpinner() {
            if (isAdded()) {
              showWaitingSpinner(false);
            }
          }
        },
        isbn
    );
  }

}
