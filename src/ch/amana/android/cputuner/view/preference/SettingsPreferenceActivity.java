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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.service.BatteryService;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SettingsPreferenceActivity extends PreferenceActivity {

	// private CheckBoxPreference systemAppPreference;
	private EditTextPreference cpuFreqPreference;
	private EditTextPreference prefMinSensibleFrequency;
	private String helpPage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_preferences);

		helpPage = HelpActivity.PAGE_SETTINGS;

		findPreference(SettingsStorage.ENABLE_PROFILES).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(final Preference preference, Object newValue) {
				if (newValue instanceof Boolean) {
					Boolean b = (Boolean) newValue;
					if (!b) {
						Builder alertBuilder = new AlertDialog.Builder(SettingsPreferenceActivity.this);
						alertBuilder.setTitle(R.string.msg_disable_cpu_tuner);
						alertBuilder.setMessage(R.string.msg_disable_cputuner_question);
						alertBuilder.setNegativeButton(R.string.no, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingsStorage.getInstance().setEnableProfiles(true);
								((CheckBoxPreference) preference).setChecked(true);
							}
						});
						alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SettingsStorage.getInstance().setEnableProfiles(false);
								((CheckBoxPreference) preference).setChecked(false);
							}
						});
						AlertDialog alert = alertBuilder.create();
						alert.show();
						return false;
					}
				}
				return true;
			}
		});

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

		// Preference enableProfilePreference =
		// findPreference(SettingsStorage.ENABLE_PROFILES);
		// enableProfilePreference.setOnPreferenceChangeListener(new
		// OnPreferenceChangeListener() {
		//
		// @Override
		// public boolean onPreferenceChange(Preference preference, Object
		// newValue) {
		// if (newValue instanceof Boolean) {
		// Boolean enableProfile = (Boolean) newValue;
		// Intent intent = new Intent(SettingsPreferenceActivity.this,
		// BatteryService.class);
		// if (enableProfile) {
		// startService(intent);
		// PowerProfiles.getInstance().reapplyProfile(true);
		// } else {
		// stopService(intent);
		// }
		// }
		// return true;
		// }
		// });
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

		// systemAppPreference = (CheckBoxPreference)
		// findPreference("prefKeySystemApp");
		// systemAppPreference.setChecked(RootHandler.isSystemApp(this));
		// systemAppPreference.setOnPreferenceChangeListener(new
		// OnPreferenceChangeListener() {
		//
		// @Override
		// public boolean onPreferenceChange(Preference preference, Object
		// newValue) {
		// return SystemAppHelper.install(SettingsPreferenceActivity.this,
		// (Boolean) newValue);
		// }
		//
		// });
		prefMinSensibleFrequency = (EditTextPreference) findPreference("prefKeyMinSensibleFrequency");
		cpuFreqPreference = (EditTextPreference) findPreference("prefKeyCpuFreq");
		// cpuFreqPreference.setEnabled(!CpuHandler.getInstance().hasAvailCpuFreq());

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

		findPreference("prefKeyLanguage").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue instanceof String) {
					GuiUtils.setLanguage(SettingsPreferenceActivity.this, (String) newValue);
				}
				return true;
			}
		});

		findPreference("prefKeyLegalOxigenIcons").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://www.oxygen-icons.org/?page_id=4"));
				startActivity(i);
				return true;
			}
		});
		findPreference("prefKeyLegalGnomeIcons").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://www.gnu.org/licenses/gpl-2.0.html"));
				startActivity(i);
				return true;
			}
		});

		PreferenceScreen configurationsManageScreen = (PreferenceScreen) findPreference("prefKeyConfigurationsManage");
		configurationsManageScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsPreferenceActivity.this, ConfigurationManageActivity.class);
				startActivity(i);
				return true;
			}
		});
		PreferenceScreen configurationsAutoloadScreen = (PreferenceScreen) findPreference("prefKeyConfigurationsAutoLoad");
		configurationsAutoloadScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(SettingsPreferenceActivity.this, ConfigurationAutoloadListActivity.class);
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
				Intent i = new Intent(SettingsPreferenceActivity.this, ChangelogActivity.class);
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
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			helpPage = HelpActivity.PAGE_SETTINGS;
			break;
		case KeyEvent.KEYCODE_MENU:
			openOptionsMenu();
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SettingsStorage settings = SettingsStorage.getInstance();
		// systemAppPreference.setEnabled(settings.isInstallAsSystemAppEnabled());
		cpuFreqPreference.setEnabled(!settings.isBeginnerUser());
		prefMinSensibleFrequency.setEnabled(!(settings.isBeginnerUser() || settings.isPowerUser()));
		findPreference("prefKeyUseVirtualGovernors").setEnabled(!settings.isBeginnerUser());
		findPreference("prefKeyEnableUserspaceGovernor").setEnabled(settings.isPowerUser());
	}

	@Override
	protected void onPause() {
		super.onPause();
		SettingsStorage.getInstance().forgetValues();
		CpuHandler.resetInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, helpPage)) {
				return true;
			}

		}
		return false;
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

}
