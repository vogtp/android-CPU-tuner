package ch.amana.android.cputuner.view.preference;

import android.os.Bundle;
import android.preference.ListPreference;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SystemCpuSettings extends BaseSettings {

	private ListPreference maxDefaultFreq;
	private ListPreference minDefaultFreq;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCpu);
		} else {
			cputunerActionBar.setTitle(R.string.prefCpu);
		}
		addPreferencesFromResource(R.xml.settings_system_cpu);
	
		int[] availCpuFreq = CpuHandler.getInstance().getAvailCpuFreq(true);
		String freqs[] = new String[availCpuFreq.length];
		for (int i = 0; i < availCpuFreq.length; i++) {
			freqs[i] = Integer.toString(availCpuFreq[i]);
		}
		maxDefaultFreq = (ListPreference) findPreference(SettingsStorage.PREF_KEY_MAX_FREQ);
		maxDefaultFreq.setEntries(freqs);
		maxDefaultFreq.setEntryValues(freqs);

		minDefaultFreq = (ListPreference) findPreference(SettingsStorage.PREF_KEY_MIN_FREQ);
		minDefaultFreq.setEntries(freqs);
		minDefaultFreq.setEntryValues(freqs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findPreference("prefKeyCpuFreq").setEnabled(!settings.isBeginnerUser());
		findPreference("prefKeyEnableUserspaceGovernor").setEnabled(settings.isPowerUser());
		findPreference("prefKeyMinSensibleFrequency").setEnabled(!(settings.isBeginnerUser()));
		maxDefaultFreq.setEnabled(!settings.isBeginnerUser());
		minDefaultFreq.setEnabled(!settings.isBeginnerUser());
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS_CPU;
	}

}
