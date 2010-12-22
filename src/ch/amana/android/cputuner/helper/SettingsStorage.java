package ch.amana.android.cputuner.helper;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import ch.amana.android.cputuner.hw.GpsHandler;
import ch.amana.android.cputuner.hw.RootHandler;

public class SettingsStorage {

	public static final String NO_VALUE = "noValue";
	private static final String APPLY_ON_BOOT = "applyCpuSettingsOnBoot";
	public static final String ENABLE_PROFILES = "prefKeyEnableProfiles";
	public static final String ENABLE_STATUSBAR_ADDTO = "prefKeyStatusbarAddTo";
	public static final String ENABLE_STATUSBAR_NOTI = "prefKeyStatusbarNotifications";
	public static final String ENABLE_TOAST_NOTI = "prefKeyToastNotifications";

	public static final int TRACK_CURRENT_AVG = 1;
	public static final int TRACK_CURRENT_CUR = 2;
	public static final int TRACK_CURRENT_HIDE = 3;

	private static final String DISABLE_DISPLAY_ISSUES = "prefKeyDisplayIssues";
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
	private boolean checkedToastNotifications = false;
	private boolean toastNotifications;
	private boolean allowManualServiceChanges;
	private boolean checkedAllowManualServiceChanges = false;
	private boolean checkUserLevel = false;
	int userLevel;

	public void forgetValues() {
		checkedBeta = false;
		checkedProfiles = false;
		trackCurrent = -1;
		checkedStatusbarNotifications = false;
		checkedToastNotifications = false;
		checkedAllowManualServiceChanges = false;
		checkUserLevel = false;
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
				editor.putString("prefKeyUserLevel", "3");
			} else {
				editor.putString("prefKeyUserLevel", "2");
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

	public boolean isApplyOnBoot() {
		return getPreferences().getBoolean(APPLY_ON_BOOT, false);
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

	public boolean isToastNotifications() {
		if (!checkedToastNotifications) {
			checkedToastNotifications = true;
			toastNotifications = getPreferences().getBoolean(ENABLE_TOAST_NOTI, false);
		}
		return toastNotifications;
	}

	public boolean isDisableDisplayIssues() {
		return getPreferences().getBoolean(DISABLE_DISPLAY_ISSUES, false);
	}

	public void setDisableDisplayIssues(boolean display) {
		Editor edit = getPreferences().edit();
		edit.putBoolean(DISABLE_DISPLAY_ISSUES, display);
		edit.commit();
	}

	public boolean isEnableBeta() {
		if (!checkedBeta) {
			checkedBeta = true;
			enableBeta = "speedup".equals((getPreferences().getString("prefKeyEnableBeta", "").trim()));
		}
		return enableBeta;
	}

	public int getUserLevel() {
		if (!checkUserLevel) {
			checkUserLevel = true;
			try {
				userLevel = Integer.parseInt(getPreferences().getString("prefKeyUserLevel", "2"));
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

	public boolean isEnableSwitchMobiledata() {
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

	public boolean isPowerUser() {
		return getUserLevel() > 2;
	}
}
