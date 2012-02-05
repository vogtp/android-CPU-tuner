package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class AdvStatisticsExtensionSettings extends BaseSettings {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCatAdvStatistics);
		} else {
			cputunerActionBar.setTitle(R.string.prefCatAdvStatistics);
		}
		addPreferencesFromResource(R.xml.settings_adv_stats_extention);

		findPreference(SettingsStorage.PREF_KEY_ADV_STATS).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					SettingsStorage.getInstance(getApplicationContext()).setAdvancesStatistics((Boolean) newValue);
					return true;
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
					return false;
				}
			}
		});
		findPreference(SettingsStorage.PREF_KEY_ENABLE_SWITCH_LOG).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					SettingsStorage.getInstance(getApplicationContext()).setEnableLogProfileSwitches((Boolean) newValue);
					return true;
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
					return false;
				}
			}
		});
		findPreference(SettingsStorage.PREF_KEY_ENABLE_STATISTICS_SERVICE).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					SettingsStorage.getInstance(getApplicationContext()).setRunStatisticsService((Boolean) newValue);
					return true;
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
					return false;
				}
			}
		});
		findPreference("prefKeyProfileSwitchLogSize").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					if (Integer.parseInt((String) newValue) > 0) {
						SwitchLog.start(getApplicationContext());
					} else {
						SwitchLog.stop(getApplicationContext());
					}
					return true;
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
					return false;
				}
			}
		});
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
