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
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfilesListActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_profiles);

		Cursor c = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, null, null, DB.CpuProfile.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.profile_item, c,
				new String[] { DB.CpuProfile.NAME_PROFILE_NAME, DB.CpuProfile.NAME_GOVERNOR,
				DB.CpuProfile.NAME_FREQUENCY_MIN, DB.CpuProfile.NAME_FREQUENCY_MAX, DB.CpuProfile.NAME_WIFI_STATE, DB.CpuProfile.NAME_GPS_STATE,
				DB.CpuProfile.NAME_BLUETOOTH_STATE, DB.CpuProfile.NAME_MOBILEDATA_3G_STATE, DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE,
				DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE },
				new int[] { R.id.tvName, R.id.tvGov, R.id.tvFreqMin, R.id.tvFreqMax, R.id.tvWifi, R.id.tvGPS, R.id.tvBluetooth,
				R.id.tvMobiledata3G, R.id.tvSync, R.id.tvMobiledataConnection });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int
					columnIndex) {
				if (cursor == null) {
					return false;
				}
				if (columnIndex == DB.CpuProfile.INDEX_PROFILE_NAME) {
					ProfileModel currentProfile = PowerProfiles.getInstance().getCurrentProfile();
					int color = Color.LTGRAY;
					if (currentProfile != null && currentProfile.getDbId() == cursor.getLong(DB.INDEX_ID)) {
						color = Color.GREEN;
					}
					((TextView) view).setTextColor(color);
				} else if (columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MIN
						|| columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MAX) {
					int freq = cursor.getInt(columnIndex);
					if (freq == HardwareHandler.NO_VALUE_INT) {
						((TextView) view).setText(R.string.notAvailable);
					} else {
						((TextView) view).setText(ProfileModel.convertFreq2GHz(freq));
					}
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_GPS_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchGps()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.labelGpsOn;
					if (state == PowerProfiles.SERVICE_STATE_ON) {
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
						textRes = R.string.labelGpsOff;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.labelGpsPrev;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
						color = Color.YELLOW;
					}
					textView.setText(textRes);
					textView.setTextColor(color);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_WIFI_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchWifi()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.labelWifiOn;
					if (state == PowerProfiles.SERVICE_STATE_ON) {
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
						textRes = R.string.labelWifiOff;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.labelWifiPrev;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
						color = Color.YELLOW;
					}
					textView.setText(textRes);
					textView.setTextColor(color);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_BLUETOOTH_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchBluetooth()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.labelBluetoothOn;
					if (state == PowerProfiles.SERVICE_STATE_ON) {
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
						textRes = R.string.labelBluetoothOff;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.labelBluetoothPrev;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
						color = Color.YELLOW;
					}
					textView.setText(textRes);
					textView.setTextColor(color);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_BACKGROUND_SYNC_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchBackgroundSync()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.labelSyncOn;
					if (state == PowerProfiles.SERVICE_STATE_ON) {
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
						textRes = R.string.labelSyncOff;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.labelSyncPrev;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
						color = Color.YELLOW;
					}
					textView.setText(textRes);
					textView.setTextColor(color);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_MOBILEDATA_3G_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchMobiledata3G()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.label3g2g;
					if (state == PowerProfiles.SERVICE_STATE_2G) {
						color = Color.LTGRAY;
						textRes = R.string.label2g;
					} else if (state == PowerProfiles.SERVICE_STATE_2G_3G) {
						color = Color.LTGRAY;
						textRes = R.string.label3g2g;
					} else if (state == PowerProfiles.SERVICE_STATE_3G) {
						color = Color.LTGRAY;
						textRes = R.string.label3g;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.label3g2gPrev;
						color = Color.LTGRAY;
					}
					textView.setTextColor(color);
					textView.setText(textRes);
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_MOBILEDATA_CONNECTION_STATE) {
					TextView textView = (TextView) view;
					if (!SettingsStorage.getInstance().isEnableSwitchMobiledataConnection()) {
						textView.setText("");
						return true;
					}
					int state = cursor.getInt(columnIndex);
					int color = Color.DKGRAY;
					int textRes = R.string.labelMobiledataOn;
					if (state == PowerProfiles.SERVICE_STATE_ON) {
						color = Color.LTGRAY;
						textRes = R.string.labelMobiledataOn;
					} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
						color = Color.LTGRAY;
						textRes = R.string.labelMobiledataOff;
					} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
						textRes = R.string.labelMobiledataPrev;
						color = Color.LTGRAY;
					} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
						textRes = R.string.labelMobiledataOn;
						color = Color.YELLOW;
					}
					textView.setTextColor(color);
					textView.setText(textRes);
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
			Logger.e("bad menuInfo", e);
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
			alertBuilder.setTitle(R.string.menuItemDelete);
			alertBuilder.setMessage(R.string.msgDeleteTriggerNotPossible);
			alertBuilder.setNegativeButton(android.R.string.ok, null);
		} else {
			alertBuilder.setTitle(R.string.menuItemDelete);
			alertBuilder.setMessage(R.string.msg_delete_selected_item);
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
