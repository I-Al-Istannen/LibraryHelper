package me.ialistannen.libraryhelper.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;

/**
 * Some utility functions to deal with Json.
 */
public class Json {

  private static Gson GSON;

  /**
   * @return The main Gson instance.
   */
  private static synchronized Gson getGson() {
    if (GSON == null) {
      GSON = IntermediaryBook.configureGson(new GsonBuilder()).create();
    }
    return GSON;
  }

  /**
   * Converts a Json string back to an object.
   *
   * @param json The json string to convert
   * @param classOfT The type of the object
   * @param <T> The type of the object
   * @return The created object or null if an error occurred
   */
  @Nullable
  public static <T> T fromJson(@NonNull String json, @NonNull Class<T> classOfT) {
    try {
      return getGson().fromJson(json, classOfT);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }

  /**
   * Converts a Json string back to an object.
   *
   * @param json The json string to convert
   * @param type The type of the object
   * @param <T> The type of the object
   * @return The created object or null if an error occurred
   */
  @Nullable
  public static <T> T fromJson(@NonNull String json, @NonNull Type type) {
    try {
      return getGson().fromJson(json, type);
    } catch (JsonSyntaxException e) {
      return null;
    }
  }

  /**
   * Converts a Json string back to an object.
   *
   * @param object The object to convert
   * @return The object as a String
   */
  @NonNull
  public static String toJson(@NonNull Object object) {
    return getGson().toJson(object);
  }
}
