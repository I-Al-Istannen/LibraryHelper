package me.ialistannen.libraryhelper.logic.query;

import java.util.Objects;
import okhttp3.HttpUrl;

/**
 * A target for a query.
 */
public class QueryTarget {

  private HttpUrl url;

  public QueryTarget(HttpUrl url) {
    this.url = Objects.requireNonNull(url, "url can not be null!");
    ;
  }

  /**
   * @return The url for the query
   */
  public HttpUrl getUrl() {
    return url;
  }
}
