package ch.amana.android.cputuner.view.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.helper.SystemAppHelper;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;
import ch.amana.android.cputuner.view.activity.CapabilityCheckerActivity;
import ch.amana.android.cputuner.view.activity.VirtualGovernorListActivity;

public class SettingsPreferenceActivity extends PreferenceActivity {

	private CheckBoxPreference systemAppPreference;
	private Preference virtualGovPref;
	private EditTextPreference cpuFreqPreference;
	private EditTextPreference prefMinSensibleFrequency;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_preferences);

		Preference capabilityPreference = findPreference("prefKeyCapabilities");
		capabilityPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingsPreferenceActivity.this, CapabilityCheckerActivity.class);
				intent.putExtra(CapabilityCheckerActivity.EXTRA_RECHEK, true);
				startActivity(intent);
				return true;
			}
		});

		Preference enableProfilePreference = findPreference(SettingsStorage.ENABLE_PROFILES);
		enableProfilePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					Boolean enableProfile = (Boolean) newValue;
					Intent intent = new Intent(SettingsPreferenceActivity.this, BatteryService.class);
					if (enableProfile) {
						startService(intent);
						PowerProfiles.getInstance().reapplyProfile(true);
					} else {
						stopService(intent);
					}
				}
				return true;
			}
		});
		Preference enableStatusBarPreference = findPreference(SettingsStorage.ENABLE_STATUSBAR_ADDTO);
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

		systemAppPreference = (CheckBoxPreference) findPreference("prefKeySystemApp");
		systemAppPreference.setChecked(RootHandler.isSystemApp(this));
		systemAppPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				return SystemAppHelper.install(SettingsPreferenceActivity.this, (Boolean) newValue);
			}

		});
		prefMinSensibleFrequency = (EditTextPreference) findPreference("prefKeyMinSensibleFrequency");
cpuFreqPreference = (EditTextPreference) findPreference("prefKeyCpuFreq");
//		cpuFreqPreference.setEnabled(!CpuHandler.getInstance().hasAvailCpuFreq());

		findPreference("prefKeyBuyMeABeer").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:ch.almana.android.buymeabeer")));
				return true;
			}
		});
		findPreference("prefKeyResetToDefault").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				InstallHelper.resetToDefault(SettingsPreferenceActivity.this);
				return true;
			}
		});

		virtualGovPref = findPreference("prefKeyVritualGovernorsList");
		virtualGovPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Context ctx = SettingsPreferenceActivity.this;
				ctx.startActivity(new Intent(ctx, VirtualGovernorListActivity.class));
				return true;
			}
		});

		findPreference("prefKeyLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(SettingsPreferenceActivity.this, (String) newValue);

				}

				return true;
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		systemAppPreference.setEnabled(SettingsStorage.getInstance().isInstallAsSystemAppEnabled());
		virtualGovPref.setEnabled(false);//!SettingsStorage.getInstance().isBeginnerUser());
		cpuFreqPreference.setEnabled(!SettingsStorage.getInstance().isBeginnerUser());
		prefMinSensibleFrequency.setEnabled(!(SettingsStorage.getInstance().isBeginnerUser() || SettingsStorage.getInstance().isPowerUser()));
	}

	@Override
	protected void onPause() {
		super.onPause();
		SettingsStorage.getInstance().forgetValues();
	}

}
