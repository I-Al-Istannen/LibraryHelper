package me.ialistannen.libraryhelper.logic.query;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.server.ApiErrorPOJO;
import me.ialistannen.libraryhelper.logic.server.ServerResponseErrorType;
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
  public abstract void executeQuery(QueryTarget target, OkHttpClient client,
      QueryCallback<T> callback);

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

  protected static class DefaultCallback implements Callback {

    private QueryCallback<List<LoanableBook>> callback;

    DefaultCallback(QueryCallback<List<LoanableBook>> callback) {
      this.callback = callback;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
      Log.w("Query", "Failure!", e);
      callback.onError(e, null, ServerResponseErrorType.IO);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
      ResponseBody body = response.body();

      if (body == null) {
        callback.onError(null, "Body is null", ServerResponseErrorType.RESPONSE_MALFORMED);
        return;
      }

      String bodyString = body.string();

      if (!response.isSuccessful()) {
        ApiErrorPOJO error = Json.getGson().fromJson(bodyString, ApiErrorPOJO.class);
        if (error == null) {
          callback.onError(
              null,
              "Response malformed: '" + bodyString + "'",
              ServerResponseErrorType.RESPONSE_MALFORMED
          );
          return;
        }
        callback.onError(null, error.message, ServerResponseErrorType.GENERIC_ERROR);
        return;
      }

      List<LoanableBook> books = booksFromJson(bodyString);

      if (books == null) {
        callback.onError(
            null,
            "Json malformed: '" + bodyString + "'",
            ServerResponseErrorType.RESPONSE_MALFORMED
        );
        return;
      }
      callback.onSuccess(books);
    }
  }

  public interface QueryCallback<T> {

    /**
     * Called when a book could not be looked up.
     *
     * @param exception The exception, if any
     * @param error The logical error, stating what is wrong with your data
     * @param type The type of the error
     */
    void onError(IOException exception, String error, ServerResponseErrorType type);

    /**
     * Called when a book was successfully added.
     *
     * @param value The response
     */
    void onSuccess(T value);

  }

  public enum SearchType {
    ISBN("isbn", R.string.search_type_isbn_name),
    TITLE_WILDCARD("title_wildcard", R.string.search_type_title_wildcard_name),
    TITLE_REGEX("title_regex", R.string.search_type_title_regex_name),
    AUTHOR_WILDCARD("author_wildcard", R.string.search_type_author_wildcard_name);

    private String value;
    private final int displayName;

    SearchType(String value, @StringRes int displayName) {
      this.value = value;
      this.displayName = displayName;
    }

    public @StringRes
    int getDisplayNameId() {
      return displayName;
    }

    public String getValue() {
      return value;
    }

    /**
     * @param context The context to use to resolve {@link #getDisplayNameId()}
     * @return A function transforming a {@link SearchType} to its display name
     */
    public static Function<SearchType, String> transformToDisplayName(final Context context) {
      return new Function<SearchType, String>() {
        @Override
        public String apply(SearchType searchType) {
          return context.getString(searchType.getDisplayNameId());
        }
      };
    }
  }
}
