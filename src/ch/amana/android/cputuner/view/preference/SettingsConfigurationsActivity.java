package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
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

		startIntentForPref("prefKeyConfigurationsManage", ConfigurationManageActivity.class);
		startIntentForPref("prefKeyConfigurationsAutoLoad", ConfigurationAutoloadListActivity.class);
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
