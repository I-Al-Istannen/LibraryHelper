package me.ialistannen.libraryhelper.logic.query;

import android.content.Context;
import android.support.annotation.StringRes;
import com.google.common.base.Function;
import me.ialistannen.libraryhelper.R;

/**
 * The different possible search types.
 */
@SuppressWarnings("unused") // The fields are dynamically matched.
public enum SearchType {
  ISBN("isbn", R.string.search_type_isbn_name),
  TITLE_WILDCARD("title_wildcard", R.string.search_type_title_wildcard_name),
  TITLE_REGEX("title_regex", R.string.search_type_title_regex_name),
  TITLE_MATCH("title_match", R.string.search_type_title_match_name),
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
