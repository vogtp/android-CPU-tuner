package ch.amana.android.cputuner.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.adapter.PagerAdapter;
import ch.amana.android.cputuner.view.fragments.CurInfoFragment;
import ch.amana.android.cputuner.view.fragments.ProfilesListFragment;
import ch.amana.android.cputuner.view.fragments.StatsFragment;
import ch.amana.android.cputuner.view.fragments.TriggersListFragment;
import ch.amana.android.cputuner.view.fragments.VirtualGovernorListFragment;
import ch.amana.android.cputuner.view.widget.PagerHeader;

import com.markupartist.android.widget.ActionBar;

public class CpuTunerViewpagerActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.viewpager);

		ActionBar actionBar = (ActionBar) findViewById(R.id.abCpuTuner);

		if (Logger.DEBUG) {
			actionBar.setTitle(getString(R.string.app_name) + " - DEBUG MODE" + " (" + getString(R.string.version) + ")");
		}

		String lang = SettingsStorage.getInstance().getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(this, lang);
		}

		GuiUtils.setLanguage(this);

		InstallHelper.ensureConfiguration(this);

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		PagerAdapter pagerAdapter = new PagerAdapter(this,
				pager,
				(PagerHeader) findViewById(R.id.pager_header));

		pagerAdapter.addPage(CurInfoFragment.class, R.string.labelCurrentTab);
		pagerAdapter.addPage(TriggersListFragment.class, R.string.labelTriggersTab);
		pagerAdapter.addPage(ProfilesListFragment.class, R.string.labelProfilesTab);
		pagerAdapter.addPage(VirtualGovernorListFragment.class, R.string.virtualGovernorsList);
		pagerAdapter.addPage(StatsFragment.class, R.string.labelStatisticsTab);

		// FIXME handle virtual govs
	}

	public static Intent getStartIntent(Context ctx) {
		Intent intent = new Intent(ctx, CpuTunerViewpagerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
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
		if (GeneralMenuHelper.onOptionsItemSelected(this, item, null)) {
			return true;
		}
		return false;
	}

}
