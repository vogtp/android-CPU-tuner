package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemProfilesSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefKeyProfiles);
		addPreferencesFromResource(R.xml.settings_system_profiles);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findPreference("prefKeyUseVirtualGovernors").setEnabled(!settings.isBeginnerUser());
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
