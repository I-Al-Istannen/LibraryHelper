package me.ialistannen.libraryhelper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ialistannen.libraryhelpercommon.book.IntermediaryBook;

/**
 * Some utility functions to deal with Json.
 */
public class Json {

  private static Gson GSON;

  /**
   * @return The main Gson instance.
   */
  public static synchronized Gson getGson() {
    if (GSON == null) {
      GSON = IntermediaryBook.configureGson(new GsonBuilder()).create();
    }
    return GSON;
  }
}
