package me.ialistannen.libraryhelper.view.input.isbninputlayout;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.libraryhelper.view.input.AbstractTextWatcher;

/**
 * A {@link TextInputLayout} that allows inputting ISBNs.
 */
public class IsbnInputTextLayout extends TextInputLayout {

  public IsbnInputTextLayout(Context context) {
    super(context);
  }

  public IsbnInputTextLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public IsbnInputTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private TextWatcher postValidationListener;

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (getEditText() != null) {
      getEditText().setFilters(new InputFilter[]{new IsbnInputFilter()});
      getEditText().addTextChangedListener(
          new IsbnLayoutValidationWatcher(this, getContext(), new IsbnConverter())
      );
    }

  }

  /**
   * @param onPostValidationListener Whether the isbn was valid.
   */
  public void setOnPostValidation(final Consumer<Boolean> onPostValidationListener) {
    if (postValidationListener != null && getEditText() != null) {
      getEditText().removeTextChangedListener(postValidationListener);
    }
    this.postValidationListener = new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        onPostValidationListener.accept(getError() == null);
      }
    };

    if (getEditText() != null) {
      getEditText().addTextChangedListener(postValidationListener);
    }
  }

  @Override
  public int getBaseline() {
    return getEditText() == null ? -1 : getEditText().getBaseline() + getEditText().getPaddingTop();
  }
}
