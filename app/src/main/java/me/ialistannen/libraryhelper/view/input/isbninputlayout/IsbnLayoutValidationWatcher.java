package me.ialistannen.libraryhelper.view.input.isbninputlayout;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelper.R;

/**
 * A {@link TextWatcher} that validates ISBNs and sets the error, if it is invalid.
 */
class IsbnLayoutValidationWatcher implements TextWatcher {

  private IsbnConverter isbnConverter;
  private TextInputLayout targetLayout;
  private final Context context;

  IsbnLayoutValidationWatcher(TextInputLayout targetLayout, Context context,
      IsbnConverter isbnConverter) {
    this.targetLayout = targetLayout;
    this.context = context;
    this.isbnConverter = isbnConverter;
  }

  @Override
  public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
    Optional<Isbn> isbnOptional = isbnConverter.fromString(charSequence.toString());
    if (isbnOptional.isPresent()) {
      targetLayout.setError(null);
    } else {
      targetLayout.setError(context.getString(R.string.invalid_isbn));
    }
  }

  @Override
  public void afterTextChanged(Editable editable) {
  }
}
