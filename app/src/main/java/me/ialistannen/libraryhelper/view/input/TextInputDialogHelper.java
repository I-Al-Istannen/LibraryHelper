package me.ialistannen.libraryhelper.view.input;

import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import me.ialistannen.libraryhelper.util.PixelUtil;

/**
 * A helper to create dialogs accepting text input.
 */
public class TextInputDialogHelper {

  /**
   * Makes a normal {@link AlertDialog} allow text input.
   *
   * @param alertBuilder The {@link AlertDialog} to convert
   * @return The {@link EditText} that was added, for you to query in the listeners
   */
  public static EditText makeTextInputDialog(AlertDialog.Builder alertBuilder) {
    FrameLayout frameLayout = new FrameLayout(alertBuilder.getContext());
    final EditText borrowerNameInput = new EditText(alertBuilder.getContext());
    int sidePadding = PixelUtil.dpToPixels(alertBuilder.getContext(), 20);

    // Adjust it to the default padding
    TypedArray typedArray = alertBuilder.getContext().obtainStyledAttributes(
        new int[]{android.support.v7.appcompat.R.attr.dialogPreferredPadding}
    );
    sidePadding = typedArray.getDimensionPixelSize(0, sidePadding);
    typedArray.recycle();

    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
    );
    layoutParams.setMarginStart(sidePadding);
    layoutParams.setMarginEnd(sidePadding);

    frameLayout.addView(borrowerNameInput, 0, layoutParams);

    alertBuilder.setView(frameLayout);

    return borrowerNameInput;
  }
}
