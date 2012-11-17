package ch.amana.android.cputuner.view.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import ch.amana.android.cputuner.view.adapter.PagerAdapter.PagerItem;

import com.markupartist.android.widget.ActionBar.Action;

public abstract class PagerListFragment extends ListFragment implements PagerItem {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public List<Action> getActions() {
		return null;
	}

}