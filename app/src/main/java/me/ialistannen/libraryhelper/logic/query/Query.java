package me.ialistannen.libraryhelper.logic.query;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelper.util.Json;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A query to the server.
 */
public abstract class Query<T> {

  /**
   * Executes the query.
   *
   * @param client The {@link OkHttpClient} to use
   * @param target The target
   * @param callback The callback to handle the result of the query.
   */
  public abstract void executeQuery(QueryTarget target, OkHttpClient client, Consumer<T> callback);

  /**
   * @param target The target of the query
   * @param searchType The search type
   * @param data The data to send
   * @return The {@link HttpUrl} for it.
   */
  Request getRequestForQuery(QueryTarget target, SearchType searchType, String data) {
    HttpUrl url = target.getUrl().newBuilder()
        .setQueryParameter("search_type", searchType.getValue())
        .setQueryParameter("query", data)
        .build();

    return new Request.Builder()
        .url(url)
        .get()
        .build();
  }

  private static Gson getGson() {
    return Json.getGson();
  }

  /**
   * @param json The json response
   * @return A list with {@link LoanableBook}s
   */
  static List<LoanableBook> booksFromJson(String json) {
    //@formatter:off
    Type type = new TypeToken<List<IntermediaryBook>>() {}.getType();
    //@formatter:on
    List<IntermediaryBook> books = getGson().fromJson(json, type);

    List<LoanableBook> results = new ArrayList<>(books.size());

    for (IntermediaryBook book : books) {
      results.add(book.toLoanableBook());
    }

    return results;
  }

  protected abstract static class DefaultCallback<T> implements Callback {

    private Consumer<T> callback;
    private T emptyResponse;

    DefaultCallback(Consumer<T> callback, T emptyResponse) {
      this.callback = callback;
      this.emptyResponse = emptyResponse;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      Log.w("MyThingyBook", "Failure!", e);
      callback.accept(emptyResponse);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
      ResponseBody body = response.body();

      if (!response.isSuccessful() || body == null) {
        Log.w("MyThingyBook", "Not successful: " + response.isSuccessful() + " " + (body == null));
        callback.accept(emptyResponse);
        return;
      }

      withBooks(booksFromJson(body.string()));
    }

    /**
     * @param books The books the server returned
     */
    protected abstract void withBooks(List<LoanableBook> books);
  }

  public enum SearchType {
    ISBN("isbn"),
    TITLE_WILDCARD("title_wildcard"),
    TITLE_REGEX("title_regex"),
    AUTHOR_WILDCARD("author_wildcard");

    private String value;

    SearchType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
