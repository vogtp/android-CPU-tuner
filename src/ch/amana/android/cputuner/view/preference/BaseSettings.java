package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public abstract class BaseSettings extends PreferenceActivity {

	protected CputunerActionBar actionBar;
	protected SettingsStorage settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		settings = SettingsStorage.getInstance();

		actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setHomeAction(new ActionBar.Action() {

			@Override
			public void performAction(View view) {
				onBackPressed();
			}

			@Override
			public int getDrawable() {
				return R.drawable.cputuner_back;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		SettingsStorage.getInstance().forgetValues();
		CpuHandler.resetInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, getHelpPage())) {
				return true;
			}

		}
		return false;
	}


	protected void startIntentForPref(final String prefKey, final Class<?> settingsClass) {
		findPreference(prefKey).setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(BaseSettings.this, settingsClass));
				return true;
			}
		});
	}
	
	protected abstract String getHelpPage();
}
