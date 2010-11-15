package ch.amana.android.cputuner.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.provider.db.DB;

public class PowerProfiles {

	public static final String UNKNOWN = "Unknown";

	private static Context context;

	private static int batteryLevel = 80;
	private static boolean acPower = false;
	private static boolean screenOff = false;

	private static CpuModel currentProfile;
	private static TriggerModel currentTrigger;

	private static List<IProfileChangeCallback> listeners;

	public static void initContext(Context ctx) {
		context = ctx;
	}

	public static void reapplyProfile(boolean force) {
		if (force) {
			changeTrigger();
		}
		applyPowerProfile(force, force);
	}

	public static void reapplyProfile() {
		// FIXME
		// if (currentProfile.equals("")) {
		applyPowerProfile(true, false);
		// }
	}

	private static void applyPowerProfile(boolean force, boolean ignoreSettings) {
		if (!SettingsStorage.getInstance().isEnableProfiles()) {
			if (!ignoreSettings) {
				return;
			}
		}
		if (currentTrigger == null) {
			return;
		}

		long profileId = currentTrigger.getBatteryProfileId();
		if (screenOff) {
			profileId = currentTrigger.getScreenOffProfileId();
		} else if (acPower) {
			profileId = currentTrigger.getPowerProfileId();
		}

		if (force || currentProfile == null || currentProfile.getDbId() != profileId) {
			Cursor c = null;
			try {
				c = context.getContentResolver().query(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT,
						DB.NAME_ID + "=?", new String[] { profileId + "" }, DB.CpuProfile.SORTORDER_DEFAULT);
				if (c != null && c.moveToFirst()) {
					currentProfile = new CpuModel(c);

					CpuHandler cpuHandler = new CpuHandler();
					cpuHandler.applyCpuSettings(currentProfile);
					applyWifiState(currentProfile.getWifiState());
					applyGpsState(currentProfile.getGpsState());
					applyBluetoothState(currentProfile.getBluetoothState());
					applyMobiledataState(currentProfile.getMobiledataState());
					Log.i(Logger.TAG, "Changed to profile " + currentProfile.getProfileName() + " using trigger " + currentTrigger.getName()
							+ " on batterylevel "
							+ batteryLevel);
					StringBuilder sb = new StringBuilder(50);
					if (force) {
						sb.append("Reappling power profile ");
					} else {
						sb.append("Setting power profile to ");
					}
					sb.append(currentProfile.getProfileName());
					notifyProfile();
					Notifier.notify(context, sb.toString(), 1);
					Notifier.notifyProfile(currentProfile.getProfileName());
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
		}
	}

	private static void applyWifiState(int state) {
		if (state > 0) {
			ServicesHandler.wifi(context, state == 1 ? true : false);
		}
	}

	private static void applyGpsState(int state) {
		if (state > 0) {
			ServicesHandler.gps(context, state == 1 ? true : false);
		}
	}

	private static void applyBluetoothState(int state) {
		if (state > 0) {
			ServicesHandler.bluetooth(context, state == 1 ? true : false);
		}
	}

	private static void applyMobiledataState(int state) {
		if (state > 0) {
			ServicesHandler.mobiledata(context, state == 1 ? true : false);
		}
	}

	private static void changeTrigger() {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
					DB.Trigger.NAME_BATTERY_LEVEL + ">?", new String[] { batteryLevel + "" }, DB.Trigger.SORTORDER_REVERSE);
			if (cursor != null && cursor.moveToFirst()) {
				if (currentTrigger == null || currentTrigger.getDbId() != cursor.getLong(DB.INDEX_ID)) {
					currentTrigger = new TriggerModel(cursor);
					Log.i(Logger.TAG, "Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public static void setBatteryLevel(int level) {
		if (batteryLevel != level) {
			batteryLevel = level;
			notifyBatteryLevel();
			changeTrigger();
			applyPowerProfile(false, false);
		}
	}

	public static int getBatteryLevel() {
		return batteryLevel;
	}

	public static void setAcPower(boolean power) {
		if (acPower != power) {
			acPower = power;
			notifyAcPower();
			applyPowerProfile(false, false);
		}
	}

	public static void setScreenOff(boolean b) {
		if (screenOff != b) {
			screenOff = b;
			applyPowerProfile(false, false);
		}
	}

	public static boolean getAcPower() {
		return acPower;
	}

	public static CharSequence getCurrentProfileName() {
		if (currentProfile == null) {
			return UNKNOWN;
		}
		return currentProfile.getProfileName();
	}

	public static CharSequence getCurrentTriggerName() {
		if (currentTrigger == null) {
			return UNKNOWN;
		}
		return currentTrigger.getName();
	}

	public static void registerCallback(IProfileChangeCallback callback) {
		if (listeners == null) {
			listeners = new ArrayList<IProfileChangeCallback>();
		}
		listeners.add(callback);
	}

	public static void unregisterCallback(IProfileChangeCallback callback) {
		if (listeners == null) {
			return;
		}
		listeners.remove(callback);
	}

	private static void notifyBatteryLevel() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.batteryLevelChanged();
		}
	}

	private static void notifyProfile() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.profileChanged();
		}
	}

	private static void notifyAcPower() {
		if (listeners == null) {
			return;
		}
		for (Iterator<IProfileChangeCallback> iterator = listeners.iterator(); iterator.hasNext();) {
			IProfileChangeCallback callback = iterator.next();
			callback.acPowerChanged();
		}
	}

}
