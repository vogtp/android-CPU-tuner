package ch.amana.android.cputuner.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class ModelAccess implements BackupRestoreCallback {

	private static final String SELECTION_BY_ID = DB.NAME_ID + "=?";
	// private static final String SELECTION_TRIGGER_BY_BATTERYLEVEL =
	// DB.Trigger.NAME_BATTERY_LEVEL + ">=?";
	private static final String SELECTION_PROFILE_BY_VIRTGOV = DB.CpuProfile.NAME_VIRTUAL_GOVERNOR + "=? ";
	private static final String SELECTION_GET_TRIGGERS_WITH_PROFILE = DB.Trigger.NAME_BATTERY_PROFILE_ID + "=? OR " + DB.Trigger.NAME_POWER_PROFILE_ID + "=? OR "
			+ DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID + "=?";

	private static ModelAccess instace;

	private final Context ctx;
	private ContentResolver contentResolver;
	// private final Handler handler2;
	private Map<Long, TriggerModel> triggerCache;
	private Map<Long, ProfileModel> profileCache;
	private Map<Long, VirtualGovernorModel> virtgovCache;
	private SortedMap<Integer, Long> triggerByBatteryLevelCache;
	private final Comparator<Integer> batteryLevelComparator;
	public static final Object triggerCacheMutex = new Object();
	public static final Object profileCacheMutex = new Object();
	public static final Object virtgovCacheMutex = new Object();
	private static final Object triggerByBatteryLevelCacheMutex = new Object();
	private SettingsStorage settings;
	private BackupRestoreHelper backupRestoreHelper;

	// TODO use init instance and get rid of context
	public static ModelAccess getInstace(Context ctx) {
		if (instace == null) {
			instace = new ModelAccess(ctx.getApplicationContext());
		}
		return instace;
	}

	private ModelAccess(Context ctx) {
		super();
		this.ctx = ctx;

		settings = SettingsStorage.getInstance();
		backupRestoreHelper = new BackupRestoreHelper(this);
		contentResolver = ctx.getContentResolver();
		// handler = new Handler();
		batteryLevelComparator = new Comparator<Integer>() {

			@Override
			public int compare(Integer level1, Integer level2) {
				return level2.compareTo(level1);
			}
		};
		clearCache();
	}

	public void clearCache() {
		synchronized (triggerCacheMutex) {
			triggerCache = new HashMap<Long, TriggerModel>();
		}
		synchronized (profileCacheMutex) {
			profileCache = new HashMap<Long, ProfileModel>();
		}
		synchronized (virtgovCacheMutex) {
			virtgovCache = new HashMap<Long, VirtualGovernorModel>();
		}
		synchronized (triggerByBatteryLevelCacheMutex) {
			triggerByBatteryLevelCache = new TreeMap<Integer, Long>(batteryLevelComparator);
		}
		initTriggerByBatteryLevelCache();
	}

	public void configChanged() {
		if (InstallHelper.hasConfig(ctx)) {
			if (settings.isEnableProfiles()) {
				PowerProfiles.getInstance().reapplyProfile(false);
			}
			if (settings.isSaveConfiguration() && InstallHelper.hasConfig(ctx)) {
				backupRestoreHelper.backupConfiguration(settings.getCurrentConfiguration());
			}
		}
	}

	private void update(final Uri uri, final ContentValues values, final String where, final String[] selectionArgs) {
		update(uri, values, where, selectionArgs, true);
	}

	private void update(final Uri uri, final ContentValues values, final String where, final String[] selectionArgs, boolean saveConfig) {
		contentResolver.update(uri, values, where, selectionArgs);
	}

	private long getIdFromUri(Uri uri) {
		return ContentUris.parseId(uri);
	}

	public ProfileModel getProfile(Uri uri) {
		return getProfile(getIdFromUri(uri));
	}

	public String getProfileName(long id) {
		ProfileModel profile = profileCache.get(id);
		if (profile == null) {
			profile = getProfile(id);
		}
		return profile.getProfileName();
	}

	public ProfileModel getProfile(long id) {
		synchronized (profileCacheMutex) {
			ProfileModel profile = profileCache.get(id);
			if (profile == null) {
				Cursor c = null;
				try {
					c = contentResolver.query(CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, SELECTION_BY_ID, new String[] { Long.toString(id) }, null);
					if (c.moveToFirst()) {
						profile = new ProfileModel(c);
					}
				} finally {
					if (c != null && !c.isClosed()) {
						c.close();
					}
				}
				if (profile == null) {
					profile = ProfileModel.NO_PROFILE;
				} else {
					profileCache.put(id, profile);
				}
			}
			return new ProfileModel(profile);
		}
	}

	public void insertProfile(ProfileModel profile) {
		synchronized (profileCacheMutex) {
			Uri uri = contentResolver.insert(DB.CpuProfile.CONTENT_URI, profile.getValues());
			long id = getIdFromUri(uri);
			if (id > -1) {
				profile.setDbId(id);
				profileCache.put(id, profile);
			}
			configChanged();
		}
	}

	public void updateProfile(ProfileModel profile) {
		synchronized (profileCacheMutex) {
			long id = profile.getDbId();
			long virtualGovernor = profile.getVirtualGovernor();
			if (SettingsStorage.getInstance().isUseVirtualGovernors() && virtualGovernor > -1) {
				VirtualGovernorModel vg = getVirtualGovernor(virtualGovernor);
				vg.applyToProfile(profile);
			}
			profileCache.put(id, profile);
			update(DB.CpuProfile.CONTENT_URI, profile.getValues(), SELECTION_BY_ID, new String[] { Long.toString(id) });
			configChanged();
		}
	}

	public VirtualGovernorModel getVirtualGovernor(Uri uri) {
		return getVirtualGovernor(getIdFromUri(uri));
	}

	public VirtualGovernorModel getVirtualGovernor(long id) {
		synchronized (virtgovCacheMutex) {
			VirtualGovernorModel virtGov = virtgovCache.get(id);
			if (virtGov == null) {
				Cursor c = null;
				try {
					c = contentResolver.query(DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, SELECTION_BY_ID, new String[] { Long.toString(id) }, null);
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
			return new VirtualGovernorModel(virtGov);
		}
	}

	public void insertVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		synchronized (virtgovCacheMutex) {
			Uri uri = contentResolver.insert(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues());
			long id = ContentUris.parseId(uri);
			if (id > -1) {
				virtualGovModel.setDbId(id);
				virtgovCache.put(id, virtualGovModel);
			}
			configChanged();
		}
	}

	public void updateVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		synchronized (virtgovCacheMutex) {
			long id = virtualGovModel.getDbId();
			virtgovCache.put(id, virtualGovModel);
			update(DB.VirtualGovernor.CONTENT_URI, virtualGovModel.getValues(), SELECTION_BY_ID, new String[] { Long.toString(id) });
			updateAllProfilesFromVirtualGovernor(virtualGovModel);
			configChanged();
		}
	}

	private void updateAllProfilesFromVirtualGovernor(VirtualGovernorModel virtualGovModel) {
		Cursor c = null;
		try {
			c = contentResolver.query(DB.CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, SELECTION_PROFILE_BY_VIRTGOV, new String[] { virtualGovModel.getDbId() + "" },
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
		synchronized (triggerCacheMutex) {
			TriggerModel trigger = triggerCache.get(id);
			if (trigger == null) {
				Cursor c = null;
				try {
					c = contentResolver.query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, SELECTION_BY_ID, new String[] { Long.toString(id) }, null);
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
					initTriggerByBatteryLevelCache();
				}
			}
			return new TriggerModel(trigger);
		}
	}

	public void insertTrigger(TriggerModel triggerModel) {
		synchronized (triggerCache) {
			Uri uri = contentResolver.insert(DB.Trigger.CONTENT_URI, triggerModel.getValues());
			long id = ContentUris.parseId(uri);
			if (id > -1) {
				triggerModel.setDbId(id);
				triggerCache.put(id, triggerModel);
				initTriggerByBatteryLevelCache();
			}
			configChanged();
		}
	}

	public void updateTrigger(TriggerModel triggerModel) {
		updateTrigger(triggerModel, true);
	}

	public void updateTrigger(TriggerModel triggerModel, boolean saveConfig) {
		synchronized (triggerCacheMutex) {
			triggerCache.put(triggerModel.getId(), triggerModel);
			initTriggerByBatteryLevelCache();
			update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?", new String[] { triggerModel.getDbId() + "" }, saveConfig);
			if (saveConfig) {
				configChanged();
			}
		}
	}

	private void initTriggerByBatteryLevelCache() {
		synchronized (triggerByBatteryLevelCacheMutex) {
			triggerByBatteryLevelCache.clear();
			Cursor cursor = null;
			try {
				cursor = contentResolver.query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, null, null, DB.Trigger.SORTORDER_DEFAULT);
				if (cursor == null) {
					return;
				}
				while (cursor.moveToNext()) {
					long id = cursor.getLong(DB.INDEX_ID);
					int bl = cursor.getInt(DB.Trigger.INDEX_BATTERY_LEVEL);
					triggerByBatteryLevelCache.put(bl, id);
				}
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
		}
	}

	public TriggerModel getTriggerByBatteryLevel(int batteryLevel) {
		long triggerId = -1;
		synchronized (triggerByBatteryLevelCacheMutex) {
			for (Integer bl : triggerByBatteryLevelCache.keySet()) {
				if (bl >= batteryLevel) {
					triggerId = triggerByBatteryLevelCache.get(bl);
				}
			}
		}
		TriggerModel tm = null;
		if (triggerId > -1) {
			tm = getTrigger(triggerId);
		}
		if (tm == null) {
			tm = new TriggerModel();
			tm.setName(ctx.getString(R.string.notAvailable));
		}
		return tm;

	}

	public boolean isProfileUsed(long profileId) {
		String id = Long.toString(profileId);
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT, SELECTION_GET_TRIGGERS_WITH_PROFILE, new String[] { id, id, id },
					DB.Trigger.SORTORDER_DEFAULT);
			return cursor != null && cursor.getCount() > 0;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public boolean isVirtualGovernorUsed(long virtgovId) {
		String id = Long.toString(virtgovId);
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, SELECTION_PROFILE_BY_VIRTGOV, new String[] { id },
					DB.CpuProfile.SORTORDER_DEFAULT);
			return cursor != null && cursor.getCount() > 0;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	@Override
	public Context getContext() {
		return ctx;
	}

	@Override
	public void hasFinished(boolean success) {
		// do nothing
	}

	public void delete(Uri uri) {
		contentResolver.delete(uri, null, null);
		clearCache();
	}

	public void updateProfileFromVirtualGovernor() {
		Cursor c = null;
		try {
			c = contentResolver.query(CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, null, null, null);
			while (c.moveToNext()) {
				ProfileModel profile = new ProfileModel(c);
				VirtualGovernorModel virtualGovernor = getVirtualGovernor(profile.getVirtualGovernor());
				virtualGovernor.applyToProfile(profile);
				updateProfile(profile);
			}
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

}
