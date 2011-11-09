package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.HelpActivity;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsFragment extends PagerFragment {

	private TextView tvStats;
	private TableLayout tlSwitches;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment 
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
		if (tvStats == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		getTotalTransitions(context, sb);
		getTimeInState(sb);
		tvStats.setText(sb.toString());
	}

	private void getTotalTransitions(Context context, StringBuilder sb) {
		String totaltransitions = CpuHandler.getInstance().getCpuTotalTransitions();
		if (!RootHandler.NOT_AVAILABLE.equals(totaltransitions)) {
			sb.append(context.getString(R.string.label_total_transitions)).append(" ").append(totaltransitions).append("\n");
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

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(2);
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (SettingsStorage.getInstance().allowExtentions()) {
			inflater.inflate(R.menu.upgrade_option, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemUpgrade:
			Intent i = new Intent(act, BillingProductListActiviy.class);
			i.putExtra(BillingProductListActiviy.EXTRA_TITLE, act.getString(R.string.title_extentions));
			i.putExtra(BillingProductListActiviy.EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_EXTENTIONS);
			act.startActivity(i);
			return true;

		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_INDEX)) {
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

}
