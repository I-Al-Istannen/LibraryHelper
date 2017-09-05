package me.ialistannen.libraryhelper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.query.QueryTarget;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

/**
 * Some utility functions for HTTP.
 */
public class HttpUtil {

  private static OkHttpClient client;

  public static final MediaType JSON_MEDIATYPE = MediaType.parse("application/json; charset=utf-8");

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
    LENDING("lending");

    private final String preferenceKey;

    EndpointType(String preferenceKey) {
      this.preferenceKey = "preference_server_settings_endpoint_" + preferenceKey;
    }

    public String getPreferenceKey() {
      return preferenceKey;
    }
  }
}
