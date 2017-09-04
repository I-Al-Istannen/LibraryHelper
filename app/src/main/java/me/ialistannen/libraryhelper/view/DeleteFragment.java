package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.google.gson.JsonObject;
import java.io.IOException;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.server.ApiErrorPOJO;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelper.util.UrlNotWellFormedException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        Toast.makeText(getFragmentHolderActivity(), "Dek: " + doNotAskAgain, Toast.LENGTH_SHORT)
            .show();
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

      HttpUtil.getClient().newCall(request).enqueue(new DefaultCallback(this));
    } catch (UrlNotWellFormedException ignored) {
      HttpUtil.sendDefaultServerUrlNotWellFormed(getFragmentHolderActivity());
    }
  }

  private class DefaultCallback implements Callback {

    private Context context;

    private DefaultCallback(DeleteFragment fragment) {
      this.context = fragment.getFragmentHolderActivity();
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      showDialog(e.getLocalizedMessage());
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
      showWaitingSpinner(false);
      ResponseBody body = response.body();
      if (body == null) {
        showDialog("Unknown, got no body.");
        return;
      }

      String bodyString = body.string();
      JsonObject jsonObject = Json.getGson().fromJson(bodyString, JsonObject.class);

      if (jsonObject == null) {
        showDialog("Not a valid json object: '" + bodyString + "'");
        return;
      }

      if (!response.isSuccessful()) {
        ApiErrorPOJO error = Json.getGson().fromJson(bodyString, ApiErrorPOJO.class);
        showDialog(error.message == null ? "Unknown" : error.message);
        return;
      }

      DeletePojo deletePojo = Json.getGson().fromJson(bodyString, DeletePojo.class);

      if (deletePojo.deleted) {
        doSyncIfAdded(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(
                context,
                "Successfully deleted the book",
                Toast.LENGTH_SHORT
            ).show();
          }
        });
      } else {
        showDialog("Server said no.");
      }
    }

    private void showDialog(final String message) {
      doSyncIfAdded(new Runnable() {
        @Override
        public void run() {
          new AlertDialog.Builder(context)
              .setTitle(R.string.delete_fragment_error_deleting_book_title)
              .setMessage(message)
              .create()
              .show();
        }
      });
    }

    private void doSyncIfAdded(Runnable runnable) {
      if (!isAdded()) {
        return;
      }
      new Handler(Looper.getMainLooper()).post(runnable);
    }

    private class DeletePojo {
      boolean deleted;
    }
  }
}
