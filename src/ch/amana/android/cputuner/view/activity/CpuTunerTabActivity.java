package ch.amana.android.cputuner.view.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.preference.SettingsPreferenceActivity;

public class CpuTunerTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tabCurrent").setIndicator("Current", getResources().getDrawable(R.drawable.phone))
				.setContent(new Intent(this, TuneCpu.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabTriggers").setIndicator("Triggers", getResources().getDrawable(R.drawable.battery))
				.setContent(new Intent(this, TriggersListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabProfiles").setIndicator("Profiles", getResources().getDrawable(R.drawable.cpu))
				.setContent(new Intent(this, ProfilesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		if (SettingsStorage.getInstance().isEnableBeta()) {
			tabHost.addTab(tabHost.newTabSpec("tabHelp").setIndicator("Help", getResources().getDrawable(R.drawable.help))
					.setContent(new Intent(this, HelpActivity.class)));

		}
		tabHost.addTab(tabHost.newTabSpec("tabSettings").setIndicator("Settings", getResources().getDrawable(R.drawable.configure))
				.setContent(new Intent(this, SettingsPreferenceActivity.class)));

	}
}
