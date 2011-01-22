package ch.amana.android.cputuner.model;

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

	public static final int SERVICE_STATE_LEAVE = 0;
	public static final int SERVICE_STATE_ON = 1;
	public static final int SERVICE_STATE_OFF = 2;
	public static final int SERVICE_STATE_PREV = 3;

	private final Context context;

	private int batteryLevel;
	private int batteryTemperature;
	private boolean acPower;
	private boolean screenOff;
	private boolean batteryHot;

	private ProfileModel currentProfile;
	private TriggerModel currentTrigger;

	private static boolean updateTrigger = true;

	private int lastSetStateWifi = -1;
	private boolean lastAciveStateWifi;

	private int lastSetStateGps = -1;
	private boolean lastActiveStateGps;

	private int lastSetStateMobiledata = -1;
	private boolean lastActiveStateMobileData;

	private int lastSetStateBluetooth = -1;
	private boolean lastActiceStateBluetooth;

	private int lastSetStateBackgroundSync = -1;
	private boolean lastActiveStateBackgroundSync;

	private static PowerProfiles instance;

	public static void initInstance(Context ctx) {
		instance = new PowerProfiles(ctx);
	}

	public static PowerProfiles getInstance() {
		return instance;
	}

	public PowerProfiles(Context ctx) {
		context = ctx;
		batteryLevel = BatteryHandler.getBatteryLevel();
		acPower = BatteryHandler.isOnAcPower();
		screenOff = false;
		initActiveStates();
	}

	public void initActiveStates() {
		lastActiveStateBackgroundSync = ServicesHandler.isBackgroundSyncEnabled(context);
		lastActiceStateBluetooth = ServicesHandler.isBlutoothEnabled();
		lastActiveStateGps = ServicesHandler.isGpsEnabled(context);
		lastActiveStateMobileData = ServicesHandler.is2gOnlyEnabled(context);
		lastAciveStateWifi = ServicesHandler.isWifiEnabaled(context);
	}

	private void resetServiceState() {
		lastSetStateWifi = -1;
		lastSetStateGps = -1;
		lastSetStateMobiledata = -1;
		lastSetStateBluetooth = -1;
		lastSetStateBackgroundSync = -1;
	}

	public void reapplyProfile(boolean force) {
		if (!updateTrigger) {
			return;
		}
		if (force) {
			changeTrigger(force);
		}
		applyPowerProfile(force, force);
	}

	public void reapplyProfile() {
		applyPowerProfile(true, false);
	}

	private void applyPowerProfile(boolean force, boolean ignoreSettings) {
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

		if (isBatteryHot()) {
			profileId = currentTrigger.getHotProfileId();
		} else if (screenOff) {
			profileId = currentTrigger.getScreenOffProfileId();
		} else if (acPower) {
			profileId = currentTrigger.getPowerProfileId();
		}

		if (force || currentProfile == null || currentProfile.getDbId() != profileId) {
			if (!SettingsStorage.getInstance().isSwitchProfileWhilePhoneNotIdle() && !ServicesHandler.isPhoneIdle(context)) {
				Logger.i("Not switching profile since phone not idle");
				return;
			}
			Cursor c = null;
			try {
				c = context.getContentResolver().query(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT,
						DB.NAME_ID + "=?", new String[] { profileId + "" }, DB.CpuProfile.SORTORDER_DEFAULT);
				if (c != null && c.moveToFirst()) {
					currentProfile = new ProfileModel(c);

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
					Notifier.notifyProfile(currentProfile.getProfileName());
					context.sendBroadcast(new Intent(Notifier.BROADCAST_PROFILE_CHANGED));
				}
			} finally {
				if (c != null && !c.isClosed()) {
					try {
						c.close();
					}catch (Exception e) {
						Logger.e("Cannot close cursor",e);
					}
				}
			}
		}
	}

	private void applyWifiState(int state) {
		if (state > SERVICE_STATE_LEAVE && SettingsStorage.getInstance().isEnableSwitchWifi()) {
			boolean stateBefore = lastAciveStateWifi;
			lastAciveStateWifi = ServicesHandler.isWifiEnabaled(context);
			if (state == SERVICE_STATE_PREV) {
				Logger.v("Sitching wifi to last state which was " + stateBefore);
				ServicesHandler.enableWifi(context, stateBefore);
				lastSetStateWifi = -1;
				return;
			} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastSetStateWifi > -1) {
					boolean b = lastSetStateWifi == SERVICE_STATE_ON ? true : false;
					if (b != lastAciveStateWifi) {
						Logger.v("Not sitching wifi since it changed since last time");
						return;
					}
				}
				lastSetStateWifi = state;
			}
			ServicesHandler.enableWifi(context, state == SERVICE_STATE_ON ? true : false);
		}
	}

	private void applyGpsState(int state) {
		if (state > SERVICE_STATE_LEAVE && SettingsStorage.getInstance().isEnableSwitchGps()) {
			boolean stateBefore = lastActiveStateGps;
			lastActiveStateGps = ServicesHandler.isGpsEnabled(context);
			if (state == SERVICE_STATE_PREV) {
				Logger.v("Sitching GPS to last state which was " + stateBefore);
				ServicesHandler.enableGps(context, stateBefore);
				lastSetStateGps = -1;
				return;
			} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastSetStateGps > -1) {
					boolean b = lastSetStateGps == SERVICE_STATE_ON ? true : false;
					if (b != lastActiveStateGps) {
						Logger.v("Not sitching GPS since it changed since last time");
						return;
					}
				}
				lastSetStateGps = state;
			}
			ServicesHandler.enableGps(context, state == SERVICE_STATE_ON ? true : false);
		}
	}

	private void applyBluetoothState(int state) {
		if (state > SERVICE_STATE_LEAVE && SettingsStorage.getInstance().isEnableSwitchBluetooth()) {
			boolean stateBefore = lastActiceStateBluetooth;
			lastActiceStateBluetooth = ServicesHandler.isBlutoothEnabled();
			if (state == SERVICE_STATE_PREV) {
				Logger.v("Sitching bluetooth to last state which was " + stateBefore);
				ServicesHandler.enableBluetooth(stateBefore);
				lastSetStateBluetooth = -1;
				return;
			} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastSetStateBluetooth > -1) {
					boolean b = lastSetStateBluetooth == SERVICE_STATE_ON ? true : false;
					if (b != lastActiceStateBluetooth) {
						Logger.v("Not sitching bluetooth it changed state since last time");
						return;
					}
				}
				lastSetStateBluetooth = state;
			}
			ServicesHandler.enableBluetooth(state == SERVICE_STATE_ON ? true : false);
		}
	}

	private void applyMobiledataState(int state) {
		if (state > SERVICE_STATE_LEAVE && SettingsStorage.getInstance().isEnableSwitchMobiledata()) {
			boolean stateBefore = lastActiveStateMobileData;
			lastActiveStateMobileData = ServicesHandler.is2gOnlyEnabled(context);
			if (state == SERVICE_STATE_PREV) {
				Logger.v("Sitching mobiledata to last state which was " + stateBefore);
				ServicesHandler.enable2gOnly(context, stateBefore);
				lastSetStateMobiledata = -1;
				return;
			} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastSetStateMobiledata > -1) {
					boolean b = lastSetStateMobiledata == SERVICE_STATE_ON ? true : false;
					if (b != lastActiveStateMobileData) {
						Logger.v("Not sitching mobiledata it changed state since last time");
						return;
					}
				}
				lastSetStateMobiledata = state;
			}
			ServicesHandler.enable2gOnly(context, state == SERVICE_STATE_ON ? true : false);
		}
	}

	private void applyBackgroundSyncState(int state) {
		if (state > SERVICE_STATE_LEAVE && SettingsStorage.getInstance().isEnableSwitchBackgroundSync()) {
			boolean stateBefore = lastActiveStateBackgroundSync;
			lastActiveStateBackgroundSync = ServicesHandler.isBackgroundSyncEnabled(context);
			if (state == SERVICE_STATE_PREV) {
				Logger.v("Sitching background sync to last state which was " + stateBefore);
				ServicesHandler.enableBackgroundSync(context, stateBefore);
				lastSetStateBackgroundSync = -1;
				return;
			} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
				if (lastSetStateBackgroundSync > -1) {
					boolean b = lastSetStateBackgroundSync == SERVICE_STATE_ON ? true : false;
					if (b != lastActiveStateBackgroundSync) {
						Logger.v("Not sitching background sync it changed since state since last time");
						return;
					}
				}
				lastSetStateBackgroundSync = state;
			}
			ServicesHandler.enableBackgroundSync(context, state == SERVICE_STATE_ON ? true : false);
		}
	}

	private boolean changeTrigger(boolean force) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
					DB.Trigger.NAME_BATTERY_LEVEL + ">=?", new String[] { batteryLevel + "" }, DB.Trigger.SORTORDER_REVERSE);
			if (cursor != null && cursor.moveToFirst()) {
				if (force || currentTrigger == null || currentTrigger.getDbId() != cursor.getLong(DB.INDEX_ID)) {
					currentTrigger = new TriggerModel(cursor);
					Logger.i("Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
					context.sendBroadcast(new Intent(Notifier.BROADCAST_TRIGGER_CHANGED));
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
		try {
			cursor = context.getContentResolver().query(DB.Trigger.CONTENT_URI, DB.Trigger.PROJECTION_DEFAULT,
						null, null, DB.Trigger.SORTORDER_DEFAULT);
			if (cursor != null && cursor.moveToFirst()) {
				if (force || currentTrigger == null || currentTrigger.getDbId() != cursor.getLong(DB.INDEX_ID)) {
					currentTrigger = new TriggerModel(cursor);
					Logger.i("Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
					context.sendBroadcast(new Intent(Notifier.BROADCAST_TRIGGER_CHANGED));
					resetServiceState();
					return true;
				}
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return false;
	}

	public void setBatteryLevel(int level) {
		if (batteryLevel != level) {
			batteryLevel = level;
			trackCurrent();
			boolean chagned = changeTrigger(false);
			if (chagned) {
				applyPowerProfile(false, false);
			} else {
				sendDeviceStatusChangedBroadcast();
			}
		}
	}

	private void sendDeviceStatusChangedBroadcast() {
		context.sendBroadcast(new Intent(Notifier.BROADCAST_DEVICESTATUS_CHANGED));
	}

	private void trackCurrent() {
		if (currentTrigger == null || SettingsStorage.getInstance().getTrackCurrentType() == SettingsStorage.TRACK_CURRENT_HIDE) {
			return;
		}
		long powerCurrentSum = 0;
		long powerCurrentCnt = 0;
		if (isBatteryHot()) {
			powerCurrentSum = currentTrigger.getPowerCurrentSumHot();
			powerCurrentCnt = currentTrigger.getPowerCurrentCntHot();
		} else if (screenOff) {
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
		switch (SettingsStorage.getInstance().getTrackCurrentType()) {
		case SettingsStorage.TRACK_CURRENT_AVG:
			powerCurrentSum += BatteryHandler.getBatteryCurrentAverage();
			break;

		default:
			powerCurrentSum += BatteryHandler.getBatteryCurrentNow();
			break;
		}
		powerCurrentCnt++;
		if (batteryHot) {
			currentTrigger.setPowerCurrentSumHot(powerCurrentSum);
			currentTrigger.setPowerCurrentCntHot(powerCurrentCnt);
		} else if (screenOff) {
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
		try {
		context.getContentResolver().update(DB.Trigger.CONTENT_URI, currentTrigger.getValues(), DB.NAME_ID + "=?",
				new String[] { currentTrigger.getDbId() + "" });
		} catch(Exception e) {
			Logger.w("Error saving power current information",e);
		}
		updateTrigger = true;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public void setAcPower(boolean power) {
		if (acPower != power) {
			acPower = power;
			sendDeviceStatusChangedBroadcast();
			trackCurrent();
			applyPowerProfile(false, false);
		}
	}

	public void setScreenOff(boolean b) {
		if (screenOff != b) {
			screenOff = b;
			trackCurrent();
			applyPowerProfile(false, false);
		}
	}

	public void setBatteryHot(boolean b) {
		if (batteryHot != b) {
			batteryHot = b;
			trackCurrent();
			applyPowerProfile(false, false);
		}
	}

	public boolean isBatteryHot() {
		return batteryHot || batteryTemperature > SettingsStorage.getInstance().getBatteryHotTemp();
	}

	public boolean isAcPower() {
		return acPower;
	}

	public CharSequence getCurrentProfileName() {
		if (currentProfile == null) {
			return UNKNOWN;
		}
		return currentProfile.getProfileName();
	}

	public CharSequence getCurrentTriggerName() {
		if (currentTrigger == null) {
			return UNKNOWN;
		}
		return currentTrigger.getName();
	}

	public static void setUpdateTrigger(boolean updateTrigger) {
		PowerProfiles.updateTrigger = updateTrigger;
	}

	public TriggerModel getCurrentTrigger() {
		return currentTrigger;
	}

	public ProfileModel getCurrentProfile() {
		return currentProfile;
	}

	public boolean isScreenOff() {
		return screenOff;
	}

	public void setBatteryTemperature(int temperature) {
		if (batteryTemperature != temperature) {
			batteryTemperature = temperature;
			sendDeviceStatusChangedBroadcast();
		}
	}

	public int getBatteryTemperature() {
		return batteryTemperature;
	}

}
