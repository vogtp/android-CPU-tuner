package ch.amana.android.cputuner.view.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.PagerAdapter;
import ch.amana.android.cputuner.view.widget.ServiceSwitcher;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class ProfilesListFragment extends PagerListFragment implements StateChangeListener, LoaderCallbacks<Cursor> {

	private static final int ALPHA_ON = 200;
	private static final int ALPHA_OFF = 40;
	private static final int ALPHA_LEAVE = 100;
	private SimpleCursorAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity act = getActivity();
		if (act == null) {
			return;
		}

		adapter = new SimpleCursorAdapter(act, R.layout.profile_item, null,
				new String[] { DB.CpuProfile.NAME_PROFILE_NAME, DB.CpuProfile.NAME_GOVERNOR,
						DB.CpuProfile.NAME_FREQUENCY_MIN, DB.CpuProfile.NAME_FREQUENCY_MAX, DB.CpuProfile.NAME_WIFI_STATE, DB.CpuProfile.NAME_GPS_STATE,
						DB.CpuProfile.NAME_BLUETOOTH_STATE, DB.CpuProfile.NAME_MOBILEDATA_3G_STATE, DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE,
						DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE, DB.CpuProfile.NAME_AIRPLANEMODE_STATE },
				new int[] { R.id.tvName, R.id.tvGov, R.id.tvFreqMin, R.id.tvFreqMax, R.id.ivServiceWifi, R.id.ivServiceGPS, R.id.ivServiceBluetooth,
						R.id.ivServiceMD3g, R.id.ivServiceSync, R.id.ivServiceMDCon, R.id.ivServiceAirplane }, 0);

		setListShown(false);
		getLoaderManager().initLoader(0, null, this);

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int
					columnIndex) {
				if (cursor == null) {
					return false;
				}
				if (columnIndex == DB.CpuProfile.INDEX_PROFILE_NAME) {
					ProfileModel currentProfile = PowerProfiles.getInstance(getActivity()).getCurrentProfile();
					int color = Color.LTGRAY;
					if (currentProfile != null && currentProfile.getDbId() == cursor.getLong(DB.INDEX_ID) && SettingsStorage.getInstance().isEnableProfiles()) {
						color = getResources().getColor(R.color.cputuner_green);
					}
					((TextView) view).setTextColor(color);
				} else if (columnIndex == DB.CpuProfile.INDEX_GOVERNOR) {
					if (SettingsStorage.getInstance().isUseVirtualGovernors()) {
						int virtGovId = cursor.getInt(CpuProfile.INDEX_VIRTUAL_GOVERNOR);
						if (virtGovId > -1) {
							Cursor virtGovCursor = null;
							try {
								virtGovCursor = getActivity().getContentResolver().query(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, DB.SELECTION_BY_ID,
									new String[] { virtGovId + "" }, VirtualGovernor.SORTORDER_DEFAULT);
							if (virtGovCursor.moveToFirst()) {
								VirtualGovernorModel vgm = new VirtualGovernorModel(virtGovCursor);
								((TextView) view).setText(vgm.getVirtualGovernorName());
								((TextView) ((View) view.getParent()).findViewById(R.id.labelGovernor)).setText(R.string.labelVirtualGovernor);
								return true;
							}
							} finally {
								if (virtGovCursor != null) {
									virtGovCursor.close();
									virtGovCursor = null;
								}
							}
						}
					}
					StringBuilder sb = new StringBuilder();
					sb.append(cursor.getString(DB.CpuProfile.INDEX_GOVERNOR));
					int up = cursor.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_UP);
					if (up > 0) {
						sb.append(" (");
						int down = cursor.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_DOWN);
						if (down > 0) {
							sb.append(down).append("% - ");
						}
						sb.append(up).append("%)");
					}
					((TextView) view).setText(sb.toString());
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MIN
						|| columnIndex == DB.CpuProfile.INDEX_FREQUENCY_MAX) {
					int freq = cursor.getInt(columnIndex);
					if (freq == HardwareHandler.NO_VALUE_INT) {
						((TextView) view).setText(R.string.notAvailable);
					} else {
						((TextView) view).setText(ProfileModel.convertFreq2GHz(freq));
					}
					return true;
				} else if (columnIndex == DB.CpuProfile.INDEX_WIFI_STATE ||
						columnIndex == DB.CpuProfile.INDEX_BLUETOOTH_STATE ||
						columnIndex == DB.CpuProfile.INDEX_BACKGROUND_SYNC_STATE ||
						columnIndex == DB.CpuProfile.INDEX_MOBILEDATA_3G_STATE ||
						columnIndex == DB.CpuProfile.INDEX_MOBILEDATA_CONNECTION_STATE ||
						columnIndex == DB.CpuProfile.INDEX_AIRPLANEMODE_STATE ||
						columnIndex == DB.CpuProfile.INDEX_GPS_STATE) {
					ServiceSwitcher serviceSwitcher = (ServiceSwitcher) view.getParent().getParent().getParent();
					serviceSwitcher.setButtuonState((String) view.getTag(), cursor.getInt(columnIndex));
					return true;
				}
				return false;
			}

		});

		setListAdapter(adapter);

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

	private void setServiceStateIcon(ImageView icon, int state) {
		icon.clearAnimation();
		if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
			icon.setAlpha(ALPHA_LEAVE);
		} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
			icon.setAlpha(ALPHA_OFF);
		} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
			setAnimation(icon, R.anim.back);
		} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
			setAnimation(icon, R.anim.pluse);
		} else {
			icon.setAlpha(ALPHA_ON);
		}
	}
	private void setAnimation(final View v, int resID) {
		final AnimationSet c = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), resID);
		c.setRepeatMode(Animation.RESTART);
		c.setRepeatCount(Animation.INFINITE);
		c.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				Logger.i("Repeat anim");
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				v.clearAnimation();
				v.startAnimation(c);
			}
		});

		v.clearAnimation();
		v.startAnimation(c);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.db_list_context, menu);
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

		final Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			deleteProfile(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;

		case R.id.menuItemInsertAsNew:
			startActivity(new Intent(CpuTunerProvider.ACTION_INSERT_AS_NEW, uri));
			return true;

		default:
			return handleCommonMenu(getActivity(), item);
		}

	}

	private void deleteProfile(final Uri uri) {
		final Activity act = getActivity();
		Builder alertBuilder = new AlertDialog.Builder(act);
		if (ModelAccess.getInstace(getActivity()).isProfileUsed(ContentUris.parseId(uri))) {
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
					ModelAccess.getInstace(getActivity()).delete(uri);
				}
			});
		}
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
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_PROFILE)) {
			return true;
		}
		return false;
	}

	private boolean handleCommonMenu(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			act.startActivity(new Intent(Intent.ACTION_INSERT, DB.CpuProfile.CONTENT_URI));
			return true;
		}
		return false;
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<ActionBar.Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				Intent intent = new Intent(Intent.ACTION_INSERT, DB.CpuProfile.CONTENT_URI);
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
		getListView().setAdapter(adapter);
	}

	@Override
	public void deviceStatusChanged() {
	}

	@Override
	public void triggerChanged() {
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		return new CursorLoader(getActivity(), DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
