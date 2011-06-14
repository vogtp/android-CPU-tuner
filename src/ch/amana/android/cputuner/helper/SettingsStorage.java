package ch.amana.android.cputuner.helper;

import java.text.SimpleDateFormat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import ch.amana.android.cputuner.hw.GpsHandler;
import ch.amana.android.cputuner.hw.RootHandler;

public class SettingsStorage {

	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

	private static final String PREF_KEY_USER_LEVEL = "prefKeyUserLevel";
	private static final String PREF_KEY_USER_LEVEL_SET = "prefKeyUserLevelSet";
	public static final String NO_VALUE = "noValue";
	public static final String ENABLE_PROFILES = "prefKeyEnableProfiles";
	public static final String ENABLE_STATUSBAR_ADDTO = "prefKeyStatusbarAddTo";
	public static final String ENABLE_STATUSBAR_NOTI = "prefKeyStatusbarNotifications";

	public static final int NO_BATTERY_HOT_TEMP = 5000;

	public static final int TRACK_CURRENT_AVG = 1;
	public static final int TRACK_CURRENT_CUR = 2;
	public static final int TRACK_CURRENT_HIDE = 3;
	public static final int TRACK_BATTERY_LEVEL = 4;

	private static final String PREF_DEFAULT_PROFILES_VERSION = "prefKeyDefaultProfileVersion";
	private static final String PREF_KEY_USE_VIRTUAL_GOVS = "prefKeyUseVirtualGovernors";

	private static final String PREF_KEY_CONFIGURATION = "prefKeyConfiguration";

	private static SettingsStorage instance;
	private final Context context;
	private boolean checkedBluetooth = false;
	private boolean enableSwitchBluetooth;
	private boolean checkedGps = false;
	private boolean enableSwitchGps;
	private boolean checkedBeta = false;
	private boolean enableBeta;
	private boolean checkedProfiles = false;
	private boolean enableProfiles;
	private int trackCurrent = -1;
	private boolean checkedStatusbarNotifications = false;
	private boolean statusbarNotifications;
	private boolean allowManualServiceChanges;
	private boolean checkedAllowManualServiceChanges = false;
	private boolean checkUserLevel = false;
	private boolean checkedSwitchWifiOnConnectedNetwork = false;
	private boolean checkedSwitchProfileWhilePhoneNotIdle = false;
	private boolean checkBatteryHotTemp = false;
	int userLevel;
	private boolean switchWifiOnConnectedNetwork;
	private boolean switchProfileWhilePhoneNotIdle;
	private int batteryHotTemp;
	private boolean enableCallInProgress;
	private boolean checkedenableCallInProgress = false;
	private boolean checkedPulseDelayOn = false;
	private long pulseDelayOn;
	private boolean checkedPulseDelayOff = false;
	private long pulseDelayOff;
	private boolean checkedEnableUserspaceGovernor = false;
	private boolean enableUserspaceGovernor;
	private boolean checkedProfileSwitchLogSize = false;
	private int profileSwitchLogSize;
	private String configuration;

	public void forgetValues() {
		checkedBeta = false;
		checkedProfiles = false;
		trackCurrent = -1;
		checkedStatusbarNotifications = false;
		checkedAllowManualServiceChanges = false;
		checkUserLevel = false;
		checkedSwitchWifiOnConnectedNetwork = false;
		checkedSwitchProfileWhilePhoneNotIdle = false;
		checkBatteryHotTemp = false;
		checkedenableCallInProgress = false;
		checkedPulseDelayOn = false;
		checkedPulseDelayOff = false;
		checkedEnableUserspaceGovernor = false;
		checkedProfileSwitchLogSize = false;
	}

	public static void initInstance(Context ctx) {
		if (instance == null) {
			instance = new SettingsStorage(ctx);
		}
	}

	public static SettingsStorage getInstance() {
		return instance;
	}

