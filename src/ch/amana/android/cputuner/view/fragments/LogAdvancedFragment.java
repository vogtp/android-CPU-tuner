package ch.amana.android.cputuner.view.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.SwitchLogDB;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class LogAdvancedFragment extends PagerFragment implements StateChangeListener {

	private Cursor displayCursor;
	private SimpleCursorTreeAdapter adapter;
	private final Date now = new Date();
	private ExpandableListView elvLog;

	private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.switch_log_adv, container, false);
		elvLog = (ExpandableListView) v.findViewById(R.id.elvLog);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();
		if (displayCursor == null) {
			displayCursor = act.managedQuery(DB.SwitchLogDB.CONTENT_URI, DB.SwitchLogDB.PROJECTION_DEFAULT, null, null, DB.SwitchLogDB.SORTORDER_DEFAULT);
			adapter = new SimpleCursorTreeAdapter(act, displayCursor,
					android.R.layout.simple_spinner_item, new String[] { DB.SwitchLogDB.NAME_MESSAGE }, new int[] { android.R.id.text1 },
					android.R.layout.two_line_list_item, new String[] { DB.SwitchLogDB.NAME_BATTERY, DB.SwitchLogDB.NAME_PROFILE }, new int[] { android.R.id.text1,
							android.R.id.text2 }) {
				
				@Override
				protected Cursor getChildrenCursor(Cursor groupCursor) {
					String id = Integer.toString(groupCursor.getInt(DB.INDEX_ID));
					return act.managedQuery(DB.SwitchLogDB.CONTENT_URI, SwitchLogDB.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
							new String[] { id }, SwitchLogDB.SORTORDER_DEFAULT);
				}
			};

			
			adapter.setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (columnIndex == DB.SwitchLogDB.INDEX_MESSAGE) {
						now.setTime(cursor.getLong(DB.SwitchLogDB.INDEX_TIME));
						StringBuilder sb = new StringBuilder();
						sb.append(logDateFormat.format(now)).append(": ");
						sb.append(cursor.getString(DB.SwitchLogDB.INDEX_MESSAGE));
						TextView textView = (TextView) view;
						textView.setText(sb.toString());
						textView.setTextColor(Color.LTGRAY);
						return true;
					}
					return false;
				}
			});

			elvLog.setAdapter(adapter);
		}

		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
	}

	@Override
	public void onPause() {

		Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).removeStateChangeListener(this);
		}
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (SettingsStorage.getInstance().isAdvancesStatistics()) {
			inflater.inflate(R.menu.log_advstat_option, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemMark:
			Intent i = new Intent(SwitchLog.ACTION_ADD_TO_LOG);
			i.putExtra(SwitchLog.EXTRA_LOG_ENTRY, act.getString(R.string.menuMarkLog));
			act.sendBroadcast(i);
			return true;
		case R.id.itemClear:
			act.getContentResolver().delete(DB.SwitchLogDB.CONTENT_URI, null, null);
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_PROFILE)) {
			return true;
		}
		return false;
	}

	@Override
	public void profileChanged() {
		requestUpdate();
	}

	@Override
	public void deviceStatusChanged() {
		requestUpdate();
	}

	@Override
	public void triggerChanged() {
		requestUpdate();
	}

	private void requestUpdate() {
		getActivity().sendBroadcast(new Intent(SwitchLog.ACTION_FLUSH_LOG));
	}
}
