package me.ialistannen.libraryhelper.view;

import com.google.gson.JsonObject;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelper.util.JsonExtractingServerCallback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A fragment that allows for adding new books.
 */
public class AddFragment extends IsbnInputFragment {

  @Override
  protected void consumeIsbn(Isbn isbn) {
    showWaitingSpinner(true);

    HttpUrl url = HttpUtil.getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.ADD);

    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("isbn", isbn.getDigitsAsString());
    String json = Json.toJson(jsonObject);

    RequestBody requestBody = RequestBody.create(HttpUtil.JSON_MEDIATYPE, json);

    final Request request = new Request.Builder()
        .url(url)
        .put(requestBody)
        .build();

    HttpUtil.getClient().newCall(request).enqueue(
        new JsonExtractingServerCallback(
            this, "acknowledged",
            R.string.query_fragment_error_querying_server_title,
            R.string.add_fragment_added_book_toast,
            R.string.add_fragment_server_refused
        ) {
          @Override
          protected void onPostExecute() {
            showWaitingSpinner(false);
          }
        }
    );
  }

}
