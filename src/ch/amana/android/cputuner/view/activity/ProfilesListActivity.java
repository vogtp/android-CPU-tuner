package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfilesListActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Profiles");

		// setContentView(R.layout.listview);
		// ((TextView)
		// findViewById(R.id.tvExplain)).setText(R.string.ProfilesExplain);

		Cursor c = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, null, null, DB.CpuProfile.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.cpu_item, c,
				new String[] { DB.CpuProfile.NAME_PROFILE_NAME, DB.CpuProfile.NAME_GOVERNOR, DB.CpuProfile.NAME_FREQUENCY_MIN,
						DB.CpuProfile.NAME_FREQUENCY_MAX, DB.CpuProfile.NAME_WIFI_STATE, DB.CpuProfile.NAME_GPS_STATE,
						DB.CpuProfile.NAME_BLUETOOTH_STATE },
				new int[] { R.id.tvName, R.id.tvGov, R.id.tvFreqMin, R.id.tvFreqMax, R.id.tvWifi, R.id.tvGPS, R.id.tvBluetooth });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int
					columnIndex) {
				if (cursor == null) {
					return false;
				}
				if (columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MIN
						|| columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MAX) {
					int freq = cursor.getInt(columnIndex);

					((TextView) view).setText(CpuModel.convertFreq2GHz(freq));
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_GPS_STATE
						|| columnIndex == DB.CpuProfile.INDEX_WIFI_STATE
						|| columnIndex == DB.CpuProfile.INDEX_BLUETOOTH_STATE) {
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					if (state == 1) {
						color = Color.LTGRAY;
					} else if (state == 2) {
						color = Color.BLACK;
					}
					((TextView) view).setTextColor(color);
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
		Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

}
