package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateValue;
import ch.amana.android.cputuner.provider.loader.TimeinstateCursorLoader;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.AdvStatsFilterAdaper;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsAdvancedFragment2 extends PagerListFragment implements LoaderCallbacks<Cursor>, StateChangeListener {

	private TimeinstateCursorLoader tisCursorLoader;
	private Spinner spTrigger;
	private Spinner spProfile;
	private Spinner spVirtGov;
	private SimpleCursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.adv_stat_list, container, false);

		spTrigger = (Spinner) v.findViewById(R.id.spTrigger);
		spProfile = (Spinner) v.findViewById(R.id.spProfile);
		spVirtGov = (Spinner) v.findViewById(R.id.spVirtGov);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[] { TimeInStateValue.NAME_STATE },
				new int[] { android.R.id.text1 });

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == TimeInStateValue.INDEX_STATE) {
					StringBuilder sb = new StringBuilder();
					int state = cursor.getInt(TimeInStateValue.INDEX_STATE);
					long time = cursor.getLong(TimeInStateValue.INDEX_TIME);
					sb.append(state / 1000).append(" MHz ");
					sb.append(Long.toString(time)).append(" ms ");
					double totalTime = tisCursorLoader.getTotalTime();
					sb.append(String.format("%.2f", time * 100 / totalTime)).append("%");
					((TextView) view).setText(sb.toString());
				}
				return true;
			}
		});

		setListAdapter(adapter);
		final Activity act = getActivity();

		spProfile.setAdapter(new AdvStatsFilterAdaper(act, DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, DB.CpuProfile.SORTORDER_DEFAULT));
		spTrigger.setAdapter(new AdvStatsFilterAdaper(act, DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_ID_NAME, DB.Trigger.SORTORDER_DEFAULT));
		spVirtGov.setAdapter(new AdvStatsFilterAdaper(act, DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_ID_NAME, DB.VirtualGovernor.SORTORDER_DEFAULT));

		spProfile.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selection = DB.SQL_WILDCARD;
				if (id != AdvStatsFilterAdaper.ALL_ID) {
					selection = ModelAccess.getInstace(getActivity()).getProfileName(id);
				}
				tisCursorLoader.setProfile(selection);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spTrigger.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selection = DB.SQL_WILDCARD;
				if (id != AdvStatsFilterAdaper.ALL_ID) {
					selection = ModelAccess.getInstace(getActivity()).getTrigger(id).getName();
				}
				tisCursorLoader.setTrigger(selection);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spVirtGov.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selection = DB.SQL_WILDCARD;
				if (id != AdvStatsFilterAdaper.ALL_ID) {
					selection = ModelAccess.getInstace(getActivity()).getVirtualGovernor(id).getVirtualGovernorName();
				}
				tisCursorLoader.setVirtGov(selection);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				updateStatistics(act);
			}
		});

		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
	}

	@Override
	public void onDestroy() {
		tisCursorLoader.stopLoading();
		super.onDestroy();
	}

	private void updateStatistics(Context context) {
		//		context.sendBroadcast(new Intent(StatisticsReceiver.BROADCAST_UPDATE_TIMEINSTATE));
		getLoaderManager().restartLoader(0, null, this);
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
				updateStatistics(view.getContext());
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
				ContentResolver resolver = ctx.getContentResolver();
				resolver.delete(DB.TimeInStateInput.CONTENT_URI, null, null);
				resolver.delete(DB.TimeInStateIndex.CONTENT_URI, null, null);
				resolver.delete(DB.TimeInStateValue.CONTENT_URI, null, null);
				//				SettingsStorage.getInstance().setTimeinstateBaseline(CpuHandler.getInstance().getCpuTimeinstate());
				updateStatistics(ctx);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
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
			updateStatistics(act);
			return true;

		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_INDEX)) {
			return true;
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		tisCursorLoader = new TimeinstateCursorLoader(getActivity(), DB.SQL_WILDCARD, DB.SQL_WILDCARD, DB.SQL_WILDCARD);
		return tisCursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	@Override
	public void profileChanged() {
		updateStatistics(getActivity());
	}

	@Override
	public void deviceStatusChanged() {
	}

	@Override
	public void triggerChanged() {
	}
}
