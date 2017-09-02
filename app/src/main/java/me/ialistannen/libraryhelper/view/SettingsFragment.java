package me.ialistannen.libraryhelper.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.ListAdapter;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.activities.FragmentHolderActivity;

/**
 * The settings fragment.
 *
 * <p><strong><u>Beware</u></strong>: <em>This removes the support action bar from the
 * activity.</em>
 */
public class SettingsFragment extends PreferenceFragment implements
    OnSharedPreferenceChangeListener {

  private static final String PREF_KEY = "preference";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);

    if (getArguments() != null && getArguments().containsKey(PREF_KEY)) {
      String key = getArguments().getString(PREF_KEY);
      Preference preference = getPreferenceManager().findPreference(key);
      if (preference instanceof PreferenceScreen) {
        setPreferenceScreen((PreferenceScreen) preference);
      }
    }

    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    setInitialPreferenceSummaries();
  }

  private void setInitialPreferenceSummaries() {
    ListAdapter rootAdapter = getPreferenceScreen().getRootAdapter();
    int count = rootAdapter.getCount();
    for (int i = 0; i < count; i++) {
      updateSummary((Preference) rootAdapter.getItem(i));
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Preference preference = findPreference(key);
    updateSummary(preference);
  }

  private void updateSummary(Preference preference) {
    if (preference.getSummary() == null) {
      return;
    }
    String summary = preference.getSummary().toString();
    if (!preference.getExtras().containsKey("original_summary")) {
      preference.getExtras().putString("original_summary", summary);
    }

    summary = preference.getExtras().getString("original_summary");
    if (preference instanceof EditTextPreference) {
      summary = String.format(summary, ((EditTextPreference) preference).getText());
    } else {
      return;
    }

    preference.setSummary(summary);
  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (!(getActivity() instanceof FragmentHolderActivity)) {
      throw new IllegalStateException("Can only attach to a FragmentHolderActivity");
    }
    // enable nicer action bar up navigation
    ((FragmentHolderActivity) getActivity()).setActionbarUpPopsFragment(true);
  }

  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    if (preference instanceof PreferenceScreen && getActivity() instanceof FragmentHolderActivity) {
      // No popup, that removes the actionbar otherwise
      ((PreferenceScreen) preference).getDialog().dismiss();

      SettingsFragment next = new SettingsFragment();

      Bundle arguments = new Bundle();
      arguments.putString(PREF_KEY, preference.getKey());
      next.setArguments(arguments);

      ((FragmentHolderActivity) getActivity()).switchToFragmentPushBack(next);
    }
    return super.onPreferenceTreeClick(preferenceScreen, preference);
  }
}
