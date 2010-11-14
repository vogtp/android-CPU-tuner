package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggersListActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Triggers");

		// setContentView(R.layout.listview);
		// ((TextView)
		// findViewById(R.id.tvExplain)).setText(R.string.triggersExplain);

		Cursor c = managedQuery(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.trigger_item, c,
				new String[] { DB.Trigger.NAME_TRIGGER_NAME, DB.Trigger.NAME_BATTERY_LEVEL, DB.Trigger.NAME_BATTERY_PROFILE_ID,
						DB.Trigger.NAME_POWER_PROFILE_ID, DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID },
				new int[] { R.id.tvName, R.id.tvBatteryLevel, R.id.tvProfileOnBattery, R.id.tvProfileOnPower, R.id.tvProfileScreenLocked });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (cursor == null) {
					return false;
				}
				if (columnIndex == DB.Trigger.INDEX_BATTERY_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_POWER_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID) {
					long profileId = cursor.getLong(columnIndex);
					Cursor cpuCursor = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME,
							DB.NAME_ID + "=?", new String[] { profileId + "" }, DB.CpuProfile.SORTORDER_DEFAULT);
					cpuCursor.moveToFirst();
					((TextView) view).setText(cpuCursor.getString(DB.CpuProfile.INDEX_PROFILE_NAME));
					return true;
				}
				return false;
			}
		});

		getListView().setAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.Trigger.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

}
