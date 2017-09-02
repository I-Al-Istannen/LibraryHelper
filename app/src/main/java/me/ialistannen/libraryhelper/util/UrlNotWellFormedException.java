package me.ialistannen.libraryhelper.util;

/**
 * Indicates an url is not well formed.
 */
public class UrlNotWellFormedException extends RuntimeException {

  public UrlNotWellFormedException(String message) {
    super(message);
  }
}
