package ch.amana.android.cputuner.view.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.application.CpuTunerApplication;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.activity.UserExperianceLevelChooser;

public class GuiSettings extends BaseSettings {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCatGUI);
		} else {
			cputunerActionBar.setTitle(R.string.prefCatGUI);
		}
		addPreferencesFromResource(R.xml.settings_gui);

		findPreference("prefKeyLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(GuiSettings.this, (String) newValue);
				}
				return true;
			}
		});

		findPreference("prefKeyUserLevel").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				UserExperianceLevelChooser uec = new UserExperianceLevelChooser(GuiSettings.this, true);
				uec.show();
				return true;
			}
		});
		ListPreference prefCalcPowerUsageType = (ListPreference) findPreference("prefKeyCalcPowerUsageType");

		if (settings.isEnableBeta()) {
			prefCalcPowerUsageType.setEntryValues(R.array.prefCalcPowerUsageValuesBeta);
			prefCalcPowerUsageType.setEntries(R.array.prefCalcPowerUsageEntriesBeta);
		}

		prefCalcPowerUsageType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int newValueInt = -1;
				try {
					newValueInt = Integer.parseInt((String) newValue);
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyCalcPowerUsageType as int", e);
					return false;
				}
				switch (newValueInt) {
				case SettingsStorage.TRACK_CURRENT_AVG:
					if (BatteryHandler.getInstance().getBatteryCurrentAverage() == HardwareHandler.NO_VALUE_INT) {
						GuiUtils.showDialog(GuiSettings.this, R.string.not_supported, R.string.msg_no_avg_battery_current);
						return false;
					}
					break;
				case SettingsStorage.TRACK_CURRENT_CUR:
					if (BatteryHandler.getInstance().getBatteryCurrentAverage() == HardwareHandler.NO_VALUE_INT) {
						GuiUtils.showDialog(GuiSettings.this, R.string.not_supported, R.string.msg_no_battery_current);
						return false;
					}
					break;
				}
				ModelAccess.getInstace(GuiSettings.this).clearPowerUsage();
				return true;
			}
		});

		findPreference("prefKeyStatusbarAddToChoice").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int newValueInt = -1;
				try {
					newValueInt = Integer.parseInt((String) newValue);
				} catch (Exception e) {
					Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
					return false;
				}
				settings.setStatusbarAddto(newValueInt);
				final Context context = getApplicationContext();
				switch (newValueInt) {
				case SettingsStorage.STATUSBAR_NEVER:
					Notifier.stopStatusbarNotifications(context);
					break;
				case SettingsStorage.STATUSBAR_ALWAYS:
					Notifier.startStatusbarNotifications(context);
					break;
				case SettingsStorage.STATUSBAR_RUNNING:
					if (settings.isEnableProfiles()) {
						Notifier.startStatusbarNotifications(context);
					} else {
						Notifier.stopStatusbarNotifications(context);
					}
					break;

				}
				CpuTunerApplication.stopCpuTuner(getApplicationContext());
				CpuTunerApplication.startCpuTuner(getApplicationContext());
				updateView();
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	private void updateView() {
		findPreference("prefKeyStatusbarNotifications").setEnabled(settings.isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER);
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_GUI;
	}

}
