package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsFragment extends PagerFragment {

	private TextView tvStats;
	private TableLayout tlSwitches;
	private LayoutInflater inflater;
	private final Map<Integer, TextView> graphs = new HashMap<Integer, TextView>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		this.inflater = inflater;
		View v = inflater.inflate(R.layout.stats, container, false);
		tvStats = (TextView) v.findViewById(R.id.tvStats);
		tlSwitches = (TableLayout) v.findViewById(R.id.tlSwitches);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView(getActivity());
			}
		};
		tvStats.setOnClickListener(clickListener);
		tlSwitches.setOnClickListener(clickListener);
	}

	@Override
	public void onResume() {
		updateView(getActivity());
		super.onResume();
	}

	private void updateView(Context context) {
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(context, sb);
		tvStats.setText(sb.toString());
		getTimeInState(context);
	}

	private void getTotalTransitions(Context context, StringBuilder sb) {
		String totaltransitions = CpuHandler.getInstance().getCpuTotalTransitions();
		if (!RootHandler.NOT_AVAILABLE.equals(totaltransitions)) {
			sb.append(context.getString(R.string.label_total_transitions)).append(" ").append(totaltransitions).append("\n");
			sb.append("\n");
		}
	}

	private void getTimeInState(Context context) {
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		tlSwitches.removeAllViews();
		if (!RootHandler.NOT_AVAILABLE.equals(timeinstate)) {
			String curCpuFreq = Integer.toString(CpuHandler.getInstance().getCurCpuFreq());
			graphs.clear();
			int max = 0;
			addTextToRow(0, "Frequency", context.getString(R.string.label_time_in_state), false);
			String[] states = timeinstate.split("\n");
			for (int i = 0; i < states.length; i++) {
				String[] vals = states[i].split(" +");
				int f = Integer.parseInt(vals[1]);
				max = Math.max(f, max);
				addTextToRow(f, String.format("%5d", Integer.parseInt(vals[0]) / 1000) + " Mhz", String.format("%13s", vals[1]), curCpuFreq.equals(vals[0]));
			}
			int width = context.getResources().getDisplayMetrics().widthPixels;
			for (Iterator<Integer> iterator = graphs.keySet().iterator(); iterator.hasNext();) {
				Integer curVal = iterator.next();
				TextView graph = graphs.get(curVal);

				int pixels = curVal * width / max;
				graph.setMaxWidth(pixels);
				graph.setWidth(pixels);
				graph.setMinimumWidth(pixels);
				graph.setMinWidth(pixels);
			}
		}
	}

	private void addTextToRow(int f, String frq, String time, boolean current) {
		if (inflater == null) {
			inflater = (LayoutInflater) tlSwitches.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		View v = inflater.inflate(R.layout.stats_table_row, tlSwitches, false);
		Resources resources = v.getContext().getResources();
		int color = resources.getColor(android.R.color.secondary_text_dark);
		if (current) {
			color = resources.getColor(R.color.cputuner_green);
		}
		TextView tvFreq = (TextView) v.findViewById(R.id.tvFreq);
		TextView tvTime = (TextView) v.findViewById(R.id.tvTime);
		TextView tvGraph = (TextView) v.findViewById(R.id.tvGraph);
		tvFreq.setText(frq);
		tvTime.setText(time);
		tvGraph.setText("");
		if (f > 0) {
			tvGraph.setBackgroundColor(color);
			graphs.put(f, tvGraph);
		}
		tvFreq.setTextColor(color);
		tvTime.setTextColor(color);

		tlSwitches.addView(v);

	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				tvStats = (TextView) view.getRootView().findViewById(R.id.tvStats);
				tlSwitches = (TableLayout) view.getRootView().findViewById(R.id.tlSwitches);
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
