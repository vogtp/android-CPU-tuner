package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.view.activity.CpuEditor;

public class CpuProfilePreferenceActivity extends PreferenceActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.cpu_profile_preferences);
		initPowerProfile("prefKeyAcPower", PowerProfiles.PROFILE_AC, false);
		initPowerProfile("prefKeyBatteryPower", PowerProfiles.PROFILE_BATTERY, false);
		initPowerProfile("prefKeyBatteryCrtitical", PowerProfiles.PROFILE_BATTERY_CRITICAL, false);
		// initPowerProfile("prefKeyBatteryGood",
		// PowerProfiles.PROFILE_BATTERY_GOOD, true);

	}

	private void initPowerProfile(String prefKey, final int profile, boolean hasPercentage) {
		Preference prefGoodWidget = findPreference(prefKey);
		Intent i = new Intent(this, CpuEditor.class);
		i.putExtra(CpuModel.INTENT_EXTRA, profile);
		prefGoodWidget.setIntent(i);
		prefGoodWidget.setEnabled(SettingsStorage.getInstance().isEnableProfiles());
	}
}
