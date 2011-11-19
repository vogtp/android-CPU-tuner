package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.view.activity.HelpActivity;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsAdvancedFragment extends PagerFragment {

	private static final long NO_TRANSITIONS = Long.MAX_VALUE;
	private TextView tvStats;
	private TableLayout tlSwitches;
	private LayoutInflater inflater;
	private long totaltransitionsBaseline;
	private String timeinstateBaseline;

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
		SettingsStorage settings = SettingsStorage.getInstance();
		totaltransitionsBaseline = settings.getTotaltransitionsBaseline();
		if (totaltransitionsBaseline == 0 || getTotalTransitions() < 0) {
			// create firsttime baseline
			createBaseline(getActivity());
			totaltransitionsBaseline = settings.getTotaltransitionsBaseline();
		}
		timeinstateBaseline = settings.getTimeinstateBaseline();
	}

	@Override
	public void onResume() {
		updateView(getActivity());
		super.onResume();
	}

	private void updateView(Context context) {
		if (tvStats == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(context, sb);
		sb.append(context.getString(R.string.label_time_in_state));
		tvStats.setText(sb.toString());
		getTimeInState(context);
	}

	private long getTotalTransitions() {
		String totaltransitionsStr = CpuHandler.getInstance().getCpuTotalTransitions();
		if (!RootHandler.NOT_AVAILABLE.equals(totaltransitionsStr)) {
			try {
				long totaltransitions = Long.parseLong(totaltransitionsStr) - totaltransitionsBaseline;
				return totaltransitions;
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse cpu total transitions", e);
			}

		}
		return NO_TRANSITIONS;
	}

	private void getTotalTransitions(Context context, StringBuilder sb) {
		long totaltransitions = getTotalTransitions();
		if (totaltransitions != NO_TRANSITIONS) {
			sb.append(context.getString(R.string.label_total_transitions)).append(" ").append(totaltransitions).append("\n");
			sb.append("\n");
		}
	}

	class TimeInStateParser {

		private long maxTime = Long.MIN_VALUE;
		private final Map<Integer, Long> states = new TreeMap<Integer, Long>();
		private TimeInStateParser baseline = null;
		private boolean parseOk = false;

		public TimeInStateParser(String timeinstate) {
			try {
				String[] lines = timeinstate.split("\n");
				for (int i = 0; i < lines.length; i++) {
					String[] vals = lines[i].split(" +");
					int freq = Integer.parseInt(vals[0]);
					long time = Long.parseLong(vals[1]);
					states.put(freq, time);
					maxTime = Math.max(time, maxTime);
				}
				parseOk = lines.length == states.size();
			} catch (Exception e) {
				Logger.w("cannot parse timeinstate");
			}
		}

		public Set<Integer> getStates() {
			return states.keySet();
		}

		public String getFreqFromated(int f) {
			return String.format("%d MHz", f / 1000);
		}

		public long getTime(int f) {
			Long time = states.get(f);
			if (baseline != null) {
				time = time - baseline.states.get(f);
			}
			if (time < 0) {
				time = 0l;

			}
			return time;
		}

		public String getTimeFromated(int f) {
			return Long.toString(getTime(f)) + "s";
		}

		public void setBaseline(TimeInStateParser bl) {
			if (parseOk && states.size() == bl.states.size()) {
				this.baseline = bl;
			}
		}

		public long getMaxTime() {
			if (baseline != null) {
				return maxTime - baseline.maxTime;
			}
			return maxTime;
		}

	}

	private void getTimeInState(Context context) {
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		tlSwitches.removeAllViews();
		if (!RootHandler.NOT_AVAILABLE.equals(timeinstate)) {
			int curCpuFreq = CpuHandler.getInstance().getCurCpuFreq();
			addTextToRow(0, context.getString(R.string.label_frequency), context.getString(R.string.label_time), false);

			TimeInStateParser tisParser = new TimeInStateParser(timeinstate);
			tisParser.setBaseline(new TimeInStateParser(timeinstateBaseline));
			int width = context.getResources().getDisplayMetrics().widthPixels;
			long max = tisParser.getMaxTime();
			for (Integer freq : tisParser.getStates()) {
				TextView graph = addTextToRow(freq, tisParser.getFreqFromated(freq), tisParser.getTimeFromated(freq), curCpuFreq == freq);
				if (graph != null) {
					long pixelsl = 0;
					if (max != 0) {
						pixelsl = tisParser.getTime(freq) * width / max;
					}
					int pixels = width;
					if (pixelsl <= Integer.MAX_VALUE) {
						pixels = (int) pixelsl;
					}
					graph.setMaxWidth(pixels);
					graph.setWidth(pixels);
					graph.setMinimumWidth(pixels);
					graph.setMinWidth(pixels);
				}
			}
		}
	}

	private TextView addTextToRow(long f, String frq, String time, boolean current) {
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
		tvFreq.setTextColor(color);
		tvTime.setTextColor(color);

		tlSwitches.addView(v);

		if (f > 0) {
			tvGraph.setBackgroundColor(color);
			return tvGraph;
		}
		return null;
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				resetStatistics(view.getContext());
			}

			@Override
			public int getDrawable() {
				return android.R.drawable.ic_input_get;
			}
		});
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				tvStats = (TextView) view.getRootView().findViewById(R.id.tvStats);
				tlSwitches = (TableLayout) view.getRootView().findViewById(R.id.tlSwitches);
				updateView(view.getContext());
			}

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}
		});
		return actions;
	}

	private void resetStatistics(final Context ctx) {
		final Activity act = getActivity();
		Builder alertBuilder = new AlertDialog.Builder(act);
		alertBuilder.setTitle(R.string.title_reset_statistics);
		alertBuilder.setMessage(R.string.msg_reset_statistics);
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				createBaseline(ctx);
				updateView(ctx);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void createBaseline(Context ctx) {
		try {
			totaltransitionsBaseline = Long.parseLong(CpuHandler.getInstance().getCpuTotalTransitions());
		} catch (NumberFormatException e) {
			Logger.w("Cannot parse cpu transitions as long ", e);
			totaltransitionsBaseline = -1;
		}
		timeinstateBaseline = CpuHandler.getInstance().getCpuTimeinstate();
		SettingsStorage.getInstance().setTimeinstateBaseline(timeinstateBaseline);
		SettingsStorage.getInstance().setTotaltransitionsBaseline(totaltransitionsBaseline);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.advstats_option, menu);
		inflater.inflate(R.menu.refresh_option, menu);
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemBaseline:
			resetStatistics(act);
			return true;

		case R.id.itemRefresh:
			updateView(act);
			return true;

		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_INDEX)) {
			return true;
		}
		return false;
	}
}
