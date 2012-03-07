package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateIndex;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateValue;
import ch.amana.android.cputuner.receiver.StatisticsReceiver;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.AdvStatsFilterAdaper;
import ch.amana.android.cputuner.view.widget.PercentGraphView;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsAdvancedFragment extends PagerListFragment implements LoaderCallbacks<Cursor>, StateChangeListener {

	private static final int LOADER_DATA = 0;
	private static final int LOADER_TIGGER = 1;
	private static final int LOADER_PROFILE = 2;
	private static final int LOADER_VIRTGOV = 3;

	private Spinner spTrigger;
	private Spinner spProfile;
	private Spinner spVirtGov;
	private SimpleCursorAdapter adapter;
	private int curCpuFreq;
	private String trigger = DB.SQL_WILDCARD;
	private String profile = DB.SQL_WILDCARD;
	private String virtgov = DB.SQL_WILDCARD;
	private double totalTime = 0;
	private ProgressBar pbWait;
	private TextView labelNoDataForFilter;
	private AdvStatsFilterAdaper profileAdapter;
	private AdvStatsFilterAdaper triggerAdapter;
	private AdvStatsFilterAdaper virtgovAdapter;
	// FIXME ugly way to get around destruction of fragments on orientation change
	private static StatsAdvancedFragment lastInstanceCreated;

	enum LoadingState {
		LOADING, HASDATA, NODATA
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.adv_stat_list, container, false);

		spTrigger = (Spinner) v.findViewById(R.id.spTrigger);
		spProfile = (Spinner) v.findViewById(R.id.spProfile);
		spVirtGov = (Spinner) v.findViewById(R.id.spVirtGov);
		pbWait = (ProgressBar) v.findViewById(R.id.pbWait);
		labelNoDataForFilter = (TextView) v.findViewById(R.id.labelNoDataForFilter);
		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.lastInstanceCreated = this;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(LOADER_DATA, null, this);
		getLoaderManager().initLoader(LOADER_TIGGER, null, this);
		getLoaderManager().initLoader(LOADER_PROFILE, null, this);
		getLoaderManager().initLoader(LOADER_VIRTGOV, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), R.layout.adv_stat_list_item, null,
				new String[] { TimeInStateValue.NAME_STATE, TimeInStateValue.NAME_TIME },
				new int[] { R.id.tvState, R.id.tvTime }, 0);

		setDataState(LoadingState.LOADING);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				View parent = (View) view.getParent().getParent();
				PercentGraphView percentGraphView = (PercentGraphView) parent.findViewById(R.id.percentGraphView1);
				if (columnIndex == TimeInStateValue.INDEX_STATE) {
					int state = cursor.getInt(TimeInStateValue.INDEX_STATE);
					TextView tv = ((TextView) view);
					if (state == 0) {
						tv.setText(R.string.deep_sleep);
						tv.setGravity(Gravity.LEFT);
					}else {
						tv.setText(Integer.toString(state / 1000) + " " + getString(R.string.mhz));
						tv.setGravity(Gravity.RIGHT);
					}
					percentGraphView.setHiglight(state == curCpuFreq);
					return true;
				} else
				if (columnIndex == TimeInStateValue.INDEX_TIME) {
					long time = cursor.getLong(TimeInStateValue.INDEX_TIME);
					float percent = (float) (time * 100f / totalTime);
					((TextView) ((View) view.getParent()).findViewById(R.id.tvPercent)).setText(String.format("%.2f", percent));
					percentGraphView.setPercent(percent);
					((TextView) view).setText(GuiUtils.milliesToString(time));
					//((TextView) view).setText(Long.toString(time));
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
		final Activity act = getActivity();

		profileAdapter = new AdvStatsFilterAdaper(act);
		triggerAdapter = new AdvStatsFilterAdaper(act);
		virtgovAdapter = new AdvStatsFilterAdaper(act);
		spProfile.setAdapter(profileAdapter);
		spTrigger.setAdapter(triggerAdapter);
		spVirtGov.setAdapter(virtgovAdapter);

		spProfile.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				profile = DB.SQL_WILDCARD;
				if (id > 0) {
					profile = ((TextView) view).getText().toString();
				}
				updateStatistics(act);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spTrigger.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				trigger = DB.SQL_WILDCARD;
				if (id > 0) {
					trigger = ((TextView) view).getText().toString();
				}
				updateStatistics(act);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spVirtGov.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				virtgov = DB.SQL_WILDCARD;
				if (id > 0) {
					virtgov = ((TextView) view).getText().toString();
				}
				updateStatistics(act);
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

	private void setDataState(LoadingState state) {
		if (!isVisible()) {
			return;
		}
		switch (state) {
		case LOADING:
			getListView().setVisibility(View.INVISIBLE);
			labelNoDataForFilter.setVisibility(View.INVISIBLE);
			pbWait.setVisibility(View.VISIBLE);
			break;
		case HASDATA:
			getListView().setVisibility(View.VISIBLE);
			labelNoDataForFilter.setVisibility(View.INVISIBLE);
			pbWait.setVisibility(View.INVISIBLE);
			break;
		case NODATA:
			getListView().setVisibility(View.INVISIBLE);
			labelNoDataForFilter.setVisibility(View.VISIBLE);
			pbWait.setVisibility(View.INVISIBLE);
			break;

		default:
			break;
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		updateStatistics(getActivity());
	}

	private void updateStatistics(Context context) {
		StatsAdvancedFragment self = this;
		if (getActivity() == null) {
			// FIXME ugly way to get around destruction of fragments on orientation change
			if (lastInstanceCreated == null) {
				return;
			}
			if (lastInstanceCreated.isDetached()) {
				FragmentManager fragmentManager = lastInstanceCreated.getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.attach(lastInstanceCreated);
				fragmentTransaction.commit();
			}
			self = lastInstanceCreated;
		}
		if (context != null) {
			context.sendBroadcast(new Intent(StatisticsReceiver.BROADCAST_UPDATE_TIMEINSTATE));
		}
		self.getLoaderManager().restartLoader(0, null, self);
		self.setDataState(LoadingState.LOADING);
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
		StatsAdvancedFragment self = this;
		if (getActivity() == null) {
			// FIXME ugly way to get around destruction of fragments on orientation change
			if (lastInstanceCreated == null) {
				return;
			}
			if (lastInstanceCreated.isDetached()) {
				FragmentManager fragmentManager = lastInstanceCreated.getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.attach(lastInstanceCreated);
				fragmentTransaction.commit();
			}
			self = lastInstanceCreated;
		}
		final Activity act = self.getActivity();
		if (act == null) {
			return;
		}
		Builder alertBuilder = new AlertDialog.Builder(act);
		alertBuilder.setTitle(R.string.title_reset_statistics);
		alertBuilder.setMessage(R.string.msg_reset_statistics);
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContentResolver resolver = ctx.getContentResolver();
				resolver.delete(DB.TimeInStateIndex.CONTENT_URI, null, null);
				resolver.delete(DB.TimeInStateValue.CONTENT_URI, null, null);
				updateStatistics(ctx);
				Handler h = new Handler();
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						updateStatistics(ctx);
					}
				}, 2000);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.advstats_option, menu);
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
		switch (id) {
		case LOADER_DATA:
			setDataState(LoadingState.LOADING);
			return new CursorLoader(getActivity(), TimeInStateValue.CONTENT_URI_GROUPED, null/*TimeInStateValue.PROJECTION_TIME_SUM*/,
					TimeInStateIndex.SELECTION_TRIGGER_PROFILE_VIRTGOV,
					new String[] { trigger, profile, virtgov }, TimeInStateValue.SORTORDER_DEFAULT);
		case LOADER_TIGGER:
			return new CursorLoader(getActivity(), TimeInStateIndex.CONTENT_URI_DISTINCT, TimeInStateIndex.PROJECTION_TRIGGER, null, null,
					TimeInStateIndex.SORTORDER_DEFAULT);
		case LOADER_PROFILE:
			return new CursorLoader(getActivity(), TimeInStateIndex.CONTENT_URI_DISTINCT, TimeInStateIndex.PROJECTION_PROFILE, null, null,
					TimeInStateIndex.SORTORDER_DEFAULT);
		case LOADER_VIRTGOV:
			return new CursorLoader(getActivity(), TimeInStateIndex.CONTENT_URI_DISTINCT, TimeInStateIndex.PROJECTION_VIRTGOV, null, null,
					TimeInStateIndex.SORTORDER_DEFAULT);

		default:
			throw new RuntimeException("No valid cursor loader");
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		switch (loader.getId()) {
		case LOADER_DATA:
			curCpuFreq = CpuHandler.getInstance().getCurCpuFreq();
			totalTime = 0;
			while (c.moveToNext()) {
				totalTime += c.getLong(TimeInStateValue.INDEX_TIME);
			}
			setDataState(totalTime > 0 ? LoadingState.HASDATA : LoadingState.NODATA);
			adapter.swapCursor(c);
			getListView().setVisibility(View.VISIBLE);
			pbWait.setVisibility(View.INVISIBLE);
			break;
		case LOADER_TIGGER:
			triggerAdapter.setCursor(c);
			break;
		case LOADER_PROFILE:
			profileAdapter.setCursor(c);
			break;
		case LOADER_VIRTGOV:
			virtgovAdapter.setCursor(c);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_DATA:
			totalTime = 0;
			setDataState(LoadingState.LOADING);
			adapter.swapCursor(null);
			break;
		case LOADER_TIGGER:
			triggerAdapter.setCursor(null);
			break;
		case LOADER_PROFILE:
			profileAdapter.setCursor(null);
			break;
		case LOADER_VIRTGOV:
			virtgovAdapter.setCursor(null);
			break;
		}
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
