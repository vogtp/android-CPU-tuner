package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import ch.amana.android.cputuner.R;

public class SettingsPreferenceActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_preferences);
	}

}
