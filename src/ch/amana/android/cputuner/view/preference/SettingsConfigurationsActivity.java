package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.ConfigurationAutoloadListActivity;
import ch.amana.android.cputuner.view.activity.ConfigurationManageActivity;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SettingsConfigurationsActivity extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefConfigurations);

		addPreferencesFromResource(R.xml.settings_configurations);

		PreferenceScreen configurationsManageScreen = (PreferenceScreen) findPreference("prefKeyConfigurationsManage");
		configurationsManageScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsConfigurationsActivity.this, ConfigurationManageActivity.class);
				startActivity(i);
				return true;
			}
		});
		PreferenceScreen configurationsAutoloadScreen = (PreferenceScreen) findPreference("prefKeyConfigurationsAutoLoad");
		configurationsAutoloadScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsConfigurationsActivity.this, ConfigurationAutoloadListActivity.class);
				startActivity(i);
				return true;
			}
		});
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
