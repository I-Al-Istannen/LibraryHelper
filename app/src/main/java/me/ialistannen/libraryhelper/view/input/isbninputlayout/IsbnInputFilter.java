package me.ialistannen.libraryhelper.view.input.isbninputlayout;

import android.text.InputFilter;
import android.text.Spanned;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link InputFilter} to only allow valid ISBN chars.
 */
public class IsbnInputFilter implements InputFilter {

  private static final Set<Character> ALLOWED_CHARACTERS = new HashSet<>(
      Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X')
  );

  @Override
  public CharSequence filter(CharSequence source, int start, int end, Spanned destination,
      int dstart, int dend) {

    for (char c : source.toString().toCharArray()) {
      if (!ALLOWED_CHARACTERS.contains(c)
          && !ALLOWED_CHARACTERS.contains(Character.toUpperCase(c))) {
        return "";
      }
    }
    return source.toString().toUpperCase();
  }
}
