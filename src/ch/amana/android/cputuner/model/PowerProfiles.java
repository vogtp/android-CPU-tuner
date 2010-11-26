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
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.provider.db.DB;

public class PowerProfiles {

	public static final String UNKNOWN = "Unknown";

	private static Context context;

	private static int batteryLevel;
	private static boolean acPower;
	private static boolean screenOff;

	private static CpuModel currentProfile;
	private static TriggerModel currentTrigger;

	private static List<IProfileChangeCallback> listeners;

	private static boolean updateTrigger = true;;

	// FIXME make singelton class

	public static void initContext(Context ctx) {
		context = ctx;
		batteryLevel = BatteryHandler.getBatteryLevel();
		acPower = BatteryHandler.isOnAcPower();
		screenOff = false;
		changeTrigger(true);
	}

	public static void reapplyProfile(boolean force) {
		if (force) {
			changeTrigger(force);
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
		if (!updateTrigger) {
			return;
		}
		if (!SettingsStorage.getInstance().isEnableProfiles()) {
			if (!ignoreSettings) {
				return;
			}
		}
		if (currentTrigger == null) {
			return;
		}
		// does cross contamination
		// trackCurrent();

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
					Log.w(Logger.TAG, "Changed to profile >" + currentProfile.getProfileName() + "> using trigger >" + currentTrigger.getName()
							+ "< on batterylevel "
							+ batteryLevel + "%");
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

	private static boolean changeTrigger(boolean force) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
					DB.Trigger.NAME_BATTERY_LEVEL + ">=?", new String[] { batteryLevel + "" }, DB.Trigger.SORTORDER_REVERSE);
			if (cursor != null && cursor.moveToFirst()) {
				if (force || currentTrigger == null || currentTrigger.getDbId() != cursor.getLong(DB.INDEX_ID)) {
					currentTrigger = new TriggerModel(cursor);
					Log.i(Logger.TAG, "Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
					return true;
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		// FIXME get a default tigger if none found
		return false;
	}

	public static void setBatteryLevel(int level) {
		if (batteryLevel != level) {
			batteryLevel = level;
			notifyBatteryLevel();
			boolean chagned = changeTrigger(false);
			if (chagned) {
				applyPowerProfile(false, false);
			} else {
				trackCurrent();
			}
		}
	}

	private static void trackCurrent() {
		if (currentTrigger == null || !SettingsStorage.getInstance().isTrackCurrent()) {
			return;
		}
		long powerCurrentSum = 0;
		long powerCurrentCnt = 0;
		if (screenOff) {
			powerCurrentSum = currentTrigger.getPowerCurrentSumScreenLocked();
			powerCurrentCnt = currentTrigger.getPowerCurrentCntScreenLocked();
		} else if (acPower) {
			powerCurrentSum = currentTrigger.getPowerCurrentSumPower();
			powerCurrentCnt = currentTrigger.getPowerCurrentCntPower();
		} else {
			powerCurrentSum = currentTrigger.getPowerCurrentSumBattery();
			powerCurrentCnt = currentTrigger.getPowerCurrentCntBattery();

		}
		// powerCurrentSum *= powerCurrentCnt;
		powerCurrentSum += BatteryHandler.getBatteryCurrentAverage();
		powerCurrentCnt++;
		if (screenOff) {
			currentTrigger.setPowerCurrentSumScreenLocked(powerCurrentSum);
			currentTrigger.setPowerCurrentCntScreenLocked(powerCurrentCnt);
		} else if (acPower) {
			currentTrigger.setPowerCurrentSumPower(powerCurrentSum);
			currentTrigger.setPowerCurrentCntPower(powerCurrentCnt);
		} else {
			currentTrigger.setPowerCurrentSumBattery(powerCurrentSum);
			currentTrigger.setPowerCurrentCntBattery(powerCurrentCnt);
		}
		updateTrigger = false;
		context.getContentResolver().update(DB.Trigger.CONTENT_URI, currentTrigger.getValues(), DB.NAME_ID + "=?",
				new String[] { currentTrigger.getDbId() + "" });
		updateTrigger = true;
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

	public static void setUpdateTrigger(boolean updateTrigger) {
		PowerProfiles.updateTrigger = updateTrigger;
	}

}
