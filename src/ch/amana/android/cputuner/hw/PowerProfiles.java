package ch.amana.android.cputuner.hw;

import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.TriggerModel;

public class PowerProfiles {

	public enum ServiceType {
		wifi, mobiledata3g, gps, bluetooth, mobiledataConnection, backgroundsync, airplainMode
	}

	public static final TriggerModel DUMMY_TRIGGER = new TriggerModel();

	public static final ProfileModel DUMMY_PROFILE = new ProfileModel();

	public static final String UNKNOWN = "Unknown";

	private static final int NO_STATE = -1;

	public static final int SERVICE_STATE_LEAVE = 0;
	public static final int SERVICE_STATE_ON = 1;
	public static final int SERVICE_STATE_OFF = 2;
	public static final int SERVICE_STATE_PREV = 3;
	public static final int SERVICE_STATE_PULSE = 4;

	public static final int SERVICE_STATE_2G = SERVICE_STATE_ON;
	public static final int SERVICE_STATE_2G_3G = SERVICE_STATE_OFF;
	public static final int SERVICE_STATE_3G = 4;

	public static final long AUTOMATIC_PROFILE = -1;

	private static final long MILLIES_TO_HOURS = 1000 * 60 * 60;

	private final Context context;

	private int batteryLevel;
	private int batteryTemperature;
	private boolean acPower;
	private boolean screenOff;
	private boolean batteryHot;

	private ProfileModel currentProfile;
	private TriggerModel currentTrigger;

	private static boolean updateTrigger = true;

	private int lastStateWifi = NO_STATE;
	private int lastStateGps = NO_STATE;
	private int lastStateMobiledataConnection = NO_STATE;
	private int lastStateBluetooth = NO_STATE;
	private int lastStateBackgroundSync = NO_STATE;
	private int lastStateAirplaneMode = NO_STATE;
	private int lastStateMobiledata3G = NO_STATE;

	private boolean callInProgress = false;

	private int lastBatteryLevel = -1;

	private long lastBatteryLevelTimestamp = -1;

	private static PowerProfiles instance;

	private final ModelAccess modelAccess;

	private long manualProfileID = AUTOMATIC_PROFILE;

	private boolean wifiManaged3gState = false;

	public static PowerProfiles getInstance(Context ctx) {
		if (instance == null) {
			instance = new PowerProfiles(ctx.getApplicationContext());
		}
		return instance;
	}

	public static PowerProfiles getInstance() {
		return instance;
	}

	public PowerProfiles(Context ctx) {
		context = ctx;
		modelAccess = ModelAccess.getInstace(ctx);
		BatteryHandler batteryHandler = BatteryHandler.getInstance();
		batteryLevel = batteryHandler.getBatteryLevel();
		acPower = batteryHandler.isOnAcPower();
		screenOff = false;
		initActiveStates();
		reapplyProfile(true);
	}

	public void initActiveStates() {
		manualProfileID = AUTOMATIC_PROFILE;
		lastStateBackgroundSync = ServicesHandler.isBackgroundSyncEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
		lastStateBluetooth = ServicesHandler.isBlutoothEnabled() ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
		lastStateGps = ServicesHandler.isGpsEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
		lastStateMobiledataConnection = ServicesHandler.isMobiledataConnectionEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
		lastStateMobiledata3G = ServicesHandler.whichMobiledata3G(context);
		lastStateWifi = ServicesHandler.isWifiEnabaled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
		lastStateAirplaneMode = ServicesHandler.isAirplaineModeEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF;
	}

	public void reapplyProfile(boolean force) {
		if (!updateTrigger) {
			return;
		}
		if (force) {
			changeTrigger(force);
		}
		applyPowerProfile(force);
	}

	public void reapplyProfile() {
		applyPowerProfile(true);
	}

	private void applyPowerProfile(boolean force) {
		if (!updateTrigger) {
			return;
		}
		if (currentTrigger == null) {
			sendDeviceStatusChangedBroadcast();
			return;
		}

		long profileId = getCurrentAutoProfileId();

		if (force || (currentProfile != null && currentProfile.getDbId() != profileId)) {
			if (!callInProgress && !SettingsStorage.getInstance().isSwitchProfileWhilePhoneNotIdle() && !ServicesHandler.isPhoneIdle(context)) {
				Logger.i("Not switching profile since phone not idle");
				return;
			}
			applyProfile(profileId, force);
		}
	}

