package me.ialistannen.libraryhelper.logic.server;

/**
 * An enum indicating the type of the error that occurred.
 */
public enum ServerResponseErrorType {
  IO,
  NOT_ACKNOWLEDGED,
  RESPONSE_MALFORMED,
  GENERIC_ERROR
}
