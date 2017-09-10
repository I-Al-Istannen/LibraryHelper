package me.ialistannen.libraryhelper.logic.server;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.BasicFragmentServerCallback;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

/**
 * Fetches a JWT token from the server using the provided credentials.
 */
public class JWTFetcher {

  /**
   * @param context The {@link Context} to use
   * @param callback The callback to invoke with the fetched token
   */
  public void fetch(final Context context, final Consumer<String> callback) {
    String username = PreferenceManager
        .getDefaultSharedPreferences(context).getString(
            "preference_server_settings_account_username", null
        );

    if (username == null) {
      showErrorDialog(
          context.getString(R.string.error_fetching_jwt_message_username_not_set), context
      );
      return;
    }

    String password = PreferenceManager
        .getDefaultSharedPreferences(context).getString(
            "preference_server_settings_account_password", null
        );

    if (password == null) {
      showErrorDialog(
          context.getString(R.string.error_fetching_jwt_message_password_not_set), context
      );
      return;
    }

    HttpUrl url = HttpUtil.getServerUrlFromSettings(context, EndpointType.LOGIN);
    final String json = Json.toJson(new RequestPojo(username, password));

    RequestBody body = RequestBody.create(HttpUtil.JSON_MEDIA_TYPE, json);

    Request request = new Builder()
        .url(url)
        .post(body)
        .build();

    HttpUtil.getClient().newCall(request).enqueue(
        new BasicFragmentServerCallback<JsonObject>(
            context, R.string.error_fetching_jwt_title, JsonObject.class
        ) {
          @Override
          protected void onPojoReceived(JsonObject pojo) {
            if (!pojo.has("token")) {
              showDialog(R.string.server_response_malformed_misses_key, "token");
              return;
            }
            JsonElement jsonElement = pojo.get("token");
            if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
              showDialog(R.string.server_response_malformed_not_a_json_primitive);
              return;
            }

            String tokenString = jsonElement.getAsString();
            callback.accept(tokenString);
          }
        }
    );
  }

  private void showErrorDialog(final String message, final Context context) {
    doSync(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(context)
            .setTitle(R.string.error_fetching_jwt_title)
            .setMessage(message)
            .create()
            .show();
      }
    });
  }

  private void doSync(Runnable runnable) {
    new Handler(Looper.getMainLooper()).post(runnable);
  }

  private static class RequestPojo {

    @SuppressWarnings("unused")
    private String username;
    @SuppressWarnings("unused")
    private String password;

    RequestPojo(String username, String password) {
      this.username = username;
      this.password = password;
    }
  }
}
