package ch.amana.android.cputuner.view.preference;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.CpuEditor;

public class CpuProfilePreferenceActivity extends PreferenceActivity {

	private final Map<Integer, Preference> prefsMap = new HashMap<Integer, Preference>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.cpu_profile_preferences);
		// initPowerProfile("prefKeyAcPower", PowerProfiles.PROFILE_AC, true);
		// initPowerProfile("prefKeyBatteryPower",
		// PowerProfiles.PROFILE_BATTERY, true);
		// initPowerProfile("prefKeyBatteryCrtitical",
		// PowerProfiles.PROFILE_BATTERY_CRITICAL, true);
		// initPowerProfile("prefKeyScreenOff", PowerProfiles.PROFILE_SCEENOFF,
		// true);

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSummary();
	}

	private void initPowerProfile(String prefKey, final int profile, boolean isSystemProfile) {
		Preference pref = findPreference(prefKey);
		prefsMap.put(profile, pref);
		Intent i = new Intent(this, CpuEditor.class);
		// i.putExtra(CpuModel.INTENT_EXTRA, profile);
		pref.setIntent(i);
		pref.setEnabled(SettingsStorage.getInstance().isEnableProfiles());
	}

	private void updateSummary() {
		// for (Iterator<Integer> iterator = prefsMap.keySet().iterator();
		// iterator.hasNext();) {
		// Integer profile = iterator.next();
		// Preference pref = prefsMap.get(profile);
		// CpuModel cpu = PowerProfiles.getCpuModelForProfile(profile);
		// pref.setSummary(cpu.toString());
		// }
	}
}
