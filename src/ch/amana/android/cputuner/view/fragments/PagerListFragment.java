package ch.amana.android.cputuner.view.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;
import ch.amana.android.cputuner.view.widget.ActionBarWrapper;

import com.markupartist.android.widget.ActionBar.ActionList;

public abstract class PagerListFragment extends ListFragment implements PagerItem {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public ActionList getActions() {
		return null;
	}

	@Override
	public void pageIsActive(CpuTunerViewpagerActivity cpuTunerViewpagerActivity) {
		if (!SettingsStorage.getInstance(getActivity()).hasHoloTheme()) {
			ActionList actions = getActions();
			if (actions != null) {
				ActionBarWrapper actionBarWrapper = cpuTunerViewpagerActivity.getActionBarWrapper();
				actionBarWrapper.removeAllActions();
				actionBarWrapper.addActions(actions);
			}
		}

	}


}