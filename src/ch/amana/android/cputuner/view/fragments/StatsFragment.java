package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsFragment extends PagerFragment {

	private TextView tvStats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.stats, container, false);
		tvStats = (TextView) v.findViewById(R.id.tvStats);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tvStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView(getActivity());
			}
		});
	}

	@Override
	public void onResume() {
		updateView(getActivity());
		super.onResume();
	}

	private void updateView(Context context) {
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(context, sb);
		getTimeInState(context, sb);
		tvStats.setText(sb.toString());
	}

	private void getTotalTransitions(Context context, StringBuilder sb) {
		String totaltransitions = CpuHandler.getInstance().getCpuTotalTransitions();
		if (!RootHandler.NOT_AVAILABLE.equals(totaltransitions)) {
			sb.append(context.getString(R.string.label_total_transitions)).append(" ").append(totaltransitions).append("\n");
			sb.append("\n");
		}
	}

	private void getTimeInState(Context context, StringBuilder sb) {
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		if (!RootHandler.NOT_AVAILABLE.equals(timeinstate)) {
			String curCpuFreq = Integer.toString(CpuHandler.getInstance().getCurCpuFreq());
			sb.append(context.getString(R.string.label_time_in_state)).append("\n");
			String[] states = timeinstate.split("\n");
			for (int i = 0; i < states.length; i++) {
				String[] vals = states[i].split(" +");
				sb.append(String.format("%5d", Integer.parseInt(vals[0]) / 1000));
				sb.append(" Mhz");
				sb.append(String.format("%13s", vals[1]));
				if (curCpuFreq.equals(vals[0])) {
					sb.append("\t\t(").append(context.getString(R.string.labelCurrentFrequency)).append(")");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				tvStats = (TextView) view.getRootView().findViewById(R.id.tvStats);
				updateView(view.getContext());
			}
			@Override
			public int getDrawable() {
				return android.R.drawable.ic_menu_revert;
			}
		});
		return actions;
	}

}
