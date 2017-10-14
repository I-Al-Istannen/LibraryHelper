package me.ialistannen.libraryhelper.logic.query;

import android.content.Context;
import android.support.annotation.StringRes;
import com.google.common.base.Function;
import me.ialistannen.libraryhelper.R;

/**
 * A field you can query
 */
@SuppressWarnings("unused") // The fields are dynamically matched.
public enum QueryField {
  ISBN("isbn", R.string.query_field_isbn_name),
  TITLE("title", R.string.query_field_title_name),
  AUTHOR("author", R.string.query_field_author_name),
  LOCATION("location", R.string.query_field_location_name);

  private String value;
  private final int displayName;

  QueryField(String value, @StringRes int displayName) {
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
  public static Function<QueryField, String> transformToDisplayName(final Context context) {
    return new Function<QueryField, String>() {
      @Override
      public String apply(QueryField queryField) {
        return context.getString(queryField.getDisplayNameId());
      }
    };
  }

}
