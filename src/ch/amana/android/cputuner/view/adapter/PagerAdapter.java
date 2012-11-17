package ch.amana.android.cputuner.view.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.fragments.CurInfoFragment;
import ch.amana.android.cputuner.view.fragments.LogAdvancedFragment;
import ch.amana.android.cputuner.view.fragments.LogFragment;
import ch.amana.android.cputuner.view.fragments.ProfilesListFragment;
import ch.amana.android.cputuner.view.fragments.StatsAdvancedFragment;
import ch.amana.android.cputuner.view.fragments.StatsFragment;
import ch.amana.android.cputuner.view.fragments.TriggersListFragment;
import ch.amana.android.cputuner.view.fragments.VirtualGovernorListFragment;

import com.markupartist.android.widget.ActionBar.Action;

public class PagerAdapter extends FragmentPagerAdapter {

	public interface PagerItem {

		List<Action> getActions();

	}

	private static final int FRAGMENT_CUR_INFO = 0;
	private static final int FRAGMENT_TRIGGER_LIST = 1;
	private static final int FRAGMENT_PROFILE_LIST = 2;
	private static final int FRAGMENT_VIRTGOV_LIST = 3;
	private static final int FRAGMENT_STAT = 4;
	private static final int FRAGMENT_LOG = 5;
	private final Context ctx;
	private final SettingsStorage settings;

	public PagerAdapter(Context ctx, FragmentManager fm) {
		super(fm);
		this.ctx = ctx.getApplicationContext();
		this.settings = SettingsStorage.getInstance(ctx);
	}

	//	} else {
	//		pagerAdapter.addPage(StatsFragment.class, R.string.labelStatisticsTab);
	//		if (settings.isEnableLogProfileSwitches()) {
	//			pagerAdapter.addPage(LogFragment.class, R.string.labelLogTab);
	//		}
	//	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case FRAGMENT_CUR_INFO:
			return new CurInfoFragment();
		case FRAGMENT_TRIGGER_LIST:
			return new TriggersListFragment();
		case FRAGMENT_PROFILE_LIST:
			return new ProfilesListFragment();
		case FRAGMENT_VIRTGOV_LIST:
			return new VirtualGovernorListFragment();
		case FRAGMENT_STAT:
			if (settings.isRunStatisticsService()) {
				if (settings.isAdvancesStatistics()) {
					return new StatsAdvancedFragment();
				}
				return new StatsFragment();
			}
		case FRAGMENT_LOG:
			if (settings.isEnableLogProfileSwitches()) {
				if (settings.isAdvancesStatistics()) {
					return new LogAdvancedFragment();
				}
			return new LogFragment();
			}
		}
		return new Fragment();
	}

	@Override
	public int getCount() {
		return 6;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case FRAGMENT_CUR_INFO:
			return ctx.getString(R.string.labelCurrentTab);
		case FRAGMENT_TRIGGER_LIST:
			return ctx.getString(R.string.labelTriggersTab);
		case FRAGMENT_PROFILE_LIST:
			return ctx.getString(R.string.labelProfilesTab);
		case FRAGMENT_VIRTGOV_LIST:
			return ctx.getString(R.string.virtualGovernorsList);
		case FRAGMENT_STAT:
			return ctx.getString(R.string.labelStatisticsTab);
		case FRAGMENT_LOG:
			return ctx.getString(R.string.labelLogTab);
		}
		return "";
	}
}