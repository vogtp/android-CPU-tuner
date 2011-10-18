package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemServiceSwitchesSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefServiceSwitches);
		addPreferencesFromResource(R.xml.settings_system_serviceswitches);
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_SERVICE_SWITCHES;
	}

}
