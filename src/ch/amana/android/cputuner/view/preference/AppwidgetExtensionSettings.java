package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class AppwidgetExtensionSettings extends BaseSettings {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCatWidget);
		} else {
			cputunerActionBar.setTitle(R.string.prefCatWidget);
		}
		addPreferencesFromResource(R.xml.settings_widget_extention);


		if (Logger.DEBUG) {
			addPreferencesFromResource(R.xml.settings_widget_extention_debug);
			Preference prefEnableWidget = findPreference(SettingsStorage.PREF_KEY_WIDGET);
			prefEnableWidget.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					try {
						SettingsStorage.getInstance(getApplicationContext()).setHasWidget((Boolean) newValue);
						return true;
					} catch (Exception e) {
						Logger.w("Cannot parse prefKeyStatusbarAddToChoice as int", e);
						return false;
					}
				}
			});
		}

	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
