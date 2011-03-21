package ch.amana.android.cputuner.view.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.view.preference.SettingsPreferenceActivity;

public class CpuTunerTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Logger.DEBUG) {
			setTitle(getTitle() + " - DEBUG MODE");
		}
		setTitle(getTitle() + " (" + getString(R.string.version) + ")");

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tabCurrent").setIndicator(getString(R.string.labelCurrentTab), getResources().getDrawable(R.drawable.phone))
				.setContent(new Intent(this, CurInfo.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabTriggers").setIndicator(getString(R.string.labelTriggersTab), getResources().getDrawable(R.drawable.battery))
				.setContent(new Intent(this, TriggersListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabProfiles").setIndicator(getString(R.string.labelProfilesTab), getResources().getDrawable(R.drawable.cpu))
				.setContent(new Intent(this, ProfilesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabHelp").setIndicator(getString(R.string.labelHelpTab), getResources().getDrawable(R.drawable.help))
					.setContent(new Intent(this, HelpActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tabSettings").setIndicator(getString(R.string.labelSettingsTab), getResources().getDrawable(R.drawable.configure))
				.setContent(new Intent(this, SettingsPreferenceActivity.class)));

	}

	@Override
	public void onContentChanged() {
		// TODO Auto-generated method stub
		super.onContentChanged();
	}

}
