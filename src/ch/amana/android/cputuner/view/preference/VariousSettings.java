package ch.amana.android.cputuner.view.preference;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.ChangelogActivity;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class VariousSettings extends BaseSettings {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefVarious);
		} else {
			cputunerActionBar.setTitle(R.string.prefVarious);
		}
		addPreferencesFromResource(R.xml.settings_various);

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
				try {
					startActivity(i);
				} catch (Throwable e) {
				}
				return true;
			}
		});

		StringBuffer versionSB = new StringBuffer();
		versionSB.append(getString(R.string.label_version)).append(" ").append(settings.getVersionName());
		findPreference("prefKeyVersion").setTitle(versionSB.toString());
		findPreference("prefKeyChangelog").setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(VariousSettings.this, ChangelogActivity.class);
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
	protected String getHelpPage() {
		return HelpActivity.PAGE_SETTINGS;
	}

}
