package ch.amana.android.cputuner.view.fragments;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;

public abstract class PagerListFragment extends ListFragment implements PagerItem {



	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		return false;
	}

}