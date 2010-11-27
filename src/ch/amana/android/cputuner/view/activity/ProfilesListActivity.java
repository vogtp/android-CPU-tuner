package ch.amana.android.cputuner.view.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfilesListActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Profiles");

		Cursor c = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, null, null, DB.CpuProfile.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.cpu_item, c,
				new String[] { DB.CpuProfile.NAME_PROFILE_NAME, DB.CpuProfile.NAME_GOVERNOR, DB.CpuProfile.NAME_FREQUENCY_MIN,
						DB.CpuProfile.NAME_FREQUENCY_MAX, DB.CpuProfile.NAME_WIFI_STATE, DB.CpuProfile.NAME_GPS_STATE,
						DB.CpuProfile.NAME_BLUETOOTH_STATE, DB.CpuProfile.NAME_MOBILEDATA_STATE,
						DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE },
				new int[] { R.id.tvName, R.id.tvGov, R.id.tvFreqMin, R.id.tvFreqMax, R.id.tvWifi, R.id.tvGPS, R.id.tvBluetooth,
						R.id.tvMobiledata, R.id.tvSync });

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
						|| columnIndex == DB.CpuProfile.INDEX_BLUETOOTH_STATE
						|| columnIndex == DB.CpuProfile.INDEX_BACKGROUND_SYNC_STATE) {
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					if (state == 1) {
						color = Color.LTGRAY;
					} else if (state == 2) {
						color = Color.BLACK;
					}
					((TextView) view).setTextColor(color);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_MOBILEDATA_STATE) {
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textID = R.string.label3g2g;
					if (state == 1) {
						color = Color.LTGRAY;
						textID = R.string.label2g;
					} else if (state == 2) {
						color = Color.LTGRAY;
					}
					TextView textView = (TextView) view;
					textView.setTextColor(color);
					textView.setText(textID);
					return true;
				}
				return false;
			}
		});

		getListView().setAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.profilelist_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(Logger.TAG, "bad menuInfo", e);
			return false;
		}

		final Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			deleteProfile(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;

		default:
			return handleCommonMenu(item);
		}

	}

	private void deleteProfile(final Uri uri) {
		String id = ContentUris.parseId(uri) + "";
		Cursor cursor = getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
					DB.Trigger.NAME_BATTERY_PROFILE_ID + "=? OR " +
							DB.Trigger.NAME_POWER_PROFILE_ID + "=? OR " +
							DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID + "=?",
				new String[] { id, id, id }, DB.Trigger.SORTORDER_DEFAULT);
		Builder alertBuilder = new AlertDialog.Builder(this);
		if (cursor != null && cursor.getCount() > 0) {
			// no not delete
			alertBuilder.setTitle("Delete");
			alertBuilder.setMessage("Cannot delete this profile since it is used in one or more tiggers!");
			alertBuilder.setNegativeButton(android.R.string.ok, null);
		} else {
			alertBuilder.setTitle("Delete");
			alertBuilder.setMessage("Delete selected item?");
			alertBuilder.setNegativeButton(android.R.string.no, null);
			alertBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					getContentResolver().delete(uri, null, null);
				}
			});
		}
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.list_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return handleCommonMenu(item);
	}

	private boolean handleCommonMenu(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			startActivity(new Intent(Intent.ACTION_INSERT, DB.CpuProfile.CONTENT_URI));
			break;
		}
		return false;
	}

}
