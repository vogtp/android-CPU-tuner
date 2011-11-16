package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.activity.UserExperianceLevelChooser;

public class GuiSettings extends BaseSettings {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.prefCatGUI);
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
				switch (newValueInt) {
					case SettingsStorage.STATUSBAR_NEVER:
						Notifier.stopStatusbarNotifications(getApplicationContext());
						break;
				case SettingsStorage.STATUSBAR_ALWAYS:
						Notifier.startStatusbarNotifications(getApplicationContext());
						break;
				case SettingsStorage.STATUSBAR_RUNNING:
						if (settings.isEnableProfiles()) {
							Notifier.startStatusbarNotifications(getApplicationContext());
						} else {
							Notifier.stopStatusbarNotifications(getApplicationContext());
						}
						break;

				}
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
