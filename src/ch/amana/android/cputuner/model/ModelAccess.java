package ch.amana.android.cputuner.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class ModelAccess {

	private static final String SELECTION_ID = DB.NAME_ID + "=?";
	private static final String PROFILE_SELECTION = CpuProfile.NAME_VIRTUAL_GOVERNOR + "=?";
	private static final String SELECTION_TRIGGER_BY_BATTERYLEVEL = DB.Trigger.NAME_BATTERY_LEVEL + ">=?";

	private static ModelAccess instace;

	private final Context ctx;
	private final Handler handler;
	private final Map<Long, TriggerModel> triggerCache;
	private final Map<Long, ProfileModel> profileCache;
	private final Map<Long, VirtualGovernorModel> virtgovCache;
	private final SortedMap<Integer, Long> triggerByBatteryLevelCache;

	public static ModelAccess getInstace(Context ctx) {
		if (instace == null) {
			instace = new ModelAccess(ctx.getApplicationContext());
		}
		return instace;
	}


	private ModelAccess(Context ctx) {
		super();
		handler = new Handler();
		triggerCache = new HashMap<Long, TriggerModel>();
		profileCache = new HashMap<Long, ProfileModel>();
		virtgovCache = new HashMap<Long, VirtualGovernorModel>();
		Comparator<Integer> batteryLevelComparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer level1, Integer level2) {
				return level2.compareTo(level1);
			}
		};
		triggerByBatteryLevelCache = new TreeMap<Integer, Long>(batteryLevelComparator);
		this.ctx = ctx;
	}

	public void configChanged() {
		BackupRestoreHelper.saveConfiguration(ctx);
	}

	private void update(final Uri uri, final ContentValues values, final String where, final String[] selectionArgs) {
		handler.post(new Runnable() {
			public void run() {
				ctx.getContentResolver().update(uri, values, where, selectionArgs);
			}
		});
	}

	private long getIdFromUri(Uri uri) {
		return ContentUris.parseId(uri);
	}

	public ProfileModel getProfile(Uri uri) {
		return getProfile(getIdFromUri(uri));
	}

	public ProfileModel getProfile(long id) {
		ProfileModel profile = profileCache.get(id);
		if (profile == null) {
			Cursor c = null;
			try {
				c = ctx.getContentResolver().query(CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, SELECTION_ID, new String[] { Long.toString(id) }, null);
				if (c.moveToFirst()) {
					profile = new ProfileModel(c);
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			if (profile == null) {
				profile = new ProfileModel();
			} else {
				profileCache.put(id, profile);
			}
		}
		return profile;
	}

	public void insertProfile(ProfileModel profile) {
		Uri uri = ctx.getContentResolver().insert(DB.CpuProfile.CONTENT_URI, profile.getValues());
		long id = getIdFromUri(uri);
		if (id > -1) {
			profile.setDbId(id);
			profileCache.put(id, profile);
		}
		configChanged();
	}

	public void updateProfile(ProfileModel profile) {
		long id = profile.getDbId();
		long virtualGovernor = profile.getVirtualGovernor();
		if (SettingsStorage.getInstance().isUseVirtualGovernors() && virtualGovernor > -1) {
			VirtualGovernorModel vg = getVirtualGovernor(virtualGovernor);
			vg.applyToProfile(profile);
		}
		update(DB.CpuProfile.CONTENT_URI, profile.getValues(), SELECTION_ID, new String[] { Long.toString(id) });
		profileCache.put(id, profile);
		configChanged();

	}

	public VirtualGovernorModel getVirtualGovernor(Uri uri) {
		return getVirtualGovernor(getIdFromUri(uri));
	}

	public VirtualGovernorModel getVirtualGovernor(long id) {
		VirtualGovernorModel virtGov = virtgovCache.get(id);
		if (virtGov == null) {
			Cursor c = null;
			try {
				c = ctx.getContentResolver().query(DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, SELECTION_ID, new String[] { Long.toString(id) }, null);
				if (c.moveToFirst()) {
					virtGov = new VirtualGovernorModel(c);
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			if (virtGov == null) {
				virtGov = new VirtualGovernorModel();
			} else {
				virtgovCache.put(id, virtGov);
			}
		}
		return virtGov;
	}

	public void insertVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		Uri uri = ctx.getContentResolver().insert(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues());
		long id = ContentUris.parseId(uri);
		if (id > -1) {
			virtualGovModel.setDbId(id);
			virtgovCache.put(id, virtualGovModel);
		}
		configChanged();
	}

	public void updateVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		long id = virtualGovModel.getDbId();
		update(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues(), SELECTION_ID, new String[] { Long.toString(id) });
		virtgovCache.put(id, virtualGovModel);
		updateAllProfilesFromVirtualGovernor(virtualGovModel);
		configChanged();
	}


	private void updateAllProfilesFromVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		Cursor c = null;
		try {
			c = ctx.getContentResolver().query(DB.CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, PROFILE_SELECTION, new String[] { virtualGovModel.getDbId() + "" },
					VirtualGovernor.SORTORDER_DEFAULT);
			while (c.moveToNext()) {
				ProfileModel profile = getProfile(c.getLong(DB.INDEX_ID));
				virtualGovModel.applyToProfile(profile);
				updateProfile(profile);
			}
		} finally {
			if (c != null && !c.isClosed()) {
				c.close();
			}
		}
	}

	public TriggerModel getTrigger(Uri uri) {
		return getTrigger(getIdFromUri(uri));
	}

	public TriggerModel getTrigger(long id) {
		TriggerModel trigger = triggerCache.get(id);
		if (trigger == null) {
			Cursor c = null;
			try {
				c = ctx.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, SELECTION_ID, new String[] { Long.toString(id) }, null);
				if (c.moveToFirst()) {
					trigger = new TriggerModel(c);
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			if (trigger == null) {
				trigger = new TriggerModel();
			} else {
				triggerCache.put(id, trigger);
				updateTriggerByBatteryLevelCache();
			}
		}
		return trigger;
	}

	public void insertTrigger(TriggerModel triggerModel) {
		Uri uri = ctx.getContentResolver().insert(DB.Trigger.CONTENT_URI, triggerModel.getValues());
		long id = ContentUris.parseId(uri);
		if (id > -1) {
			triggerModel.setDbId(id);
			triggerCache.put(id, triggerModel);
			updateTriggerByBatteryLevelCache();
		}
		configChanged();
	}

	public void updateTrigger(TriggerModel triggerModel) {
		update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?", new String[] { triggerModel.getDbId() + "" });
		triggerCache.put(triggerModel.getId(), triggerModel);
		updateTriggerByBatteryLevelCache();
		configChanged();
	}

	private void updateTriggerByBatteryLevelCache() {
		triggerByBatteryLevelCache.clear();
		for (TriggerModel trigger : triggerCache.values()) {
			triggerByBatteryLevelCache.put(trigger.getBatteryLevel(), trigger.getDbId());
		}
	}

	public TriggerModel getTriggerByBatteryLevel(int batteryLevel) {
		long triggerId = -1;
		for (Integer bl : triggerByBatteryLevelCache.keySet()) {
			if (bl >= batteryLevel) {
				triggerId = triggerByBatteryLevelCache.get(bl);
			}
		}
		if (triggerId < 0) {
			triggerId = getTriggerByBatteryLevelFromDB(batteryLevel);
		}
		TriggerModel tm = triggerCache.get(triggerId);
		if (tm == null) {
			tm = new TriggerModel();
			tm.setName(ctx.getString(R.string.notAvailable));
		}
		return tm;

	}

	private long getTriggerByBatteryLevelFromDB(int batteryLevel) {
		Cursor cursor = null;
		try {
			cursor = ctx.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, SELECTION_TRIGGER_BY_BATTERYLEVEL,
					new String[] { batteryLevel + "" }, DB.Trigger.SORTORDER_REVERSE);
			if (cursor != null && cursor.moveToFirst()) {
				long id = cursor.getLong(DB.INDEX_ID);
				triggerCache.put(id, new TriggerModel(cursor));
				return id;
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		try {
			cursor = ctx.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);
			if (cursor != null && cursor.moveToFirst()) {
				long id = cursor.getLong(DB.INDEX_ID);
				triggerCache.put(id, new TriggerModel(cursor));
				return id;
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return -1;
	}

}
