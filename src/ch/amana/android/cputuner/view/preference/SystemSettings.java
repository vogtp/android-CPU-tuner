package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.CapabilityCheckerActivity;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefCatSystem);
		addPreferencesFromResource(R.xml.settings_system);

		startIntentForPref("prefKeyProfiles", SystemProfilesSettings.class);
		startIntentForPref("prefKeyServiceSwitches", SystemServiceSwitchesSettings.class);
		startIntentForPref("prefKeyCpu", SystemCpuSettings.class);

		findPreference("prefKeyCapabilities").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SystemSettings.this, CapabilityCheckerActivity.class);
				intent.putExtra(CapabilityCheckerActivity.EXTRA_RECHEK, true);
				startActivity(intent);
				return true;
			}
		});

	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_BACKEND;
	}

}