	public long getCurrentAutoProfileId() {
		long profileId = currentTrigger.getBatteryProfileId();

		if (callInProgress) {
			profileId = currentTrigger.getCallInProgessProfileId();
		} else if (isBatteryHot()) {
			profileId = currentTrigger.getHotProfileId();
		} else if (screenOff) {
			profileId = currentTrigger.getScreenOffProfileId();
		} else if (acPower) {
			profileId = currentTrigger.getPowerProfileId();
		}
		return profileId;
	}

	public void applyProfile(long profileId) {
		applyProfile(profileId, false);
	}

	private void applyProfile(long profileId, boolean force) {
		if (isManualProfile()) {
			Logger.i("Setting profile to its manually chosen.");
			profileId = manualProfileID;
		}
		if (currentProfile != null && currentProfile.getDbId() == profileId) {
			if (!force) {
				Logger.i("Not switching profile since it is the correct one " + currentProfile.getProfileName());
				return;
			}
		}

		try {
			currentProfile = modelAccess.getProfile(profileId);

			if (currentProfile == ProfileModel.NO_PROFILE) {
				Logger.i("no profile found");
				return;
			}

			SettingsStorage settings = SettingsStorage.getInstance();

			if (settings.getProfileSwitchLogSize() > 0) {
				updateProfileSwitchLog();
			}

			CpuHandler cpuHandler = CpuHandler.getInstance();
			cpuHandler.applyCpuSettings(currentProfile);
			applyWifiState(currentProfile.getWifiState());
			applyGpsState(currentProfile.getGpsState());
			applyBluetoothState(currentProfile.getBluetoothState());
			applyMobiledata3GState(currentProfile.getMobiledata3GState());
			applyMobiledataConnectionState(currentProfile.getMobiledataConnectionState());
			applyBackgroundSyncState(currentProfile.getBackgroundSyncState());
			applyAirplanemodeState(currentProfile.getAirplainemodeState());
			try {
				Logger.w("Changed to profile >" + currentProfile.getProfileName() + "< using trigger >" + currentTrigger.getName() + "< on batterylevel " + batteryLevel + "%");
			} catch (Exception e) {
				Logger.w("Error printing switch profile", e);
			}
			StringBuilder sb = new StringBuilder(50);
			sb.append("Setting power profile to ");
			sb.append(currentProfile.getProfileName());
			context.sendBroadcast(new Intent(Notifier.BROADCAST_PROFILE_CHANGED));
		} catch (Throwable e) {
			Logger.e("Failure while appling a profile", e);
		}
	}

	private void updateProfileSwitchLog() {
		StringBuilder sb = new StringBuilder();
		sb.append(currentTrigger.getName()).append(" -> ");
		sb.append(currentProfile.getProfileName());
		Logger.addToLog(sb.toString());
	}

	private String getServiceTypeName(ServiceType type) {
		return type.toString();
		//		switch (type) {wifi, mobiledata3g, gps, bluetooth, mobiledataConnection, backgroundsync, airplainMode
		//		case wifi:
		//			return "wifi";
		//		case mobiledata3g:
		//			return "mobiledata 2/3G";
		//		case bluetooth:
		//			return "bluetooth";
		//		case mobiledataConnection:
		//			return "mobiledataConnection";
		//		case backgroundsync:
		//			return "backgroundsync";
		//		case airplainMode:
		//			return "airplainMode";
		//		case gps:
		//			return "gps";
		//		default:
		//			return "service type "+type.t;
		//		}
	}

