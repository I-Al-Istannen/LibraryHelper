package me.ialistannen.libraryhelper.view;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import me.ialistannen.libraryhelper.activities.FragmentHolderActivity;

/**
 * A base class for fragments.
 */
public class FragmentBase extends Fragment {

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
}
