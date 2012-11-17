package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemProfilesSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefKeyProfiles);
		} else {
			cputunerActionBar.setTitle(R.string.prefKeyProfiles);
		}
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
				return true;
			}
		});
		//		findPreference(SettingsStorage.PREF_KEY_SWITCH_CPU_SETTINGS).setIntent(new Intent(ProfileEditor.ACTION_EDIT_SWITCHPROFILE));

	}

	@Override
	protected void onResume() {
		super.onResume();
		//		findPreference("prefKeyUseVirtualGovernors").setEnabled(!settings.isBeginnerUser());
		//		findPreference("prefCatSwitchCpuSettings").setEnabled(SettingsStorage.getInstance(this).isPowerUser());
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_PROFILE;
	}

}
