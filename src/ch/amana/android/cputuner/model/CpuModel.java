package ch.amana.android.cputuner.model;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.provider.db.DB;

public class CpuModel {

	public static final String INTENT_EXTRA = "CpuModelData";
	public static final String NO_PROFILE = "None";

	private String profileName = NO_PROFILE;
	private String gov;
	private int maxFreq;
	private int minFreq;
	private long id;

	public CpuModel(String gov, int maxFreq, int minFreq) {
		super();
		this.gov = gov;
		this.maxFreq = maxFreq;
		this.minFreq = minFreq;
	}

	public CpuModel(String profile) {
		super();
		SettingsStorage store = SettingsStorage.getInstance();
		store.dumpPerferences();
		this.profileName = profile;
		this.gov = getFromStore(store, profile, CpuHandler.SCALING_GOVERNOR);
		this.maxFreq = str2int(getFromStore(store, profile, CpuHandler.SCALING_MAX_FREQ));
		this.minFreq = str2int(getFromStore(store, profile, CpuHandler.SCALING_MIN_FREQ));
	}

	private int str2int(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 0;
	}

	public CpuModel(Cursor c) {
		super();
		this.id = c.getLong(DB.INDEX_ID);
		this.profileName = c.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
		this.gov = c.getString(DB.CpuProfile.INDEX_GOVERNOR);
		this.maxFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MAX);
		this.minFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MIN);
	}

	public CpuModel(Bundle bundle) {
		super();
		readFromBundle(bundle);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putString(DB.CpuProfile.NAME_PROFILE_NAME, getProfileName());
		bundle.putString(DB.CpuProfile.NAME_GOVERNOR, getGov());
		bundle.putInt(DB.CpuProfile.NAME_FREQUENCY_MAX, getMaxFreq());
		bundle.putInt(DB.CpuProfile.NAME_FREQUENCY_MIN, getMinFreq());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		profileName = bundle.getString(DB.CpuProfile.NAME_PROFILE_NAME);
		gov = bundle.getString(DB.CpuProfile.NAME_GOVERNOR);
		maxFreq = bundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MAX);
		minFreq = bundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MIN);
	}

	private String getFromStore(SettingsStorage store, String profile, String key) {
		String value = store.getValue(profile + "_" + key);
		if (value.equals(SettingsStorage.NO_VALUE)) {
			value = store.getValue(key);
		}
		return value;
	}

	public void save() {
		save(getProfileName());
	}

	public void save(CharSequence currentProfile) {
		if (NO_PROFILE.equals(currentProfile)) {
			Log.w(Logger.TAG, "Not saving since profile is " + currentProfile);
		}
		SettingsStorage store = SettingsStorage.getInstance();
		store.writeValue(currentProfile + "_" + CpuHandler.SCALING_GOVERNOR, gov);
		store.writeValue(currentProfile + "_" + CpuHandler.SCALING_MAX_FREQ, maxFreq + "");
		store.writeValue(currentProfile + "_" + CpuHandler.SCALING_MIN_FREQ, minFreq + "");
	}

	public String getGov() {
		return gov;
	}

	public void setGov(String gov) {
		this.gov = gov;
	}

	public int getMaxFreq() {
		return maxFreq;
	}

	public void setMaxFreq(int maxFreq) {
		this.maxFreq = maxFreq;
	}

	public int getMinFreq() {
		return minFreq;
	}

	public void setMinFreq(int minFreq) {
		this.minFreq = minFreq;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@Override
	public String toString() {
		return gov + "; " + convertFreq2GHz(minFreq) + " - " + convertFreq2GHz(maxFreq);
	}

	public static String convertFreq2GHz(int freq) {
		try {
			int i = Math.round(freq / 1000f);
			return i + " MHz";
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot convert freq", e);
		}
		return "NaN";
	}

}
