package ch.amana.android.cputuner.view.preference;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class SettingsMainActivity extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setTitle(R.string.labelSettingsTab);
		addPreferencesFromResource(R.xml.settings_main);

		startIntentForPref("prefKeyConfigurations", SettingsConfigurationsActivity.class);
		startIntentForPref("prefKeyGuiScreen", GuiSettings.class);
		startIntentForPref("prefKeySystemScreen", SystemSettings.class);
		startIntentForPref("prefKeyVarious", VariousSettings.class);
		
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

		Preference prefExtentions = findPreference("prefKeyExtentions");
		prefExtentions.setEnabled(settings.allowExtentions());
		prefExtentions.setOnPreferenceClickListener(new OnPreferenceClickListener() {

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
					}
					settings.setEnableProfiles(true);
					return true;
				}
				return false;
			}
		});

	}

	//
	//	@Override
	//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	//		super.onCreateContextMenu(menu, v, menuInfo);
	//		getMenuInflater().inflate(R.menu.db_list_context, menu);
	//	}
	//
	//	@Override
	//	public boolean onContextItemSelected(MenuItem item) {
	//		super.onContextItemSelected(item);
	//
	//		AdapterView.AdapterContextMenuInfo info;
	//		try {
	//			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	//		} catch (ClassCastException e) {
	//			Logger.e("bad menuInfo", e);
	//			return false;
	//		}
	//
	//		final Uri uri = ContentUris.withAppendedId(DB.ConfigurationAutoload.CONTENT_URI, info.id);
	//		switch (item.getItemId()) {
	//		case R.id.menuItemDelete:
	//			// deleteProfile(uri);
	//			return true;
	//
	//		case R.id.menuItemEdit:
	//			startActivity(new Intent(Intent.ACTION_EDIT, uri));
	//			return true;
	//
	//		default:
	//			return false;
	//		}
	//
	//	}

	@Override
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
