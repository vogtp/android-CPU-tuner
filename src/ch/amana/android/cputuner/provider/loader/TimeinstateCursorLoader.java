package ch.amana.android.cputuner.provider.loader;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateIndex;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateInput;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateValue;
import ch.amana.android.cputuner.receiver.StatisticsReceiver;

public class TimeinstateCursorLoader extends CursorLoader {
	private String triggerName = null;
	private String profileName = null;
	private String virtgovName = null;
	private long indexID = -1;
	private final ContentResolver resolver;
	private final Context context;
	private long totalTime;

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

	public TimeinstateCursorLoader(Context context) {
		super(context);
		this.context = context;
		this.resolver = getContext().getContentResolver();
	}

	public TimeinstateCursorLoader(Context context, String triggerName, String profileName, String virtgovName) {
		super(context, DB.TimeInStateInput.CONTENT_URI, DB.TimeInStateInput.PROJECTION_DEFAULT, null, null, DB.TimeInStateInput.SORTORDER_DEFAULT);
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
		addCurrentValues();
		processInput(triggerName, profileName, virtgovName);
		return doResult();
	}

	private void addCurrentValues() {
		PowerProfiles instance = PowerProfiles.getInstance();
		if (DB.SQL_WILDCARD.equals(profileName) || (instance.getCurrentProfileName().equals(profileName)) &&
				(DB.SQL_WILDCARD.equals(triggerName) || instance.getCurrentTriggerName().equals(triggerName))) {
			Bundle bundle = new Bundle(3);
			bundle.putString(DB.SwitchLogDB.NAME_TRIGGER, profileName);
			bundle.putString(DB.SwitchLogDB.NAME_PROFILE, triggerName);
			bundle.putString(DB.SwitchLogDB.NAME_VIRTGOV, instance.getCurrentVirtGovName());
			StatisticsReceiver.updateStatisticsInputQueue(context, null);
		}
	}

	private Cursor doResult() {
		String selection = TimeInStateIndex.SELECTION_TRIGGER_PROFILE_VIRTGOV;
		String[] selectionArgs = new String[] { triggerName, profileName, virtgovName };
		Cursor resultCursor = resolver.query(TimeInStateValue.CONTENT_URI_GROUPED, null, selection, selectionArgs, TimeInStateValue.SORTORDER_DEFAULT);
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

	public long getTotalTime() {
		return totalTime;
	}

}