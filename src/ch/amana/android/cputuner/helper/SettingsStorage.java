package ch.amana.android.cputuner.helper;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsStorage {

	public static final String NO_VALUE = "noValue";
	private static final String APPLY_ON_BOOT = "applyCpuSettingsOnBoot";
	private static SettingsStorage instance;
	private Context context;

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

	public void writeValue(String key, String val) {
		Editor editor = getPreferences().edit();
		editor.putString(key, val);
		editor.commit();
	}

	public String getValue(String key) {
		return getPreferences().getString(key, NO_VALUE);
	}

	public void setApplyOnBoot(boolean applyOnBoot) {
		Editor editor = getPreferences().edit();
		editor.putBoolean(APPLY_ON_BOOT, applyOnBoot);
		editor.commit();
	}

	public boolean isApplyOnBoot() {
		return getPreferences().getBoolean(APPLY_ON_BOOT, false);
	}

	public Set<String> getKeys() {
		Map<String, ?> all = getPreferences().getAll();
		all.remove(APPLY_ON_BOOT);
		return all.keySet();
	}

	public void dumpPerferences() {
		Map<String, ?> all = getPreferences().getAll();
		for (Iterator<String> iterator = all.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			String val = all.get(key).toString();
			Log.d(Logger.TAG, key + " -> " + val);
		}
	}
}
