package me.ialistannen.libraryhelper.util;

import android.support.annotation.StringRes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.ialistannen.libraryhelper.R;

/**
 * A {@link BasicFragmentServerCallback} that extracts a boolean response via a specified key.
 */
public class JsonExtractingServerCallback extends BasicFragmentServerCallback<JsonObject> {

  private final String jsonKey;
  private final int successTost;
  private final int failureDialog;

  /**
   * @param context The context to use
   * @param jsonKey The key in the returned json
   * @param title The message to use as the dialog title
   * @param successToast The message to toast if it was successful
   * @param failureDialog The dialog message to show it was unsuccessful
   */
  public JsonExtractingServerCallback(Object context, String jsonKey,
      @StringRes int title,
      @StringRes int successToast, @StringRes int failureDialog) {
    super(context, title, JsonObject.class);
    this.jsonKey = jsonKey;
    this.successTost = successToast;
    this.failureDialog = failureDialog;
  }

  @Override
  protected void onPojoReceived(JsonObject pojo) {
    if (!pojo.has(jsonKey)) {
      showDialog(R.string.server_response_malformed_misses_key, jsonKey);
      return;
    }
    JsonElement jsonElement = pojo.get(jsonKey);
    if (!jsonElement.isJsonPrimitive()) {
      showDialog(R.string.server_response_malformed_not_a_json_primitive);
      return;
    }

    if (jsonElement.getAsBoolean()) {
      toast(successTost);
    } else {
      showDialog(failureDialog);
    }
  }
}
