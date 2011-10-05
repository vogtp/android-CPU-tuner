package ch.amana.android.cputuner.view.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;

public class PagerListFragment extends ListFragment implements PagerItem {


	private Fragment currentPage;

	public Fragment getCurrentPage() {
		return currentPage;
	}

	@Override
	public void setCurrentPage(Fragment f) {
		currentPage = f;
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		return false;
	}

}