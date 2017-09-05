package me.ialistannen.libraryhelper.view;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import me.ialistannen.libraryhelper.activities.FragmentHolderActivity;

/**
 * A base class for fragments.
 */
public class FragmentBase extends Fragment {

  private Dialog dialog;

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (!(context instanceof FragmentHolderActivity)) {
      throw new IllegalArgumentException("Can only attach to FragmentHolderActivitys");
    }
  }

  protected FragmentHolderActivity getFragmentHolderActivity() {
    return (FragmentHolderActivity) getActivity();
  }

  /**
   * @param show Whether to show the bar, or not
   */
  void showWaitingSpinner(boolean show) {
    if (!show || !isAdded()) {
      if (dialog != null && dialog.isShowing()) {
        dialog.dismiss();
      }
      return;
    }
    if (dialog == null) {
      dialog = new Dialog(getFragmentHolderActivity());

      dialog.getWindow().setDimAmount(0.7F);

      LayoutParams attributes = dialog.getWindow().getAttributes();
      ProgressBar progressBar = new ProgressBar(getFragmentHolderActivity());
      progressBar.setIndeterminate(true);

      dialog.getWindow().addContentView(progressBar, attributes);
    }
    dialog.show();
  }

}
