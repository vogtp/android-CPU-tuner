package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class LogFragment extends PagerFragment {

	private TextView tvStats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.log, container, false);
		tvStats = (TextView) v.findViewById(R.id.tvLog);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tvStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView();
			}
		});
	}

	@Override
	public void onResume() {
		updateView();
		super.onResume();
	}

	private void updateView() {
		tvStats.setText(Logger.getLog(getActivity()));
	}


	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				tvStats = (TextView) view.getRootView().findViewById(R.id.tvLog);
				updateView();
			}

			@Override
			public int getDrawable() {
				return android.R.drawable.ic_menu_revert;
			}
		});
		return actions;
	}

}
