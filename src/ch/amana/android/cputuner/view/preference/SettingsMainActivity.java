package ch.amana.android.cputuner.view.preference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.CapabilityCheckerActivity;
import ch.amana.android.cputuner.view.activity.ChangelogActivity;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.activity.UserExperianceLevelChooser;

public class SettingsMainActivity extends BaseSettings {

	private EditTextPreference cpuFreqPreference;
	private EditTextPreference prefMinSensibleFrequency;
	private ListPreference maxDefaultFreq;
	private ListPreference minDefaultFreq;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.labelSettingsTab);
		addPreferencesFromResource(R.xml.settings_main);

		findPreference("prefKeyConfigurations").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(SettingsMainActivity.this, SettingsConfigurationsActivity.class));
				return true;
			}
		});
		findPreference("prefKeyBuyMeABeer").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsMainActivity.this, BillingProductListActiviy.class);
				i.putExtra(BillingProductListActiviy.EXTRA_TITLE, getString(R.string.prefBuyMeABeer));
				i.putExtra(BillingProductListActiviy.EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_BUY_ME_BEER);
				startActivity(i);
				return true;
			}
		});
		findPreference("prefKeyExtentions").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsMainActivity.this, BillingProductListActiviy.class);
				i.putExtra(BillingProductListActiviy.EXTRA_TITLE, getString(R.string.prefBuyMeABeer));
				i.putExtra(BillingProductListActiviy.EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_EXTENTIONS);
				startActivity(i);
				return true;
			}
		});

		findPreference(SettingsStorage.ENABLE_PROFILES).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(final Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					Boolean b = (Boolean) newValue;
					if (!b) {
						Builder alertBuilder = new AlertDialog.Builder(SettingsMainActivity.this);
						alertBuilder.setTitle(R.string.msg_disable_cpu_tuner);
						alertBuilder.setMessage(R.string.msg_disable_cputuner_question);
						alertBuilder.setNegativeButton(R.string.no, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								settings.setEnableProfiles(true);
								((CheckBoxPreference) preference).setChecked(true);
							}
						});
						alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								settings.setEnableProfiles(false);
								((CheckBoxPreference) preference).setChecked(false);
							}
						});
						AlertDialog alert = alertBuilder.create();
						alert.show();
						return false;
					} else {
						settings.setEnableProfiles(true);
					}
				}
				return true;
			}
		});

		Preference capabilityPreference = findPreference("prefKeyCapabilities");
		capabilityPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(SettingsMainActivity.this, CapabilityCheckerActivity.class);
				intent.putExtra(CapabilityCheckerActivity.EXTRA_RECHEK, true);
				startActivity(intent);
				return true;
			}
		});

		Preference enableStatusBarPreference = findPreference(SettingsStorage.ENABLE_STATUSBAR_ADDTO);
		enableStatusBarPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					settings.setEnableProfiles((Boolean) newValue);
				}
				return true;
			}
		});

		prefMinSensibleFrequency = (EditTextPreference) findPreference("prefKeyMinSensibleFrequency");
		cpuFreqPreference = (EditTextPreference) findPreference("prefKeyCpuFreq");


		findPreference("prefKeyLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(SettingsMainActivity.this, (String) newValue);
				}
				return true;
			}
		});

		//		findPreference("prefKeyLegalOxigenIcons").setOnPreferenceClickListener(new OnPreferenceClickListener() {
		//
		//			@Override
		//			public boolean onPreferenceClick(Preference preference) {
		//				Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://www.oxygen-icons.org/?page_id=4"));
		//				startActivity(i);
		//				return true;
		//			}
		//		});
		findPreference("prefKeyLegalGnomeIcons").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://www.gnu.org/licenses/gpl-2.0.html"));
				startActivity(i);
				return true;
			}
		});

		findPreference("prefKeyLegalServiceIcons").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://www.apache.org/licenses/LICENSE-2.0.html"));
				startActivity(i);
				return true;
			}
		});

		StringBuffer versionSB = new StringBuffer();
		versionSB.append(getString(R.string.label_version)).append(" ").append(getString(R.string.version));
		findPreference("prefKeyVersion").setTitle(versionSB.toString());
		findPreference("prefKeyChangelog").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsMainActivity.this, ChangelogActivity.class);
				startActivity(i);
				return true;
			}
		});

		findPreference("prefKeyVersion").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri fromParts = Uri.parse("market://search?q=pname:" + getPackageName());
				i.setData(fromParts);
				startActivity(i);
				return true;
			}
		});

		findPreference("prefKeyUserLevel").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				UserExperianceLevelChooser uec = new UserExperianceLevelChooser(SettingsMainActivity.this, true);
				uec.show();
				return true;
			}
		});

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

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event) {
	//		switch (event.getKeyCode()) {
	//		case KeyEvent.KEYCODE_BACK:
	//			helpPage = HelpActivity.PAGE_SETTINGS;
	//			break;
	//		case KeyEvent.KEYCODE_MENU:
	//			openOptionsMenu();
	//			break;
	//
	//		default:
	//			break;
	//		}
	//		return super.onKeyDown(keyCode, event);
	//	}

	@Override
	protected void onResume() {
		super.onResume();
		// systemAppPreference.setEnabled(settings.isInstallAsSystemAppEnabled());
		cpuFreqPreference.setEnabled(!settings.isBeginnerUser());
		prefMinSensibleFrequency.setEnabled(!(settings.isBeginnerUser() || settings.isPowerUser()));
		findPreference("prefKeyUseVirtualGovernors").setEnabled(!settings.isBeginnerUser());
		findPreference("prefKeyEnableUserspaceGovernor").setEnabled(settings.isPowerUser());
		maxDefaultFreq.setEnabled(!settings.isBeginnerUser());
		minDefaultFreq.setEnabled(!settings.isBeginnerUser());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.db_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Logger.e("bad menuInfo", e);
			return false;
		}

		final Uri uri = ContentUris.withAppendedId(DB.ConfigurationAutoload.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			// deleteProfile(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;

		default:
			return handleCommonMenu(item);
		}

	}

	private boolean handleCommonMenu(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			startActivity(new Intent(Intent.ACTION_INSERT, DB.CpuProfile.CONTENT_URI));
			return true;
		}
		return false;
	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
