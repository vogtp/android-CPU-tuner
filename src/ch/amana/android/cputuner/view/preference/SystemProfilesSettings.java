package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemProfilesSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefKeyProfiles);
		addPreferencesFromResource(R.xml.settings_system_profiles);
		findPreference("prefKeyCallInProgressProfile").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					Boolean b = (Boolean) newValue;
					if (b) {
						CallPhoneStateListener.register(getApplicationContext());
					} else {
						CallPhoneStateListener.unregister(getApplicationContext());
					}
				}
				// we just start and stop let the system handle the settings...
				return false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		findPreference("prefKeyUseVirtualGovernors").setEnabled(!settings.isBeginnerUser());
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_PROFILE;
	}

}
