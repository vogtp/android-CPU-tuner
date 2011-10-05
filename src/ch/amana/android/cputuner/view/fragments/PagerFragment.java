package ch.amana.android.cputuner.view.fragments;

import java.util.List;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;

import com.markupartist.android.widget.ActionBar.Action;

public class PagerFragment extends Fragment implements PagerItem {

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		return false;
	}

	@Override
	public List<Action> getActions() {
		return null;
	}

}