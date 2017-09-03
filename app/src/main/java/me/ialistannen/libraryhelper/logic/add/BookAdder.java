package me.ialistannen.libraryhelper.logic.add;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.gson.JsonObject;
import java.io.IOException;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A class that adds books to the server.
 */
public class BookAdder {

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  /**
   * Adds a book to the server.
   *
   * @param context The {@link Context} to get the settings from
   * @param client The {@link OkHttpClient} to use
   * @param isbn The {@link Isbn} of the book
   * @throws IllegalArgumentException if the url is invalid
   */
  public void addBook(final Context context, final OkHttpClient client,
      final BookAddCallback callback, Isbn isbn) {

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("isbn", isbn.getDigitsAsString());
    String json = Json.getGson().toJson(jsonObject);

    makeRequest(context, json, client, callback);
  }

  private void makeRequest(Context context, final String json, OkHttpClient client,
      final BookAddCallback callback) {

    HttpUrl url = HttpUtil.getServerUrlFromSettings(context, EndpointType.ADD);

    RequestBody requestBody = RequestBody.create(JSON, json);

    final Request request = new Request.Builder()
        .url(url)
        .put(requestBody)
        .build();

    Log.w("TEST_ME", "Request: " + request);

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NonNull Call call, @NonNull IOException e) {
        callback.onFailure(e, null, ErrorType.IO);
      }

      @Override
      public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
          callback.onFailure(null, "Unknown, got no body.", ErrorType.RESPONSE_MALFORMED);
          return;
        }

        String bodyString = body.string();
        JsonObject jsonObject = Json.getGson().fromJson(bodyString, JsonObject.class);

        if (jsonObject == null) {
          callback.onFailure(null, "Not a valid json object: " + bodyString,
              ErrorType.RESPONSE_MALFORMED);
          return;
        }

        if (!response.isSuccessful()) {
          String message;
          if (jsonObject.has("message")) {
            message = jsonObject.getAsJsonPrimitive("message").getAsString();
          } else {
            message = "Unknown body received: " + bodyString;
          }
          callback.onFailure(null, message, ErrorType.RESPONSE_MALFORMED);
          return;
        }

        if (!jsonObject.has("acknowledged")) {
          callback.onFailure(
              null,
              "Not acknowledged but no error...",
              ErrorType.RESPONSE_MALFORMED
          );
          return;
        }

        if (jsonObject.getAsJsonPrimitive("acknowledged").getAsBoolean()) {
          callback.onSuccess();
        } else {
          callback.onFailure(null, "Not acknowledged", ErrorType.NOT_ACKNOWLEDGED);
        }
      }
    });
  }

  public interface BookAddCallback {

    /**
     * Called when a book could not be looked up.
     *
     * @param e The exception, if any
     * @param error The logical error, stating what is wrong with your data
     * @param type The type of the erro
     */
    void onFailure(@Nullable IOException e, @Nullable String error, @NonNull ErrorType type);

    /**
     * Called when a book was successfully added.
     */
    void onSuccess();
  }

  public enum ErrorType {
    IO,
    NOT_ACKNOWLEDGED,
    RESPONSE_MALFORMED
  }
}
