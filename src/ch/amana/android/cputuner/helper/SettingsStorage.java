package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SettingsStorage {

	public static final String NO_VALUE = "noValue";
	private static final String APPLY_ON_BOOT = "applyCpuSettingsOnBoot";
	public static final String ENABLE_PROFILES = "prefKeyEnableProfiles";
	public static final String ENABLE_STATUSBAR_ADDTO = "prefKeyStatusbarAddTo";
	public static final String ENABLE_STATUSBAR_NOTI = "prefKeyStatusbarNotifications";
	public static final String ENABLE_TOAST_NOTI = "prefKeyToastNotifications";
	private static final String DISABLE_DISPLAY_ISSUES = "prefKeyDisplayIssues";
	private static SettingsStorage instance;
	private final Context context;

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
	}

	protected SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isEnableProfiles() {
		return getPreferences().getBoolean(ENABLE_PROFILES, true);
	}

	public boolean isApplyOnBoot() {
		return getPreferences().getBoolean(APPLY_ON_BOOT, false);
	}

	public boolean isStatusbarAddto() {
		return getPreferences().getBoolean(ENABLE_STATUSBAR_ADDTO, true);
	}

	public boolean isStatusbarNotifications() {
		return getPreferences().getBoolean(ENABLE_STATUSBAR_NOTI, true);
	}

	public boolean isToastNotifications() {
		return getPreferences().getBoolean(ENABLE_TOAST_NOTI, false);
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
		return "speedup".equals(getPreferences().getString("prefKeyEnableBeta", ""));
	}
}
