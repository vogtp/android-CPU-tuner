package ch.amana.android.cputuner.view.fragments;

import java.util.List;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;

import com.markupartist.android.widget.ActionBar.Action;

public abstract class PagerListFragment extends ListFragment implements PagerItem {


	@Override
	public List<Action> getActions() {
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		return false;
	}

}