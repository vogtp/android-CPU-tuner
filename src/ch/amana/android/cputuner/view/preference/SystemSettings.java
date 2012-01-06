package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.CapabilityCheckerActivity;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCatSystem);
		} else {
			cputunerActionBar.setTitle(R.string.prefCatSystem);
		}
		addPreferencesFromResource(R.xml.settings_system);

		startIntentForPref("prefKeyProfiles", SystemProfilesSettings.class);
		startIntentForPref("prefKeyServiceSwitches", SystemServiceSwitchesSettings.class);
		startIntentForPref("prefKeyCpu", SystemCpuSettings.class);

		findPreference("prefKeyCapabilities").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SystemSettings.this, CapabilityCheckerActivity.class));
				return true;
			}
		});

	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_BACKEND;
	}

}