	private int evaluateState(ServiceType type, int state, int lastState, int stateNow) {
		int ret = state;
		if (type != ServiceType.mobiledata3g) {
			// FIXME handle manual service changes in pluse
			if (state == SERVICE_STATE_PULSE || lastState == SERVICE_STATE_PULSE) {
				PulseHelper.getInstance(context).pulse(type, true);
				return NO_STATE;
			} else {
				PulseHelper.getInstance(context).pulse(type, false);
			}
		} else {
			if (ServicesHandler.isWifiConnected(context)) {
				ret = SettingsStorage.getInstance().getNetworkStateOnWifi();
			}
		}
		if (state == SERVICE_STATE_LEAVE) {
			return NO_STATE;
		}
		if (state == SERVICE_STATE_PREV) {
			Logger.v("Switching " + getServiceTypeName(type) + "  to last state which was " + lastState);
			ret = lastState;
		} else if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
			if (stateNow != lastState) {
				Logger.v("Not switching " + getServiceTypeName(type) + " since it changed since last time");
				return NO_STATE;
			}
		}
		lastState = ret;
		return ret;
	}

	private void applyWifiState(int state) {
		if (SettingsStorage.getInstance().isEnableSwitchWifi()) {
			int newState = evaluateState(ServiceType.wifi, state, lastStateWifi, ServicesHandler.isWifiEnabaled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableWifi(context, newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private void applyGpsState(int state) {
		if (SettingsStorage.getInstance().isEnableSwitchGps()) {
			int newState = evaluateState(ServiceType.gps, state, lastStateGps, ServicesHandler.isGpsEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableGps(context, newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private void applyBluetoothState(int state) {
		if (SettingsStorage.getInstance().isEnableSwitchBluetooth()) {
			int newState = evaluateState(ServiceType.bluetooth, state, lastStateBluetooth, ServicesHandler.isBlutoothEnabled() ? SERVICE_STATE_ON
					: SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableBluetooth(newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private void applyMobiledata3GState(int state) {
		if (wifiManaged3gState) {
			return;
		}
		if (SettingsStorage.getInstance().isEnableSwitchMobiledata3G()) {
			int newState = evaluateState(ServiceType.mobiledata3g, state, lastStateMobiledata3G, ServicesHandler.whichMobiledata3G(context));
			if (newState != NO_STATE) {
				ServicesHandler.enable2gOnly(context, newState);
			}
		}
	}

	private void applyMobiledataConnectionState(int state) {
		if (SettingsStorage.getInstance().isEnableSwitchMobiledataConnection()) {
			int newState = evaluateState(ServiceType.mobiledataConnection, state, lastStateMobiledataConnection,
					ServicesHandler.isMobiledataConnectionEnabled(context) ? SERVICE_STATE_ON
							: SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableMobileData(context, newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private void applyBackgroundSyncState(int state) {
		if (SettingsStorage.getInstance().isEnableSwitchBackgroundSync()) {
			int newState = evaluateState(ServiceType.backgroundsync, state, lastStateBackgroundSync,
					ServicesHandler.isBackgroundSyncEnabled(context) ? SERVICE_STATE_ON : SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableBackgroundSync(context, newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private void applyAirplanemodeState(int state) {
		if (SettingsStorage.getInstance().isEnableAirplaneMode()) {
			int newState = evaluateState(ServiceType.airplainMode, state, lastStateAirplaneMode,
					ServicesHandler.isAirplaineModeEnabled(context) ? SERVICE_STATE_ON
							: SERVICE_STATE_OFF);
			if (newState != NO_STATE) {
				ServicesHandler.enableAirplaneMode(context, newState == SERVICE_STATE_ON ? true : false);
			}
		}
	}

	private boolean changeTrigger(boolean force) {
		TriggerModel trigger = modelAccess.getTriggerByBatteryLevel(batteryLevel);
		if (!force && trigger.getDbId() == currentTrigger.getDbId()) {
			return false;
		}
		currentTrigger = trigger;
		Logger.i("Changed to trigger " + currentTrigger.getName() + " since batterylevel is " + batteryLevel);
		context.sendBroadcast(new Intent(Notifier.BROADCAST_TRIGGER_CHANGED));
		initActiveStates();
		PulseHelper.stopPulseService(context);
		return true;
	}

	public void setBatteryLevel(int level) {
		if (batteryLevel != level) {
			batteryLevel = level;
			trackCurrent();
			boolean chagned = changeTrigger(false);
			if (chagned) {
				applyPowerProfile(false);
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
		if (callInProgress) {
			powerCurrentSum = currentTrigger.getPowerCurrentSumCall();
			powerCurrentCnt = currentTrigger.getPowerCurrentCntCall();
		} else if (isBatteryHot()) {
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

		if (powerCurrentSum > Long.MAX_VALUE / 2) {
			powerCurrentSum = powerCurrentSum / 2;
			powerCurrentCnt = powerCurrentCnt / 2;
		}

		// powerCurrentSum *= powerCurrentCnt;
		switch (SettingsStorage.getInstance().getTrackCurrentType()) {
		case SettingsStorage.TRACK_CURRENT_AVG:
			powerCurrentSum += BatteryHandler.getInstance().getBatteryCurrentAverage();
			break;

		case SettingsStorage.TRACK_BATTERY_LEVEL:
			if (lastBatteryLevel != batteryLevel) {
				if (lastBatteryLevelTimestamp != -1) {
					long deltaBat = lastBatteryLevel - batteryLevel;
					long deltaT = System.currentTimeMillis() - lastBatteryLevelTimestamp;
					if (deltaBat > 0 && deltaT > 0) {
						double db = (double) deltaBat / (double) deltaT;
						db = db * MILLIES_TO_HOURS;
						if (powerCurrentCnt > 0) {
							powerCurrentCnt = 2;
						} else {
							powerCurrentCnt = 0;
						}
						powerCurrentCnt = 2 * powerCurrentSum + Math.round(db);
					}
				}
				lastBatteryLevel = batteryLevel;
				lastBatteryLevelTimestamp = System.currentTimeMillis();
			}
			break;

		default:
			powerCurrentSum += BatteryHandler.getInstance().getBatteryCurrentNow();
			break;
		}
		powerCurrentCnt++;
		if (callInProgress) {
			currentTrigger.setPowerCurrentSumCall(powerCurrentSum);
			currentTrigger.setPowerCurrentCntCall(powerCurrentCnt);
		} else if (batteryHot) {
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
			synchronized (ModelAccess.triggerCacheMutex) {

				modelAccess.updateTrigger(currentTrigger, false);
			}

		} catch (Exception e) {
			Logger.w("Error saving power current information", e);
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
			applyPowerProfile(false);
		}
	}

	public void setScreenOff(boolean b) {
		if (screenOff != b) {
			screenOff = b;
			trackCurrent();
			applyPowerProfile(false);
		}
	}

	public void setBatteryHot(boolean b) {
		if (batteryHot != b) {
			batteryHot = b;
			trackCurrent();
			applyPowerProfile(false);
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
		if (currentTrigger == null) {
			currentTrigger = DUMMY_TRIGGER;
		}
		return currentTrigger;
	}

	public ProfileModel getCurrentProfile() {
		if (currentProfile == null) {
			currentProfile = DUMMY_PROFILE;
		}
		return currentProfile;
	}

	public boolean isScreenOff() {
		return screenOff;
	}

	public void setBatteryTemperature(int temperature) {
		if (batteryTemperature != temperature) {
			batteryTemperature = temperature;
			sendDeviceStatusChangedBroadcast();
			applyPowerProfile(false);
		}
	}

	public int getBatteryTemperature() {
		return batteryTemperature;
	}

	public void setCallInProgress(boolean b) {
		if (callInProgress != b) {
			callInProgress = b;
			sendDeviceStatusChangedBroadcast();
			applyPowerProfile(false);
		}
	}

	public void setWifiConnected(boolean wifiConnected) {
		int state = SettingsStorage.getInstance().getNetworkStateOnWifi();
		if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
			wifiManaged3gState = false;
			return;
		}
		if (wifiConnected) {
			wifiManaged3gState = false;
			applyMobiledata3GState(state);
			wifiManaged3gState = true;
		} else {
			wifiManaged3gState = false;
			if (currentProfile != null) {
				applyMobiledata3GState(currentProfile.getMobiledata3GState());
			}
		}
	}

	public boolean isManualProfile() {
		return manualProfileID != AUTOMATIC_PROFILE;
	}

	public void setManualProfile(long manualProfileID) {
		this.manualProfileID = manualProfileID;
		applyProfile(manualProfileID);
	}

}
