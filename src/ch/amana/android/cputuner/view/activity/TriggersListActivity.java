package ch.amana.android.cputuner.view.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentResolver;
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
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggersListActivity extends ListActivity {

	protected static final String NO_PROFILE = "no profile";
	private Cursor displayCursor;
	private Cursor checkCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Triggers");

		displayCursor = managedQuery(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);

		checkCursor = managedQuery(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_MINIMAL_HOT_PROFILE, DB.Trigger.NAME_HOT_PROFILE_ID + " > -1", null,
				DB.Trigger.SORTORDER_MINIMAL_HOT_PROFILE);

	}

	@Override
	protected void onResume() {
		super.onResume();
		SimpleCursorAdapter adapter;
		checkCursor.requery();
		if (checkCursor.getCount() > 0) {
			adapter = new SimpleCursorAdapter(this, R.layout.trigger_item, displayCursor,
					new String[] { DB.Trigger.NAME_TRIGGER_NAME, DB.Trigger.NAME_BATTERY_LEVEL, DB.Trigger.NAME_BATTERY_PROFILE_ID,
							DB.Trigger.NAME_POWER_PROFILE_ID, DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, DB.Trigger.NAME_POWER_CURRENT_CNT_POW,
							DB.Trigger.NAME_POWER_CURRENT_CNT_BAT, DB.Trigger.NAME_POWER_CURRENT_CNT_LCK, DB.Trigger.NAME_HOT_PROFILE_ID,
							DB.Trigger.NAME_POWER_CURRENT_CNT_HOT },
					new int[] { R.id.tvName, R.id.tvBatteryLevel, R.id.tvProfileOnBattery, R.id.tvProfileOnPower, R.id.tvProfileScreenLocked,
							R.id.tvPowerCurrentPower, R.id.tvPowerCurrentBattery, R.id.tvPowerCurrentLocked, R.id.tvProfileHot, R.id.tvPowerCurrentHot });

		} else {
			adapter = new SimpleCursorAdapter(this, R.layout.trigger_item_no_hot,
					displayCursor,
					new String[] { DB.Trigger.NAME_TRIGGER_NAME,
							DB.Trigger.NAME_BATTERY_LEVEL, DB.Trigger.NAME_BATTERY_PROFILE_ID,
							DB.Trigger.NAME_POWER_PROFILE_ID,
							DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID,
							DB.Trigger.NAME_POWER_CURRENT_CNT_POW,
							DB.Trigger.NAME_POWER_CURRENT_CNT_BAT,
							DB.Trigger.NAME_POWER_CURRENT_CNT_LCK },
					new int[] { R.id.tvName, R.id.tvBatteryLevel,
							R.id.tvProfileOnBattery, R.id.tvProfileOnPower,
							R.id.tvProfileScreenLocked,
							R.id.tvPowerCurrentPower, R.id.tvPowerCurrentBattery,
							R.id.tvPowerCurrentLocked });

		}

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (cursor == null) {
					return false;
				}
				if (columnIndex == DB.Trigger.INDEX_TRIGGER_NAME) {
					TriggerModel currentTrigger = PowerProfiles.getInstance().getCurrentTrigger();
					int color = Color.LTGRAY;
					if (currentTrigger != null && currentTrigger.getDbId() == cursor.getLong(DB.INDEX_ID)) {
						color = Color.GREEN;
					}
					((TextView) view).setTextColor(color);
				} else if (columnIndex == DB.Trigger.INDEX_BATTERY_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_POWER_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_HOT_PROFILE_ID) {
					long profileId = cursor.getLong(columnIndex);
					String profileName = NO_PROFILE;
					Cursor cpuCursor = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME,
								DB.NAME_ID + "=?", new String[] { profileId + "" }, DB.CpuProfile.SORTORDER_DEFAULT);
					if (cpuCursor.moveToFirst()) {
						profileName = cpuCursor.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
					}

					((TextView) view).setText(profileName);
					return true;
				} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_POW
							|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_LCK
							|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_BAT
							|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_HOT) {

					if (SettingsStorage.getInstance().getTrackCurrentType() == SettingsStorage.TRACK_CURRENT_HIDE) {
						((TextView) view).setText("");
						return true;
					}

					long cnt = 0;
					double current = 0;
					if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_POW) {
						cnt = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_POW);
						current = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_POW);
					} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_LCK) {
						cnt = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_LCK);
						current = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_LCK);
					} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_BAT) {
						cnt = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_BAT);
						current = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_BAT);
					} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_HOT) {
						cnt = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_HOT);
						current = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_HOT);
					}
					if (cnt < 1) {
						((TextView) view).setText("-");
						return true;
					}
					current /= cnt;
					((TextView) view).setText(String.format("%.0f mA/h", current));
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
		Uri uri = ContentUris.withAppendedId(DB.Trigger.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.triggerlist_context, menu);
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

		final Uri uri = ContentUris.withAppendedId(DB.Trigger.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			deleteTrigger(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;

		case R.id.menuItemClearPowerCurrent:
			clearPowerConsumtion(uri);
			return true;

		default:
			return handleCommonMenu(item);
		}

	}

	private void clearPowerConsumtion(final Uri uri) {
		final ContentResolver resolver = getContentResolver();
		Cursor c = resolver.query(uri, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);
		if (c.moveToFirst()) {
			final TriggerModel triggerModel = new TriggerModel(c);
			Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setTitle(R.string.menuItemClearPowerCurrent);
			alertBuilder.setMessage("Clear power consumption of \"" + triggerModel.getName() + "\" trigger?");
			alertBuilder.setNegativeButton(android.R.string.no, null);
			alertBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					triggerModel.clearPowerCurrent();
					PowerProfiles powerProfiles = PowerProfiles.getInstance();
					try {
						powerProfiles.setUpdateTrigger(false);
						resolver.update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?", new String[] { triggerModel.getDbId() + "" });
					} catch (Exception e) {
						Logger.w("Cannot reset trigger power consumption", e);
					} finally {
						powerProfiles.setUpdateTrigger(true);
					}

				}
			});
			AlertDialog alert = alertBuilder.create();
			alert.show();
		}
		if (c != null && !c.isClosed()) {
			c.close();
		}
		PowerProfiles.getInstance().reapplyProfile(true);
	}

	private void deleteTrigger(final Uri uri) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.menuItemDelete);
		alertBuilder.setMessage("Delete selected item?");
		alertBuilder.setNegativeButton(android.R.string.no, null);
		alertBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
			}
		});
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
			startActivity(new Intent(Intent.ACTION_INSERT, DB.Trigger.CONTENT_URI));
			break;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem menuItemClearPowerCurrent = menu.findItem(R.id.menuItemClearPowerCurrent);
		if (menuItemClearPowerCurrent != null) {
			menuItemClearPowerCurrent.setVisible(SettingsStorage.getInstance().getTrackCurrentType() != SettingsStorage.TRACK_CURRENT_HIDE);
		}
		return true;
	}
}