	protected SettingsStorage(Context ctx) {
		super();
		context = ctx;
		if (getPreferences().contains("prefKeyPowerUser")) {
			Editor editor = getPreferences().edit();
			if (getPreferences().getBoolean("prefKeyPowerUser", false)) {
				editor.putString(PREF_KEY_USER_LEVEL, "3");
			} else {
				editor.putString(PREF_KEY_USER_LEVEL, "2");
			}
			editor.remove("prefKeyPowerUser");
			editor.commit();
		}
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isEnableProfiles() {
		if (!checkedProfiles) {
			checkedProfiles = true;
			enableProfiles = getPreferences().getBoolean(ENABLE_PROFILES, true);
		}
		return enableProfiles;
	}

	public boolean isStatusbarAddto() {
		return getPreferences().getBoolean(ENABLE_STATUSBAR_ADDTO, true);
	}

	public boolean isStatusbarNotifications() {
		if (!checkedStatusbarNotifications) {
			checkedStatusbarNotifications = true;
			statusbarNotifications = getPreferences().getBoolean(ENABLE_STATUSBAR_NOTI, true);
		}
		return statusbarNotifications;
	}

	public boolean isEnableBeta() {
		if (!checkedBeta) {
			checkedBeta = true;
			enableBeta = "speedup".equals((getPreferences().getString("prefKeyEnableBeta", "").trim()));
		}
		return enableBeta;
	}

	public void setUserLevel(int level) {
		checkUserLevel = false;
		Editor editor = getPreferences().edit();
		editor.putString(PREF_KEY_USER_LEVEL, Integer.toString(level));
		editor.putBoolean(PREF_KEY_USER_LEVEL_SET, true);
		editor.commit();
	}
	
	public boolean isUserLevelSet() {
		return getPreferences().getBoolean(PREF_KEY_USER_LEVEL_SET, false);
	}
	
	public int getUserLevel() {
		if (!checkUserLevel) {
			checkUserLevel = true;
			try {
				userLevel = Integer.parseInt(getPreferences().getString(PREF_KEY_USER_LEVEL, "2"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyUserLevel as int", e);
				userLevel = 2;
			}
		}
		return userLevel;
	}

	public int getTrackCurrentType() {
		if (trackCurrent < 0) {
			String trackCurrentStr = getPreferences().getString("prefKeyCalcPowerUsageType", "1");
			try {
				trackCurrent = Integer.parseInt(trackCurrentStr);
			} catch (Exception e) {
				Logger.w("Cannot parse prefKeyCalcPowerUsage as int", e);
				trackCurrent = 1;
			}
		}
		return trackCurrent;
	}

	public boolean isEnableSwitchMobiledataConnection() {
		return true;
	}

	public boolean isEnableSwitchMobiledata3G() {
		return true;
	}

	public boolean isEnableSwitchBackgroundSync() {
		return true;
	}

	public boolean isEnableSwitchBluetooth() {
		if (!checkedBluetooth) {
			checkedBluetooth = true;
			enableSwitchBluetooth = BluetoothAdapter.getDefaultAdapter() != null;
		}
		return enableSwitchBluetooth;
	}

	public boolean isEnableSwitchGps() {
		if (!checkedGps) {
			checkedGps = true;
			enableSwitchGps = GpsHandler.isEnableSwitchGps(context);
		}
		return enableSwitchGps;
	}

	public boolean isEnableSwitchWifi() {
		// TODO check if wifi is present
		return true;
	}

	public String getCpuFreqs() {
		return getPreferences().getString("prefKeyCpuFreq", "");
	}

	public boolean isAllowManualServiceChanges() {
		if (!checkedAllowManualServiceChanges) {
			checkedAllowManualServiceChanges = true;
			allowManualServiceChanges = getPreferences().getBoolean("prefKeyAllowManualServiceChanges", false);
		}
		return allowManualServiceChanges;
	}

	public boolean isInstallAsSystemAppEnabled() {
		return RootHandler.isSystemApp(context) || (isEnableBeta() && isPowerUser());
	}

	public int getMinimumSensibeFrequency() {
		try {
			return Integer.parseInt(getPreferences().getString("prefKeyMinSensibleFrequency", "400"));
		} catch (NumberFormatException e) {
			Logger.w("Error parsing fot MinimumSensibeFrequency ", e);
			return 400;
		}
	}

	public boolean isBeginnerUser() {
		return getUserLevel() == 1;
	}

	public boolean isPowerUser() {
		return getUserLevel() > 2;
	}

	public boolean isSwitchWifiOnConnectedNetwork() {
		if (!checkedSwitchWifiOnConnectedNetwork) {
			checkedSwitchWifiOnConnectedNetwork = true;
			switchWifiOnConnectedNetwork = getPreferences().getBoolean("prefKeySwitchWifiOnConnectedNetwork", false);
		}
		return switchWifiOnConnectedNetwork;
	}

	public boolean isSwitchProfileWhilePhoneNotIdle() {
		if (!checkedSwitchProfileWhilePhoneNotIdle) {
			checkedSwitchProfileWhilePhoneNotIdle = true;
			switchProfileWhilePhoneNotIdle = getPreferences().getBoolean("prefKeySwitchProfileWhilePhoneNotIdle", false);
		}
		return switchProfileWhilePhoneNotIdle;
	}

	public int getBatteryHotTemp() {

		if (!checkBatteryHotTemp) {
			checkBatteryHotTemp = true;
			try {
				batteryHotTemp = Integer.parseInt(getPreferences().getString("prefKeyBatteryHotTemp", NO_BATTERY_HOT_TEMP + ""));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyUserLevel as int", e);
				batteryHotTemp = NO_BATTERY_HOT_TEMP;
			}
		}
		return batteryHotTemp;
	}

	public int getDefaultProfilesVersion() {
		return getPreferences().getInt(PREF_DEFAULT_PROFILES_VERSION, 0);
	}

	public void setDefaultProfilesVersion(int version) {
		Editor editor = getPreferences().edit();
		editor.putInt(PREF_DEFAULT_PROFILES_VERSION, version);
		editor.commit();
	}

	public boolean isEnableCallInProgressProfile() {
		if (!checkedenableCallInProgress) {
			checkedenableCallInProgress = true;
			enableCallInProgress = getPreferences().getBoolean("prefKeyCallInProgressProfile", true);
		}
		return enableCallInProgress;
	}

	public long getPulseDelayOn() {

		if (!checkedPulseDelayOn) {
			checkedPulseDelayOn = true;
			try {
				pulseDelayOn = Long.parseLong(getPreferences().getString("prefKeyPulseDelayOn", "1"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse pulseDelayOn as int", e);
				pulseDelayOn = 1;
			}
		}
		return pulseDelayOn;
	}

	public long getPulseDelayOff() {

		if (!checkedPulseDelayOff) {
			checkedPulseDelayOff = true;
			try {
				pulseDelayOff = Long.parseLong(getPreferences().getString("prefKeyPulseDelayOff", "30"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse pulseDelayOn as int", e);
				pulseDelayOff = 1;
			}
		}
		return pulseDelayOff;
	}

	public boolean isEnableUserspaceGovernor() {
		if (!checkedEnableUserspaceGovernor) {
			checkedEnableUserspaceGovernor = true;
			enableUserspaceGovernor = getPreferences().getBoolean("prefKeyEnableUserspaceGovernor", false);
		}
		return enableUserspaceGovernor;
	}

	public boolean isEnableScriptOnProfileChange() {
		return isPowerUser();
	}

	public String getLanguage() {
		return getPreferences().getString("prefKeyLanguage", "");
	}
	
	public boolean isPulseMobiledataOnWifi() {
		return getPreferences().getBoolean("prefKeyPulseMobiledataOnWifi", true);
	}
		
	public boolean isUseVirtualGovernors() {
		return getPreferences().getBoolean(PREF_KEY_USE_VIRTUAL_GOVS, true);
	}

	public void setUseVirtualGovernors(boolean b) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(PREF_KEY_USE_VIRTUAL_GOVS, b);
		editor.commit();
	}

	public boolean isEnableAirplaneMode() {
		return true;
	}

	public boolean is24Hour() {
		// TODO Auto-generated method stub
		return true;
	}

	public int getProfileSwitchLogSize() {
		if (!checkedProfileSwitchLogSize) {
			checkedProfileSwitchLogSize = true;
			try {
				profileSwitchLogSize = Integer.parseInt(getPreferences().getString("prefKeyProfileSwitchLogSize", "10"));
			} catch (NumberFormatException e) {
				Logger.w("Cannot parse prefKeyProfileSwitchLogSize as int", e);
				profileSwitchLogSize = 10;
			}
		}
		return profileSwitchLogSize;
	}

	public void setCurrentConfiguration(String configuration) {
		Editor edit = getPreferences().edit();
		edit.putString(PREF_KEY_CONFIGURATION, configuration);
		edit.commit();
	}


	public String getCurrentConfiguration() {
		return getPreferences().getString(PREF_KEY_CONFIGURATION, null);
	}

	public SimpleDateFormat getSimpledateformat() {
		return simpleDateFormat;
	}

	public boolean isSaveConfiguration() {
		return getPreferences().getBoolean("prefKeySaveConfigOnSwitch", true);
	}

	public boolean hasCurrentConfiguration() {
		String config = getCurrentConfiguration();
		return config != null && !config.trim().equals("");
	}

	public boolean useMulticore() {
		return isEnableBeta();
	}
}
