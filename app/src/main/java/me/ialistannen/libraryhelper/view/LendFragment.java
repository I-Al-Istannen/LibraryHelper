package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import com.google.gson.JsonObject;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelper.util.JsonExtractingServerCallback;
import me.ialistannen.libraryhelper.view.input.TextInputDialogHelper;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A fragment to handle the lend status of a book.
 */
public class LendFragment extends IsbnInputFragment {

  @Override
  protected void consumeIsbn(Isbn isbn) {
    new Builder(getFragmentHolderActivity())
        .setTitle(getString(R.string.lend_fragment_choice_dialog_title))
        .setMessage(getString(R.string.lend_fragment_choice_dialog_message))
        .setNegativeButton(R.string.lend_fragment_choice_dialog_lend, getLendListener(isbn))
        .setPositiveButton(R.string.lend_fragment_choice_dialog_receive, getReceiveListener(isbn))
        .create()
        .show();
  }

  private OnClickListener getReceiveListener(final Isbn isbn) {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        showWaitingSpinner(true);

        HttpUrl httpUrl = getHttpUrl(isbn);

        final Request request = new Request.Builder()
            .url(httpUrl)
            .delete()
            .build();

        JsonExtractingServerCallback callback = new JsonExtractingServerCallback(
            LendFragment.this, "deleted",
            R.string.lend_fragment_received_book_back,
            R.string.lend_fragment_server_refused
        ) {
          @Override
          protected void onPostExecute() {
            showWaitingSpinner(false);
          }
        };
        HttpUtil.getClient().newCall(request).enqueue(callback);
      }
    };
  }

  private HttpUrl getHttpUrl(Isbn isbn) {
    return HttpUtil
        .getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.LENDING)
        .newBuilder()
        .setQueryParameter("isbn", isbn.getDigitsAsString())
        .build();
  }

  private OnClickListener getLendListener(final Isbn isbn) {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getFragmentHolderActivity());
        final EditText borrowerNameInput = TextInputDialogHelper.makeTextInputDialog(builder);
        builder
            .setTitle(getString(R.string.lend_fragment_input_borrower_title))
            .setMessage(getString(R.string.lend_fragment_input_borrower_message))
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                addBorrower(isbn, borrowerNameInput.getText().toString());
              }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .show();
      }
    };
  }

  private void addBorrower(Isbn isbn, String borrower) {
    showWaitingSpinner(true);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("borrower", borrower);
    String json = Json.getGson().toJson(jsonObject);

    RequestBody body = RequestBody.create(HttpUtil.JSON_MEDIATYPE, json);

    Request request = new Request.Builder()
        .url(getHttpUrl(isbn))
        .put(body)
        .build();

    HttpUtil.getClient().newCall(request).enqueue(
        new JsonExtractingServerCallback(
            this, "added",
            R.string.lend_fragment_lent_book, R.string.lend_fragment_server_refused
        ) {
          @Override
          protected void onPostExecute() {
            showWaitingSpinner(false);
          }
        }
    );
  }
}
