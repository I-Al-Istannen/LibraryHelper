package me.ialistannen.libraryhelper.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.isbn.IsbnConverter;
import me.ialistannen.isbnlookuplib.util.Consumer;
import me.ialistannen.isbnlookuplib.util.Optional;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.input.isbninputlayout.IsbnInputTextLayout;

/**
 * A fragment allowing the user to input an ISBN via barcode or directly.
 */
public abstract class IsbnInputFragment extends FragmentBase {

  private IsbnConverter isbnConverter;

  @BindView(R.id.isbn_input_field)
  IsbnInputTextLayout isbnInputField;

  @BindView(R.id.button_accept)
  Button acceptButton;

  @BindView(R.id.progress_bar)
  ProgressBar progressBar;

  private String lastIsbn;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_isbn_input, container, false);

    setHasOptionsMenu(true);

    ButterKnife.bind(this, view);

    isbnConverter = new IsbnConverter();

    acceptButton.setEnabled(false);

    isbnInputField.setOnPostValidation(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean isValid) {
        acceptButton.setEnabled(isValid);
      }
    });

    return view;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.fragment_isbn_input_action_bar_scan) {
      IntentIntegrator intentIntegrator = IntentIntegrator.forFragment(this);
      intentIntegrator.initiateScan();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.fragment_isbn_input_action_bar, menu);
  }

  @OnClick(R.id.button_accept)
  void onAcceptIsbn() {
    EditText editText = isbnInputField.getEditText();
    if (editText == null) {
      return;
    }

    consumeIsbn(editText.getText().toString());
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

    if (scanResult == null) {
      return;
    }

    final String isbnString = scanResult.getContents();
    if (isbnString == null || isbnString.isEmpty()) {
      return;
    }

    EditText editText = isbnInputField.getEditText();
    if (editText != null) {
      editText.setText(isbnString);
      lastIsbn = isbnString;
    }

    consumeIsbn(isbnString);
  }

  private void consumeIsbn(String isbn) {
    Optional<Isbn> isbnOptional = isbnConverter.fromString(isbn);
    if (!isbnOptional.isPresent()) {
      Toast.makeText(
          getFragmentHolderActivity(),
          getString(R.string.invalid_isbn),
          Toast.LENGTH_SHORT
      ).show();
      return;
    }
    consumeIsbn(isbnOptional.get());
  }

  @Override
  public void onResume() {
    super.onResume();
    if (lastIsbn != null && isbnInputField != null && isbnInputField.getEditText() != null) {
      isbnInputField.getEditText().setText(lastIsbn);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (isbnInputField.getEditText() != null) {
      String text = isbnInputField.getEditText().getText().toString();
      if (!text.trim().isEmpty()) {
        lastIsbn = text;
      }
    }
  }

  /**
   * @param isbn The {@link Isbn} the user entered.
   */
  protected abstract void consumeIsbn(Isbn isbn);
}