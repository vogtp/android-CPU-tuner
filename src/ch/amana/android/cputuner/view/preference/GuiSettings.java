package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
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
		findPreference(SettingsStorage.ENABLE_STATUSBAR_ADDTO).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					settings.setEnableProfiles((Boolean) newValue);
				}
				return true;
			}
		});
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_GUI;
	}

}
