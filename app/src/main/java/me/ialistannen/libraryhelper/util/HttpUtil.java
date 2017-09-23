package me.ialistannen.libraryhelper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.query.QueryTarget;
import me.ialistannen.libraryhelper.logic.server.JWTFetcher;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Some utility functions for HTTP.
 */
public class HttpUtil {

  private static OkHttpClient client;

  public static final MediaType JSON_MEDIA_TYPE = MediaType
      .parse("application/json; charset=utf-8");

  private static final AtomicReference<JWT> token = new AtomicReference<>();
  private static volatile boolean fetchingToken = false;
  private static final ConcurrentLinkedQueue<DelayedCallback> callbackQueue
      = new ConcurrentLinkedQueue<>();

  /**
   * @return The shared {@link OkHttpClient}
   */
  public static synchronized OkHttpClient getClient() {
    if (client == null) {
      client = new OkHttpClient();
    }
    return client;
  }

  /**
   * Executes a passed request using a shared {@link OkHttpClient}.
   *
   * @param request The {@link Request} to issue
   * @param callback The {@link Callback} to invoke
   */
  public static void makeCall(Request request, Context context, final Callback callback) {
    // We need to execute them, but first wait to get the token
    if (fetchingToken) {
      callbackQueue.add(new DelayedCallback(context, request, callback));
      return;
    }
    // two null checks as only the second is synchronized
    if (token.get() == null || token.get().isExpired()) {
      synchronized (token) {
        if (token.get() == null || token.get().isExpired()) {
          fetchingToken = true;
          callbackQueue.add(new DelayedCallback(context, request, callback));

          new JWTFetcher().fetch(context, new Consumer<String>() {
            @Override
            public void accept(String jwt) {
              token.set(new JWT(jwt));
              fetchingToken = false;

              // execute the waiting requests
              while (!callbackQueue.isEmpty()) {
                DelayedCallback delayedCallback = callbackQueue.poll();
                makeCall(
                    delayedCallback.request, delayedCallback.context, delayedCallback.callback
                );
              }
            }
          });

          // stop execution, we need to wait for the async fetcher to return and are then called
          // back
          return;
        }
      }
    }
    Request newRequest = request.newBuilder()
        .header("Authorization", "Bearer " + token.get().getToken())
        .build();
    getClient().newCall(newRequest).enqueue(callback);
  }

  /**
   * Returns the {@link QueryTarget} configured in the preferences of the context.
   *
   * @param context The {@link Context} to use
   * @return The corresponding query target
   * @throws UrlNotWellFormedException if the url is not well formed
   */
  public static QueryTarget getTargetFromSettings(Context context) {
    return new QueryTarget(getServerUrlFromSettings(context, EndpointType.SEARCH));
  }

  /**
   * Returns the url for the server configured in the preferences of the context.
   *
   * @param context The {@link Context} to use
   * @param endpointType The {@link EndpointType} to query
   * @return The corresponding url
   * @throws UrlNotWellFormedException if the url is not well formed
   */
  public static HttpUrl getServerUrlFromSettings(Context context, EndpointType endpointType) {
    String remoteUrl = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getString("preference_server_settings_remote_url", "localhost");
    int port = getIntFromPreferences(
        PreferenceManager.getDefaultSharedPreferences(context),
        "preference_server_settings_remote_port",
        8080
    );
    String endpoint = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getString(endpointType.getPreferenceKey(), "/null");

    if (!endpoint.startsWith("/")) {
      endpoint = "/" + endpoint;
    }

    HttpUrl httpUrl = HttpUrl.parse(remoteUrl + ":" + port + endpoint);

    if (httpUrl == null) {
      throw new UrlNotWellFormedException("Url is not well formed");
    }

    return httpUrl;
  }

  /**
   * Sends a message informing the user that the server url in the settings is not well formed.
   *
   * @param context The {@link Context} to use
   */
  public static void sendDefaultServerUrlNotWellFormed(Context context) {
    Toast.makeText(
        context,
        context.getString(R.string.settings_server_url_not_well_formed),
        Toast.LENGTH_SHORT
    ).show();
  }

  private static int getIntFromPreferences(SharedPreferences preferences, String key,
      int defValue) {
    try {
      return preferences.getInt(key, defValue);
    } catch (ClassCastException e) {
      return Integer.parseInt(preferences.getString(key, Integer.toString(defValue)));
    }
  }

  public enum EndpointType {
    SEARCH("search"),
    ADD("add"),
    DELETE("delete"),
    COVER("cover"),
    LENDING("lending"),
    LOGIN("login"),
    MODIFY("modify");

    private final String preferenceKey;

    EndpointType(String preferenceKey) {
      this.preferenceKey = "preference_server_settings_endpoint_" + preferenceKey;
    }

    public String getPreferenceKey() {
      return preferenceKey;
    }
  }

  private static class JWT {

    private String token;
    private Date expiration;

    JWT(String token) {
      this.token = token;
      this.expiration = extractExpirationDate(token);
    }

    private Date extractExpirationDate(String token) {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new IllegalArgumentException("Invalid token!");
      }

      String data = parts[1];
      String json = new String(Base64.decode(data, Base64.DEFAULT));
      JsonObject jsonObject = Json.fromJson(json, JsonObject.class);

      if (jsonObject == null) {
        throw new IllegalArgumentException("Invalid token!");
      }

      if (!jsonObject.has("exp") || !jsonObject.get("exp").isJsonPrimitive()) {
        return new Date(Long.MAX_VALUE);
      }

      long secondsSinceEpoch = jsonObject.getAsJsonPrimitive("exp").getAsLong();
      return new Date(TimeUnit.SECONDS.toMillis(secondsSinceEpoch));
    }

    String getToken() {
      return token;
    }

    boolean isExpired() {
      return expiration.before(new Date());
    }
  }

  private static class DelayedCallback {

    private Context context;
    private Request request;
    private Callback callback;

    DelayedCallback(Context context, Request request, Callback callback) {
      this.context = context;
      this.request = request;
      this.callback = callback;
    }
  }
}
