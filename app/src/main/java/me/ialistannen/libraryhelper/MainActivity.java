package me.ialistannen.libraryhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.ialistannen.libraryhelper.activities.FragmentHolderActivity;
import me.ialistannen.libraryhelper.view.AddFragment;
import me.ialistannen.libraryhelper.view.DeleteFragment;
import me.ialistannen.libraryhelper.view.QueryFragment;
import me.ialistannen.libraryhelper.view.SettingsFragment;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    if (savedInstanceState == null) {
      setSupportActionBar((Toolbar) findViewById(R.id.activity_main_action_bar));

      ActionBar supportActionBar = getSupportActionBar();
      if (supportActionBar != null) {
        supportActionBar.setDisplayShowTitleEnabled(true);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_activity_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.w("FRAG", "ID: " + item.getItemId());
    if(item.getItemId() == R.id.main_activity_action_bar_settings) {
      Log.w("FRAG", "Calling!");
      startFragmentActivity(SettingsFragment.class.getSimpleName());
      return true;
    }
    return super.onContextItemSelected(item);
  }

  @OnClick(R.id.button_accept)
  void onAddClicked() {
    startFragmentActivity(AddFragment.class.getSimpleName());
  }

  private void startFragmentActivity(String tag) {
    Intent intent = new Intent(this, FragmentHolderActivity.class);
    intent.putExtra(FragmentHolderActivity.FRAGMENT_EXTRA_KEY, tag);
    startActivity(intent);
  }

  @OnClick(R.id.button_remove)
  void onRemoveClicked() {
    startFragmentActivity(DeleteFragment.class.getSimpleName());
  }

  @OnClick(R.id.button_search)
  void onQueryClicked() {
    startFragmentActivity(QueryFragment.class.getSimpleName());
  }
}
