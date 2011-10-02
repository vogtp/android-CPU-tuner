package ch.amana.android.cputuner.view.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;

public class StatsFragment extends Fragment {

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
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(sb);
		getTimeInState(sb);
		getProfileSwitches(sb);

		tvStats.setText(sb.toString());
	}

	private void getProfileSwitches(StringBuilder sb) {
		sb.append(getString(R.string.label_profile_switches)).append("\n");
		sb.append(Logger.getLog(getActivity()));

	}

	private void getTotalTransitions(StringBuilder sb) {
		String totaltransitions = CpuHandler.getInstance().getCpuTotalTransitions();
		if (!RootHandler.NOT_AVAILABLE.equals(totaltransitions)) {
			// if (sb.length() > 0) {
			// sb.append("--------------------------------------\n\n");
			// }
			sb.append(getString(R.string.label_total_transitions)).append(" ").append(totaltransitions).append("\n");

			sb.append("\n");
		}
	}

	private void getTimeInState(StringBuilder sb) {
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		if (!RootHandler.NOT_AVAILABLE.equals(timeinstate)) {

			String curCpuFreq = Integer.toString(CpuHandler.getInstance().getCurCpuFreq());
			sb.append(getString(R.string.label_time_in_state)).append("\n");
			String[] states = timeinstate.split("\n");
			for (int i = 0; i < states.length; i++) {
				String[] vals = states[i].split(" +");
				sb.append(String.format("%5d", Integer.parseInt(vals[0]) / 1000));
				sb.append(" Mhz");
				sb.append(String.format("%13s", vals[1]));
				if (curCpuFreq.equals(vals[0])) {
					sb.append("\t\t(").append(getString(R.string.labelCurrentFrequency)).append(")");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
	}
}
