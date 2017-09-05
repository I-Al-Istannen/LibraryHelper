package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.JsonExtractingServerCallback;
import me.ialistannen.libraryhelper.util.UrlNotWellFormedException;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Deletes a book from the server.
 */
public class DeleteFragment extends IsbnInputFragment {

  private boolean doNotAsk;

  @Override
  protected void consumeIsbn(Isbn isbn) {
    if (doNotAsk) {
      deleteBook(isbn);
    } else {
      confirmDeletionWithUser(isbn);
    }
  }

  private void confirmDeletionWithUser(Isbn isbn) {
    new Builder(getFragmentHolderActivity())
        .setTitle(getString(R.string.delete_fragment_confirm_tite))
        .setMessage(getString(R.string.delete_fragment_confirm_message))
        // hacky way to have three buttons.
        .setNeutralButton(
            getString(R.string.delete_fragment_confirm_button_no),
            getNopListener()
        )
        .setNegativeButton(
            getString(R.string.delete_fragment_confirm_button_yes),
            getDeletingListener(false, isbn)
        )
        .setPositiveButton(
            getString(R.string.delete_fragment_confirm_button_yes_to_all),
            getDeletingListener(true, isbn)
        )
        .create()
        .show();
  }

  private Dialog.OnClickListener getNopListener() {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    };
  }

  private Dialog.OnClickListener getDeletingListener(final boolean doNotAskAgain, final Isbn isbn) {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        doNotAsk = doNotAskAgain;
        deleteBook(isbn);
      }
    };
  }

  private void deleteBook(Isbn isbn) {
    showWaitingSpinner(true);
    try {
      HttpUrl httpUrl = HttpUtil
          .getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.DELETE)
          .newBuilder()
          .setQueryParameter("isbn", isbn.getDigitsAsString())
          .build();

      Request request = new Request.Builder()
          .url(httpUrl)
          .delete()
          .build();

      HttpUtil.getClient().newCall(request).enqueue(
          new JsonExtractingServerCallback(
              this, "deleted",
              R.string.delete_fragment_error_deleting_book_title,
              R.string.delete_fragment_book_deleted,
              R.string.delete_fragment_server_refused_deletion
          ) {
            @Override
            protected void onPostExecute() {
              showWaitingSpinner(false);
            }
          }
      );
    } catch (UrlNotWellFormedException ignored) {
      showWaitingSpinner(false);
      HttpUtil.sendDefaultServerUrlNotWellFormed(getFragmentHolderActivity());
    }
  }
}
