package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import com.google.common.base.Function;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import me.ialistannen.isbnlookuplib.book.BookDataKey;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.isbnlookuplib.util.Price;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * Converts a {@link LoanableBook} to a List of string Pairs.
 */
class BookDataConverter {

  private Map<String, Function<Object, String>> specialConverters;
  private Set<BookDataKey> blacklistedKeys;

  BookDataConverter() {
    specialConverters = new HashMap<>();
    blacklistedKeys = new HashSet<>();
    addConverter(StandardBookDataKeys.ISBN.name(), new IsbnConverter());
    addConverter(StandardBookDataKeys.PRICE.name(), new PriceConverter());
    addConverter(StandardBookDataKeys.AUTHORS.name(), new AuthorConverter());

    blacklistedKeys.add(StandardBookDataKeys.ISBN_STRING);
    blacklistedKeys.add(StandardBookDataKeys.COVER_IMAGE_URL);
  }

  /**
   * Converts a {@link LoanableBook} to a displayable form.
   *
   * @param book The {@link LoanableBook} to convert
   * @param context The {@link Context} to use for resource lookups
   * @return A List with displayable key-value pairs
   */
  List<Pair<String, String>> convert(LoanableBook book, Context context) {
    List<Pair<String, String>> result = new ArrayList<>();

    for (Entry<BookDataKey, Object> entry : book.getAllData().entrySet()) {
      if (blacklistedKeys.contains(entry.getKey())) {
        continue;
      }
      String key = translateKeyWithFallback(context.getApplicationContext(), entry.getKey());
      String value;

      if (specialConverters.containsKey(entry.getKey().name())) {
        value = specialConverters.get(entry.getKey().name()).apply(entry.getValue());
      } else {
        value = entry.getValue().toString();
      }

      result.add(new Pair<>(key, value));
    }

    return result;
  }

  private String translateKeyWithFallback(Context context, BookDataKey bookDataKey) {
    String lookupKey = "book_data_key_" + normalizeName(bookDataKey.name()) + "_name";

    String packageName = context.getPackageName();
    int id = context.getResources().getIdentifier(lookupKey, "string", packageName);

    if (id != 0) {
      return context.getString(id);
    } else {
      return capitalizeName(bookDataKey.name());
    }
  }

  private String normalizeName(String name) {
    return name.toLowerCase().replaceAll("\\s", "_");
  }

  private String capitalizeName(String name) {
    StringBuilder builder = new StringBuilder();

    boolean upperCase = true;
    for (char c : name.toCharArray()) {
      if (Character.isWhitespace('c') || c == '_') {
        upperCase = true;
        builder.append(" ");
        continue;
      }
      if (upperCase) {
        builder.append(Character.toUpperCase(c));
        upperCase = false;
      } else {
        builder.append(Character.toLowerCase(c));
      }
    }

    return builder.toString();
  }

  private <T> void addConverter(String name, final Function<T, String> converter) {
    specialConverters.put(name, new Function<Object, String>() {
      @Override
      public String apply(Object o) {
        @SuppressWarnings("unchecked")
        T t = (T) o;
        return converter.apply(t);
      }
    });
  }

  private static class IsbnConverter implements Function<Isbn, String> {

    @Override
    public String apply(Isbn isbn) {
      return isbn.getDigitsAsString();
    }
  }

  private static class PriceConverter implements Function<Price, String> {

    private static NumberFormat format = NumberFormat.getNumberInstance();

    @Override
    public String apply(Price price) {
      return format.format(price.getPrice()) + " " + price.getCurrencyIdentifier();
    }
  }

  private static class AuthorConverter implements Function<List<Pair<String, String>>, String> {

    @Override
    public String apply(List<Pair<String, String>> personAndOccupation) {
      StringBuilder result = new StringBuilder();

      for (Pair<String, String> pair : personAndOccupation) {
        result.append(String.format("%s (%s)", pair.getKey(), pair.getValue()))
            .append("\n");
      }

      return result.toString();
    }
  }
}
