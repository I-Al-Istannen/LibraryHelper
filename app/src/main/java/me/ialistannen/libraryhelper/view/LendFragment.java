package me.ialistannen.libraryhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.gson.JsonObject;
import java.io.IOException;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelper.util.PixelUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A fragment to handle the lend status of a book.
 */
public class LendFragment extends IsbnInputFragment {

  @Override
  protected void consumeIsbn(Isbn isbn) {
    new AlertDialog.Builder(getFragmentHolderActivity())
        .setTitle("Lending a book")
        .setMessage("Do you want to lend it or receive it back?")
        .setNegativeButton("Lend", getLendListener(isbn))
        .setPositiveButton("Receive", getReceiveListener(isbn))
        .create()
        .show();
  }

  private OnClickListener getReceiveListener(final Isbn isbn) {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        HttpUrl httpUrl = HttpUtil
            .getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.LENDING)
            .newBuilder()
            .setQueryParameter("isbn", isbn.getDigitsAsString())
            .build();

        final Request request = new Request.Builder()
            .url(httpUrl)
            .delete()
            .build();

        HttpUtil.getClient().newCall(request).enqueue(new Callback() {
          @Override
          public void onFailure(@NonNull Call call, final @NonNull IOException e) {
            showErrorDialog(e.getLocalizedMessage());
          }

          @Override
          public void onResponse(@NonNull Call call, @NonNull final Response response)
              throws IOException {

            JsonObject jsonObject = verifyServerResponse(response, "deleted");

            if (jsonObject == null) {
              return;
            }

            if (!jsonObject.getAsJsonPrimitive("deleted").getAsBoolean()) {
              showErrorDialog(getString(R.string.lend_fragment_server_refused));
              return;
            }

            toastIfAdded(R.string.lend_fragment_received_book_back);
          }
        });
      }
    };
  }

  private void toastIfAdded(@StringRes final int message) {
    doSyncIfAdded(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(
            getFragmentHolderActivity(),
            getString(message),
            Toast.LENGTH_SHORT
        ).show();
      }
    });
  }

  private void showErrorDialog(final String message) {
    doSyncIfAdded(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(getFragmentHolderActivity())
            .setTitle(getString(R.string.add_fragment_error_adding_book_title))
            .setMessage(message)
            .create()
            .show();
      }
    });
  }

  @Nullable
  private JsonObject verifyServerResponse(Response response, String propertyName)
      throws IOException {
    ResponseBody body = response.body();

    if (body == null) {
      showErrorDialog(getString(R.string.server_response_body_is_null));
      return null;
    }

    String bodyString = body.string();
    JsonObject jsonObject = Json.getGson().fromJson(bodyString, JsonObject.class);

    if (jsonObject == null || !jsonObject.has(propertyName)) {
      showErrorDialog(getString(R.string.server_response_malformed, bodyString));
      return null;
    }
    return jsonObject;
  }

  private OnClickListener getLendListener(final Isbn isbn) {
    return new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        FrameLayout frameLayout = new FrameLayout(getFragmentHolderActivity());
        final EditText borrowerNameInput = new EditText(getFragmentHolderActivity());
        int sidePadding = PixelUtil.dpToPixels(getFragmentHolderActivity(), 20);

        // Adjust it to the default padding
        TypedArray typedArray = getFragmentHolderActivity().obtainStyledAttributes(
            new int[]{android.support.v7.appcompat.R.attr.dialogPreferredPadding}
        );
        sidePadding = typedArray.getDimensionPixelSize(0, sidePadding);
        typedArray.recycle();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMarginStart(sidePadding);
        layoutParams.setMarginEnd(sidePadding);

        frameLayout.addView(borrowerNameInput, 0, layoutParams);

        new Builder(getFragmentHolderActivity())
            .setView(frameLayout)
            .setTitle(getString(R.string.lend_fragment_input_borrower_title))
            .setMessage(getString(R.string.lend_fragment_input_borrower_message))
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(
                    getFragmentHolderActivity(),
                    borrowerNameInput.getText(),
                    Toast.LENGTH_SHORT
                ).show();
                addBorrower(isbn, borrowerNameInput.getText().toString());
              }
            })
            .setNegativeButton(android.R.string.cancel, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
              }
            })
            .create()
            .show();
      }
    };
  }

  private void addBorrower(Isbn isbn, String borrower) {
    HttpUrl url = HttpUtil
        .getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.LENDING)
        .newBuilder()
        .setQueryParameter("isbn", isbn.getDigitsAsString())
        .build();

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("borrower", borrower);
    String json = Json.getGson().toJson(jsonObject);

    RequestBody body = RequestBody.create(HttpUtil.JSON_MEDIATYPE, json);

    Request request = new Request.Builder()
        .url(url)
        .put(body)
        .build();

    HttpUtil.getClient().newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        showErrorDialog(e.getLocalizedMessage());
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        JsonObject jsonObject = verifyServerResponse(response, "added");

        if (jsonObject == null) {
          return;
        }

        if (!jsonObject.getAsJsonPrimitive("added").getAsBoolean()) {
          showErrorDialog(getString(R.string.lend_fragment_server_refused));
          return;
        }

        toastIfAdded(R.string.lend_fragment_lent_book);
      }
    });
  }

  private void doSyncIfAdded(Runnable runnable) {
    if (!isAdded()) {
      return;
    }
    new Handler(Looper.getMainLooper()).post(runnable);
  }
}
