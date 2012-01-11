package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateIndex;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateInput;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateValue;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.AdvStatsFilterAdaper;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatsAdvancedFragment2 extends PagerListFragment implements OnLoadCompleteListener<Cursor> {

	private TisCursorLoader tisCursorLoader;
	private double totalTime = 0;
	private Spinner spTrigger;
	private Spinner spProfile;
	private Spinner spVirtGov;

	private class TimeInStateParser {

		private final Map<Integer, Long> states = new TreeMap<Integer, Long>();
		private TimeInStateParser baseline = null;
		private boolean parseOk = false;

		public TimeInStateParser(String start, String end) {
			this(end);
			setBaseline(new TimeInStateParser(start));
		}

		public TimeInStateParser(String timeinstate) {
			try {
				String[] lines = timeinstate.split("\n");
				for (int i = 0; i < lines.length; i++) {
					String[] vals = lines[i].split(" +");
					int freq = Integer.parseInt(vals[0]);
					long time = Long.parseLong(vals[1]);
					states.put(freq, time);
				}
				parseOk = lines.length == states.size();
			} catch (Exception e) {
				Logger.w("cannot parse timeinstate");
			}
		}

		public Set<Integer> getStates() {
			return states.keySet();
		}

		public long getTime(int f) {
			Long time = states.get(f);
			if (baseline != null && baseline.states != null) {
				time = time - baseline.states.get(f);
			}
			if (time < 0) {
				time = 0l;

			}
			return time;
		}

		public void setBaseline(TimeInStateParser bl) {
			if (parseOk && states.size() == bl.states.size()) {
				this.baseline = bl;
			}
		}

	}

	private class TisCursorLoader extends CursorLoader {
		private String triggerName = null;
		private String profileName = null;
		private String virtgovName = null;
		private long indexID = -1;
		private final ContentResolver resolver;
		private final Context context;

		public TisCursorLoader(Context context) {
			super(context);
			this.context = context;
			this.resolver = getContext().getContentResolver();
		}

		public TisCursorLoader(Context context, String triggerName, String profileName, String virtgovName) {
			super(context, DB.TimeInStateInput.CONTENT_URI, DB.TimeInStateInput.PROJECTION_DEFAULT, DB.TimeInStateInput.SELECTION_FINISHED_BY_TRIGGER_PROFILE_VIRTGOV,
					new String[] { triggerName, profileName, virtgovName },
					DB.TimeInStateInput.SORTORDER_DEFAULT);
			this.context = context;
			this.triggerName = triggerName;
			this.profileName = profileName;
			this.virtgovName = virtgovName;
			this.resolver = getContext().getContentResolver();
		}

		@Override
		protected void onStartLoading() {
			doResult();
			super.onStartLoading();
		}

		@Override
		public Cursor loadInBackground() {
			//			doResult();
			processInput(triggerName, profileName, virtgovName);
			//			doResult();
			//			processInput("%", "%", "%");
			return doResult();
		}

		private Cursor doResult() {


			//			SQLiteDatabase db = new CpuTunerOpenHelper(context).getReadableDatabase();
			//
			//			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			//			qb.setTables(TimeInStateValue.TABLE_NAME + ", " + TimeInStateIndex.TABLE_NAME);
			//			qb.appendWhere("TimeInStateValue.tisIndex=TimeInStateIndex._id");
			//			//			qb.appendWhere(" AND ");
			//			//			qb.appendWhere(TimeInStateIndex.NAME_TRIGGER + "=?");
			//			//			qb.appendWhere(" AND ");
			//			//			qb.appendWhere(TimeInStateIndex.NAME_PROFILE + "=?");
			//			//			qb.appendWhere(" AND ");
			//			//			qb.appendWhere(TimeInStateIndex.NAME_VIRTGOV + "=?");
			//			// select tisIndex, state, total(TimeInStateValue.time) as time from TimeInStateValue, TimeInStateIndex where (TimeInStateValue.tisIndex=TimeInStateIndex._id) and (TimeInStateIndex.trigger = 'All')  group by state;
			//			String sql = qb.buildQuery(TimeInStateValue.PROJECTION_TIME_SUM, null, TimeInStateValue.NAME_STATE, null, TimeInStateValue.SORTORDER_DEFAULT, null);
			//			Cursor resultCursor = qb.query(db, TimeInStateValue.PROJECTION_TIME_SUM, TimeInStateIndex.SELECTION_TRIGGER_PROFILE_VIRTGOV, new String[] { triggerName, profileName,
			//					virtgovName }, TimeInStateValue.NAME_STATE, null, TimeInStateValue.SORTORDER_DEFAULT);
			//			//			Cursor resultCursor = db.rawQuery("select _id, tisIndex, state, total(time) as time from TimeInStateValue group by state", selectionAgrs);

			Cursor resultCursor = resolver.query(TimeInStateValue.CONTENT_URI_GROUPED, null, TimeInStateIndex.SELECTION_TRIGGER_PROFILE_VIRTGOV,
					new String[] { triggerName, profileName, virtgovName }, TimeInStateValue.SORTORDER_DEFAULT);
			long tt = 0;
			while (resultCursor.moveToNext()) {
				tt += resultCursor.getLong(TimeInStateValue.INDEX_TIME);
			}
			totalTime = tt;
			//			deliverResult(resultCursor);
			return resultCursor;
		}

		private void processInput(String tn, String pn, String vgn) {
			Cursor inputCursor = resolver.query(DB.TimeInStateInput.CONTENT_URI, DB.TimeInStateInput.PROJECTION_DEFAULT, DB.TimeInStateInput.SELECTION_FINISHED,
					null, DB.TimeInStateInput.SORTORDER_DEFAULT);
			while (inputCursor.moveToNext()) {
				updateValues(inputCursor);
			}
			if (inputCursor != null) {
				inputCursor.close();
			}
			resolver.delete(DB.TimeInStateInput.CONTENT_URI, DB.TimeInStateInput.SELECTION_FINISHED_BY_TRIGGER_PROFILE_VIRTGOV,
					new String[] { triggerName, profileName, virtgovName });
		}

		private void updateValues(Cursor input) {
			ContentValues values = new ContentValues();
			values.put(TimeInStateIndex.NAME_TRIGGER, input.getString(TimeInStateInput.INDEX_TRIGGER));
			values.put(TimeInStateIndex.NAME_PROFILE, input.getString(TimeInStateInput.INDEX_PROFILE));
			values.put(TimeInStateIndex.NAME_VIRTGOV, input.getString(TimeInStateInput.INDEX_VIRTGOV));
			Uri uri = resolver.insert(TimeInStateIndex.CONTENT_URI, values);
			indexID = ContentUris.parseId(uri);
			String start = input.getString(TimeInStateInput.INDEX_TIS_START);
			String end = input.getString(TimeInStateInput.INDEX_TIS_END);
			TimeInStateParser tisParser = new TimeInStateParser(start, end);
			String idStr = Long.toString(indexID);
			for (Integer state : tisParser.getStates()) {
				Long time = tisParser.getTime(state);
				String[] selection = new String[] { idStr, Integer.toString(state) };
				Cursor c = resolver.query(TimeInStateValue.CONTENT_URI, TimeInStateValue.PROJECTION_DEFAULT, TimeInStateValue.SELECTION_BY_ID_STATE, selection,
						TimeInStateValue.SORTORDER_DEFAULT);
				if (c.moveToFirst()) {
					time += c.getLong(TimeInStateValue.INDEX_TIME);
					values = new ContentValues();
					values.put(TimeInStateValue.NAME_TIME, time);
					resolver.update(TimeInStateValue.CONTENT_URI, values, TimeInStateValue.SELECTION_BY_ID_STATE, selection);
				} else {
					values = new ContentValues();
					values.put(TimeInStateValue.NAME_IDX, indexID);
					values.put(TimeInStateValue.NAME_STATE, state);
					values.put(TimeInStateValue.NAME_TIME, time);
					resolver.insert(TimeInStateValue.CONTENT_URI, values);
				}

				if (c != null) {
					c.close();
				}
			}
		}

		public void setProfile(String selection) {
			if (selection == null || selection.equals(profileName)) {
				return;
			}
			profileName = selection;
			updateInputSelection();
		}

		public void setTrigger(String selection) {
			if (selection == null || selection.equals(triggerName)) {
				return;
			}
			triggerName = selection;
			updateInputSelection();
		}

		public void setVirtGov(String selection) {
			if (selection == null || selection.equals(virtgovName)) {
				return;
			}
			virtgovName = selection;
			updateInputSelection();
		}

		private void updateInputSelection() {
			setSelection(TimeInStateInput.SELECTION_FINISHED_BY_TRIGGER_PROFILE_VIRTGOV);
			setSelectionArgs(new String[] { triggerName, profileName, virtgovName });
			forceLoad();
		}
	}

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

		tisCursorLoader = new TisCursorLoader(getActivity(), "%", "%", "%");

		tisCursorLoader.registerListener(0, this);
		Activity act = getActivity();

		spProfile.setAdapter(new AdvStatsFilterAdaper(act, DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, DB.CpuProfile.SORTORDER_DEFAULT));
		spTrigger.setAdapter(new AdvStatsFilterAdaper(act, DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_ID_NAME, DB.Trigger.SORTORDER_DEFAULT));
		spVirtGov.setAdapter(new AdvStatsFilterAdaper(act, DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_ID_NAME, DB.VirtualGovernor.SORTORDER_DEFAULT));

		spProfile.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selection = "%";
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
				String selection = "%";
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
				String selection = "%";
				if (id != AdvStatsFilterAdaper.ALL_ID) {
					selection = ModelAccess.getInstace(getActivity()).getVirtualGovernor(id).getVirtualGovernorName();
				}
				tisCursorLoader.setVirtGov(selection);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView(getActivity());
			}
		};

	}

	@Override
	public void onResume() {
		updateView(getActivity());
		super.onResume();
	}

	@Override
	public void onDestroy() {
		tisCursorLoader.stopLoading();
		super.onDestroy();
	}

	private void updateView(Context context) {
		tisCursorLoader.startLoading();
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
				ContentResolver resolver = ctx.getContentResolver();
				resolver.delete(DB.TimeInStateInput.CONTENT_URI, null, null);
				resolver.delete(DB.TimeInStateIndex.CONTENT_URI, null, null);
				resolver.delete(DB.TimeInStateValue.CONTENT_URI, null, null);
				//				SettingsStorage.getInstance().setTimeinstateBaseline(CpuHandler.getInstance().getCpuTimeinstate());
				updateView(ctx);
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
			updateView(act);
			return true;

		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_INDEX)) {
			return true;
		}
		return false;
	}

	@Override
	public void onLoadComplete(Loader<Cursor> arg0, Cursor displayCursor) {

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, displayCursor,
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
					double l = time * 100 / totalTime;
					sb.append(String.format("%.2f", time * 100 / totalTime)).append("%");
					((TextView) view).setText(sb.toString());
				}
				return true;
			}
		});

		setListAdapter(adapter);

	}
}
