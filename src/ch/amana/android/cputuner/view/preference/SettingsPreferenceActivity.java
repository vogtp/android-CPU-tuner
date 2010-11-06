package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class SettingsPreferenceActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_preferences);

		Preference enableProfilePreference = findPreference(SettingsStorage.ENABLE_PROFILES);
		enableProfilePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					Boolean enableProfile = (Boolean) newValue;
					Intent intent = new Intent(SettingsPreferenceActivity.this, BatteryService.class);
					if (enableProfile) {
						startService(intent);
						PowerProfiles.reapplyCurProfile();
					} else {
						stopService(intent);
					}
				}
				return true;
			}
		});
		Preference enableStatusBarPreference = findPreference(SettingsStorage.ENABLE_STATUSBAR_NOTI);
		enableStatusBarPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					if (SettingsStorage.getInstance().isEnableProfiles()) {
						Intent intent = new Intent(SettingsPreferenceActivity.this, BatteryService.class);
						startService(intent);
					}
				}
				return true;
			}
		});
	}
}
