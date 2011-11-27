package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.PagerAdapter;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class TriggersListFragment extends PagerListFragment implements StateChangeListener {

	protected static final String NO_PROFILE = "no profile";
	private Cursor displayCursor;
	//	private Cursor checkCursor;
	private SimpleCursorAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private boolean initListView() {
		if (displayCursor != null && !displayCursor.isClosed()) {
			return false;
		}
		final Activity act = getActivity();
		displayCursor = act.managedQuery(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);

		int layout = SettingsStorage.getInstance().isPowerStrongerThanScreenoff() ? R.layout.trigger_item_pwrstrong : R.layout.trigger_item_pwrweak;
		adapter = new SimpleCursorAdapter(getActivity(), layout, displayCursor, new String[] { DB.Trigger.NAME_TRIGGER_NAME, DB.Trigger.NAME_BATTERY_LEVEL,
				DB.Trigger.NAME_BATTERY_PROFILE_ID, DB.Trigger.NAME_POWER_PROFILE_ID, DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, DB.Trigger.NAME_POWER_CURRENT_CNT_POW,
				DB.Trigger.NAME_POWER_CURRENT_CNT_BAT, DB.Trigger.NAME_POWER_CURRENT_CNT_LCK, DB.Trigger.NAME_HOT_PROFILE_ID, DB.Trigger.NAME_POWER_CURRENT_CNT_HOT,
				DB.Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID, DB.Trigger.NAME_POWER_CURRENT_CNT_CALL }, new int[] { R.id.tvName, R.id.tvBatteryLevel, R.id.tvProfileOnBattery,
				R.id.tvProfileOnPower, R.id.tvProfileScreenLocked, R.id.tvPowerCurrentPower, R.id.tvPowerCurrentBattery, R.id.tvPowerCurrentLocked, R.id.tvProfileHot,
				R.id.tvPowerCurrentHot, R.id.tvProfileCall, R.id.tvPowerCurrentCall });

		setListAdapter(adapter);

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (cursor == null) {
					return false;
				}
				int color = Color.LTGRAY;
				PowerProfiles powerProfiles = PowerProfiles.getInstance(getActivity());
				TriggerModel currentTrigger = powerProfiles.getCurrentTrigger();
				boolean isCurrentTrigger = currentTrigger != null && currentTrigger.getDbId() == cursor.getLong(DB.INDEX_ID)
						&& SettingsStorage.getInstance().isEnableProfiles();
				if (columnIndex == DB.Trigger.INDEX_TRIGGER_NAME) {
					if (isCurrentTrigger) {
						color = getResources().getColor(R.color.cputuner_green);
					}
					((TextView) view).setTextColor(color);
				} else if (columnIndex == DB.Trigger.INDEX_BATTERY_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_POWER_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_HOT_PROFILE_ID
						|| columnIndex == DB.Trigger.INDEX_CALL_IN_PROGRESS_PROFILE_ID) {
					long profileId = cursor.getLong(columnIndex);
					String profileName = NO_PROFILE;
					Cursor cpuCursor = getActivity().managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME,
							DB.NAME_ID + "=?", new String[] { profileId + "" }, DB.CpuProfile.SORTORDER_DEFAULT);
					if (cpuCursor.moveToFirst()) {
						profileName = cpuCursor.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
						((View) view.getParent()).setVisibility(View.VISIBLE);
					} else {
						((View) view.getParent()).setVisibility(View.GONE);
					}
					if (columnIndex == DB.Trigger.INDEX_CALL_IN_PROGRESS_PROFILE_ID && !SettingsStorage.getInstance().isEnableCallInProgressProfile()) {
						((View) view.getParent()).setVisibility(View.GONE);
					}
					if (isCurrentTrigger && cursor.getLong(columnIndex) == powerProfiles.getCurrentProfile().getDbId()) {
						if (powerProfiles.isCallInProgress() && SettingsStorage.getInstance().isEnableCallInProgressProfile()) {
							if (columnIndex == DB.Trigger.INDEX_CALL_IN_PROGRESS_PROFILE_ID) {
								color = getResources().getColor(R.color.cputuner_green);
							}
						} else if (powerProfiles.isBatteryHot() && cursor.getInt(DB.Trigger.INDEX_HOT_PROFILE_ID) != PowerProfiles.NO_PROFILE) {
							if (columnIndex == DB.Trigger.INDEX_HOT_PROFILE_ID) {
								color = getResources().getColor(R.color.cputuner_green);
							}
						} else {
							if ((columnIndex == DB.Trigger.INDEX_BATTERY_PROFILE_ID && powerProfiles.isOnBatteryProfile())
									|| (columnIndex == DB.Trigger.INDEX_POWER_PROFILE_ID && powerProfiles.isAcPower())
									|| (columnIndex == DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID && powerProfiles.isScreenOff())) {
								color = getResources().getColor(R.color.cputuner_green);
							}
						}
					}
					if (cpuCursor != null) {
						cpuCursor.close();
					}
					TextView tv = ((TextView) view);
					tv.setText(profileName);
					tv.setTextColor(color);
					return true;
				} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_POW
						|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_LCK
						|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_BAT
						|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_HOT
						|| columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_CALL) {

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
					} else if (columnIndex == DB.Trigger.INDEX_POWER_CURRENT_CNT_CALL) {
						cnt = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_CALL);
						current = cursor.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_CALL);
					}
					if (cnt < 1) {
						((TextView) view).setText("-");
						return true;
					}
					current /= cnt;
					if (current < -10000 || current > 10000) {
						((TextView) view).setText("-");
						return true;
					}
					((TextView) view).setText(String.format("%.0f mA/h", current));
					return true;
				}
				return false;
			}

		});
		return true;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();

		initListView();

		getListView().setOnCreateContextMenuListener(this);

		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
	}

	@Override
	public void onDestroy() {
		Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			if (act != null) {
				((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
			}
		}
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.Trigger.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final Activity act = getActivity();
		act.getMenuInflater().inflate(R.menu.db_list_context, menu);
		act.getMenuInflater().inflate(R.menu.triggerlist_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!this.getClass().equals(PagerAdapter.getCurrentItem().getClass())) {
			return false;
		}
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

		case R.id.menuItemInsertAsNew:
			startActivity(new Intent(CpuTunerProvider.ACTION_INSERT_AS_NEW, uri));
			return true;

		case R.id.menuItemClearPowerCurrent:
			clearPowerConsumtion(uri);
			return true;

		default:
			return handleCommonMenu(getActivity(), item);
		}

	}

	private void clearPowerConsumtion(final Uri uri) {
		final Activity act = getActivity();
		final ContentResolver resolver = act.getContentResolver();
		Cursor c = resolver.query(uri, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);
		if (c.moveToFirst()) {
			final TriggerModel triggerModel = new TriggerModel(c);
			Builder alertBuilder = new AlertDialog.Builder(act);
			alertBuilder.setTitle(R.string.menuItemClearPowerCurrent);
			alertBuilder.setMessage(getResources().getString(R.string.msg_clear_power_consumption_of_named_trigger, triggerModel.getName()));
			alertBuilder.setNegativeButton(android.R.string.no, null);
			alertBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					triggerModel.clearPowerCurrent();
					try {
						PowerProfiles.setUpdateTrigger(false);
						//						resolver.update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?", new String[] { triggerModel.getDbId() + "" });
						triggerModel.clearPowerCurrent();
						ModelAccess.getInstace(getActivity()).updateTrigger(triggerModel, false);
					} catch (Exception e) {
						Logger.w("Cannot reset trigger power consumption", e);
					} finally {
						PowerProfiles.setUpdateTrigger(true);
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
		final Activity act = getActivity();
		Builder alertBuilder = new AlertDialog.Builder(act);
		alertBuilder.setTitle(R.string.menuItemDelete);
		alertBuilder.setMessage(R.string.msg_delete_selected_item);
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ModelAccess.getInstace(getActivity()).delete(uri);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list_option, menu);
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		if (handleCommonMenu(act, item)) {
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_TRIGGER)) {
			return true;
		}
		return false;
	}

	private boolean handleCommonMenu(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			act.startActivity(new Intent(Intent.ACTION_INSERT, DB.Trigger.CONTENT_URI));
			return true;
		}
		return false;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem menuItemClearPowerCurrent = menu.findItem(R.id.menuItemClearPowerCurrent);
		if (menuItemClearPowerCurrent != null) {
			menuItemClearPowerCurrent.setVisible(SettingsStorage.getInstance().getTrackCurrentType() != SettingsStorage.TRACK_CURRENT_HIDE);
		}
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				Intent intent = new Intent(Intent.ACTION_INSERT, DB.Trigger.CONTENT_URI);
				view.getContext().startActivity(intent);
			}

			@Override
			public int getDrawable() {
				return android.R.drawable.ic_menu_add;
			}
		});
		return actions;
	}

	@Override
	public void profileChanged() {
	}

	@Override
	public void deviceStatusChanged() {
	}

	@Override
	public void triggerChanged() {
		if (!initListView()) {
			getListView().setAdapter(adapter);
		}
	}

	@Override
	public void onResume() {
		initListView();
		super.onResume();
	}

}
