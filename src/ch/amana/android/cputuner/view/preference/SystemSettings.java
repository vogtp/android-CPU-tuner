package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import ch.amana.android.cputuner.R;
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
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
