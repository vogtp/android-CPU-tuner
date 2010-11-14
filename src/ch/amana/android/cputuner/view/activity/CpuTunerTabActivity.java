package ch.amana.android.cputuner.view.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import ch.amana.android.cputuner.view.preference.SettingsPreferenceActivity;

public class CpuTunerTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tabCurrent").setIndicator("Current")
				.setContent(new Intent(this, TuneCpu.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabTriggers").setIndicator("Triggers")
				.setContent(new Intent(this, TriggersListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabProfiles").setIndicator("Profiles")
				.setContent(new Intent(this, ProfilesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabSettings").setIndicator("Settings").setContent(new Intent(this, SettingsPreferenceActivity.class)));

	}
}
