package ch.amana.android.cputuner.view.activity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
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
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.adapter.PagerAdapter;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;
import ch.amana.android.cputuner.view.fragments.CurInfoFragment;
import ch.amana.android.cputuner.view.fragments.LogFragment;
import ch.amana.android.cputuner.view.fragments.ProfilesListFragment;
import ch.amana.android.cputuner.view.fragments.StatsAdvancedFragment;
import ch.amana.android.cputuner.view.fragments.StatsFragment;
import ch.amana.android.cputuner.view.fragments.TriggersListFragment;
import ch.amana.android.cputuner.view.fragments.VirtualGovernorListFragment;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;
import ch.amana.android.cputuner.view.widget.PagerHeader;

import com.markupartist.android.widget.ActionBar;

public class CpuTunerViewpagerActivity extends FragmentActivity {

	private boolean doCheckConfig = true;
	private PagerAdapter pagerAdapter;

	private static final int[] lock = new int[1];
	private CpuTunerReceiver receiver;
	private final Set<StateChangeListener> stateChangeListeners = new HashSet<StateChangeListener>();

	//	private BillingManager billingManager;

	public interface StateChangeListener {

		void profileChanged();

		void deviceStatusChanged();

		void triggerChanged();

	}

	protected class CpuTunerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			deviceStatusChanged();
			if (Notifier.BROADCAST_TRIGGER_CHANGED.equals(action)) {
				triggerChanged();
			}
			if (Notifier.BROADCAST_PROFILE_CHANGED.equals(action)) {
				profileChanged();
			}

		}
	}

	public void registerReceiver() {
		synchronized (lock) {
			IntentFilter deviceStatusFilter = new IntentFilter(Notifier.BROADCAST_DEVICESTATUS_CHANGED);
			IntentFilter triggerFilter = new IntentFilter(Notifier.BROADCAST_TRIGGER_CHANGED);
			IntentFilter profileFilter = new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED);
			receiver = new CpuTunerReceiver();
			registerReceiver(receiver, deviceStatusFilter);
			registerReceiver(receiver, triggerFilter);
			registerReceiver(receiver, profileFilter);
			Logger.i("Registered CpuTunerReceiver");
		}
	}

	public void unregisterReceiver() {
		synchronized (lock) {
			if (receiver != null) {
				try {
					unregisterReceiver(receiver);
					receiver = null;
				} catch (Throwable e) {
					Logger.w("Could not unregister BatteryReceiver", e);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);


		SettingsStorage settings = SettingsStorage.getInstance();
		String lang = settings.getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(this, lang);
		}
		GuiUtils.setLanguage(this);

		if (!sanityChecks(settings)) {
			return;
		}

		setContentView(R.layout.viewpager);

		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setTitle(R.string.app_name);
		actionBar.setHomeLogo(R.drawable.icon);
		actionBar.setHomeTitleAction(new ActionBar.IntentAction(this, CpuTunerViewpagerActivity.getStartIntent(this), R.drawable.icon));

		if (Logger.DEBUG) {
			actionBar.setTitle(getString(R.string.app_name) + " - DEBUG MODE" + " (" + getString(R.string.version) + ")");
		}

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new PagerAdapter(this, pager, (PagerHeader) findViewById(R.id.pager_header), actionBar);
		pagerAdapter.addPage(CurInfoFragment.class, R.string.labelCurrentTab);
		pagerAdapter.addPage(TriggersListFragment.class, R.string.labelTriggersTab);
		pagerAdapter.addPage(ProfilesListFragment.class, R.string.labelProfilesTab);
		if (settings.isUseVirtualGovernors()) {
			pagerAdapter.addPage(VirtualGovernorListFragment.class, R.string.virtualGovernorsList);
		}
		if (settings.isAdvancesStatistics()) {
			pagerAdapter.addPage(StatsAdvancedFragment.class, R.string.labelStatisticsTab);
		} else {
			pagerAdapter.addPage(StatsFragment.class, R.string.labelStatisticsTab);
		}
		if (settings.getProfileSwitchLogSize() > 0) {
			pagerAdapter.addPage(LogFragment.class, R.string.labelLogTab);
		}
	}

	private boolean sanityChecks(SettingsStorage settings) {
		if (settings.isFirstRun() && !InstallHelper.hasConfig(this)) {
			startActivity(new Intent(getApplicationContext(), FirstRunActivity.class));
			finish();
			return false;
		} else {
			if (doCheckConfig && !InstallHelper.hasConfig(this)) {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
				alertBuilder.setTitle(R.string.title_no_configuration);
				alertBuilder.setMessage(R.string.label_no_configuration);
				alertBuilder.setPositiveButton(R.string.load, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						InstallHelper.ensureConfiguration(CpuTunerViewpagerActivity.this, false);
					}
				});
				alertBuilder.setNegativeButton(R.string.cont, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						doCheckConfig = false;
					}
				});
				AlertDialog alert = alertBuilder.create();
				alert.show();
			}
		}
		if (!SettingsStorage.getInstance().isUserLevelSet()) {
			UserExperianceLevelChooser uec = new UserExperianceLevelChooser(this, false);
			uec.show();
		}
		return true;
	}

	public static Intent getStartIntent(Context ctx) {
		Intent intent = new Intent(ctx, CpuTunerViewpagerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//		return pagerAdapter.onCreateOptionsMenu(menu);
		//		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		//		getMenuInflater().inflate(R.menu.gerneral_options_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//		return pagerAdapter.onPrepareOptionsMenu(menu);
		menu.clear();
		PagerAdapter.getCurrentItem().onCreateOptionsMenu(menu, getMenuInflater());
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.gerneral_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (((PagerItem) PagerAdapter.getCurrentItem()).onOptionsItemSelected(this, item)) {
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(this, item, null)) {
			return true;
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		PagerAdapter.getCurrentItem().onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		unregisterReceiver();
		super.onPause();
	}

	public void addStateChangeListener(StateChangeListener listener) {
		stateChangeListeners.add(listener);
	}

	public void removeStateChangeListener(StateChangeListener listener) {
		stateChangeListeners.remove(listener);
	}

	private void profileChanged() {
		for (Iterator<StateChangeListener> iterator = stateChangeListeners.iterator(); iterator.hasNext();) {
			try {
				iterator.next().profileChanged();
			} catch (Exception e) {
			}
		}
	}

	private void triggerChanged() {
		for (Iterator<StateChangeListener> iterator = stateChangeListeners.iterator(); iterator.hasNext();) {
			try {
				iterator.next().triggerChanged();
			} catch (Exception e) {
			}
		}
	}

	private void deviceStatusChanged() {
		for (Iterator<StateChangeListener> iterator = stateChangeListeners.iterator(); iterator.hasNext();) {
			try {
				iterator.next().deviceStatusChanged();
			} catch (Exception e) {
			}
		}
	}

}
