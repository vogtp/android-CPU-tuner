package ch.amana.android.cputuner.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

	private static boolean updateTrigger = true;

	private static int lastStateWifi = -1;

	private static int lastStateGps = -1;

	private static int lastStateMobiledata = -1;

	private static int lastStateBluetooth = -1;

	private static int lastStateBackgroundSync = -1;

	// FIXME make singelton class

	private static void resetServiceState() {
		lastStateWifi = -1;
		lastStateGps = -1;
		lastStateMobiledata = -1;
		lastStateBluetooth = -1;
		lastStateBackgroundSync = -1;
	}

	public static void initContext(Context ctx) {
		context = ctx;
		batteryLevel = BatteryHandler.getBatteryLevel();
		acPower = BatteryHandler.isOnAcPower();
		screenOff = false;
		// changeTrigger(true);
	}

	public static void reapplyProfile(boolean force) {
		if (!updateTrigger) {
			return;
		}
		if (force) {
			changeTrigger(force);
		}
		applyPowerProfile(force, force);
	}

	public static void reapplyProfile() {
		applyPowerProfile(true, false);
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
			sendDeviceStatusChangedBroadcast();
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
					applyBackgroundSyncState(currentProfile.getBackgroundSyncState());
					Logger.w("Changed to profile >" + currentProfile.getProfileName() + "> using trigger >" + currentTrigger.getName()
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
					if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
						context.sendBroadcast(new Intent(Notifier.BROADCAST_PROFILE_CHANGED));
					}
				}
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
		}
	}

	private static void applyWifiState(int state) {
		if (state > 0 && SettingsStorage.getInstance().isEnableSwitchWifi()) {
			if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastStateWifi > -1) {
					boolean b = lastStateWifi == 1 ? true : false;
					if (b != ServicesHandler.isWifiEnabaled(context)) {
						Logger.v("Not sitching wifi since it changed since last time");
						return;
					}
				}
				lastStateWifi = state;
			}
			ServicesHandler.enableWifi(context, state == 1 ? true : false);
		}
	}

	private static void applyGpsState(int state) {
		if (state > 0 && SettingsStorage.getInstance().isEnableSwitchGps()) {
			if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastStateGps > -1) {
					boolean b = lastStateGps == 1 ? true : false;
					if (b != ServicesHandler.isGpsEnabled(context)) {
						Logger.v("Not sitching GPS since it changed since last time");
						return;
					}
				}
				lastStateGps = state;
			}
			ServicesHandler.enableGps(context, state == 1 ? true : false);
		}
	}

	private static void applyBluetoothState(int state) {
		if (state > 0 && SettingsStorage.getInstance().isEnableSwitchBluetooth()) {
			if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastStateBluetooth > -1) {
					boolean b = lastStateBluetooth == 1 ? true : false;
					if (b != ServicesHandler.isBlutoothEnabled()) {
						Logger.v("Not sitching bluetooth it changed state since last time");
						return;
					}
				}
				lastStateBluetooth = state;
			}
			ServicesHandler.enableBluetooth(state == 1 ? true : false);
		}
	}

	private static void applyMobiledataState(int state) {
		if (state > 0 && SettingsStorage.getInstance().isEnableSwitchMobiledata()) {
			if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastStateMobiledata > -1) {
					boolean b = lastStateMobiledata == 1 ? true : false;
					if (b != ServicesHandler.is2gOnlyEnabled(context)) {
						Logger.v("Not sitching mobiledata it changed state since last time");
						return;
					}
				}
				lastStateMobiledata = state;
			}
			ServicesHandler.enable2gOnly(context, state == 1 ? true : false);
		}
	}

	private static void applyBackgroundSyncState(int state) {
		if (state > 0 && SettingsStorage.getInstance().isEnableSwitchBackgroundSync()) {
			if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastStateBackgroundSync > -1) {
					boolean b = lastStateBackgroundSync == 1 ? true : false;
					if (b != ServicesHandler.isBackgroundSyncEnabled(context)) {
						Logger.v("Not sitching background sync it changed since state since last time");
						return;
					}
				}
				lastStateBackgroundSync = state;
			}
			ServicesHandler.enableBackgroundSync(context, state == 1 ? true : false);
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
					Logger.i("Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
					if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
						context.sendBroadcast(new Intent(Notifier.BROADCAST_TRIGGER_CHANGED));
					}
					resetServiceState();
					return true;
				} else {
					// no change
					return false;
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		if (SettingsStorage.getInstance().isEnableBeta()) {
			// no trigger found i.e. no trigger with bigger battery level...
			// getting the next best
			try {
				cursor = context.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
						null, null, DB.Trigger.SORTORDER_DEFAULT);
				if (cursor != null && cursor.moveToFirst()) {
					if (force || currentTrigger == null || currentTrigger.getDbId() != cursor.getLong(DB.INDEX_ID)) {
						currentTrigger = new TriggerModel(cursor);
						Logger.i("Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
						if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
							context.sendBroadcast(new Intent(Notifier.BROADCAST_TRIGGER_CHANGED));
						}
						resetServiceState();
						return true;
					}
				}
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
		}
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
				sendDeviceStatusChangedBroadcast();
				trackCurrent();
			}
		}
	}

	private static void sendDeviceStatusChangedBroadcast() {
		if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
			context.sendBroadcast(new Intent(Notifier.BROADCAST_DEVICESTATUS_CHANGED));
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
		// FIXME remove listeners after new switch task is non beta
		if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
			return;
		}
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

	public static TriggerModel getCurrentTrigger() {
		return currentTrigger;
	}

	public static CpuModel getCurrentProfile() {
		return currentProfile;
	}
}
