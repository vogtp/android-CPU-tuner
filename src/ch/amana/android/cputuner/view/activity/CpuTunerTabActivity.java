package ch.amana.android.cputuner.view.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class CpuTunerTabActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Logger.DEBUG) {
			setTitle(getTitle() + " - DEBUG MODE" + " (" + getString(R.string.version) + ")");
		}

		String lang = SettingsStorage.getInstance().getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(this, lang);
		}

		GuiUtils.setLanguage(this);

		InstallHelper.ensureSetup(this);

		final TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("tabCurrent").setIndicator(getString(R.string.labelCurrentTab), getResources().getDrawable(R.drawable.phone))
				.setContent(new Intent(this, CurInfo.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabTriggers").setIndicator(getString(R.string.labelTriggersTab), getResources().getDrawable(R.drawable.battery))
				.setContent(new Intent(this, TriggersListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabProfiles").setIndicator(getString(R.string.labelProfilesTab), getResources().getDrawable(R.drawable.cpu))
				.setContent(new Intent(this, ProfilesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabVirtGov").setIndicator(getString(R.string.virtualGovernorsList), getResources().getDrawable(R.drawable.virtgov))
				.setContent(new Intent(this, VirtualGovernorListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
		tabHost.addTab(tabHost.newTabSpec("tabStats").setIndicator(getString(R.string.labelStatisticsTab), getResources().getDrawable(R.drawable.stats))
				.setContent(new Intent(this, StatsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.gerneral_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GeneralMenuHelper.onOptionsItemSelected(this,item,null)) {
			return true;
		}
		return false;
	}
}
