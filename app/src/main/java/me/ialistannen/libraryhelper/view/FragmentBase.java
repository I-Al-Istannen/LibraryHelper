package me.ialistannen.libraryhelper.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * A base class for fragments.
 */
public class FragmentBase extends Fragment {

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setRetainInstance(true);
  }

  protected AppCompatActivity getAppCompatActivity() {
    return (AppCompatActivity) getActivity();
  }
}
