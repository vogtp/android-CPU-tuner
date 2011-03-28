package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;

public class StatsActivity extends Activity {

	private TextView tvStats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);
		tvStats = (TextView) findViewById(R.id.tvStats);
	}

	@Override
	protected void onResume() {
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(sb);
		getTimeInState(sb);

		tvStats.setText(sb.toString());
		super.onResume();
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
			// if (sb.length() > 0) {
			// sb.append("--------------------------------------\n");
			// }
			sb.append(getString(R.string.label_time_in_state)).append("\n");
			String[] states = timeinstate.split("\n");
			for (int i = 0; i < states.length; i++) {
				String[] vals = states[i].split(" +");
				sb.append(Integer.parseInt(vals[0]) / 1000).append(" Mhz").append("\t\t").append(vals[1]).append("\n");
			}
			sb.append("\n");
		}
	}
}
