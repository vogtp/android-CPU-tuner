package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class CpuModel {

	public static final String NO_VALUE_STR = "None";

	public static final int NO_VALUE_INT = -1;

	private String profileName = NO_VALUE_STR;
	private String gov = NO_VALUE_STR;
	private int maxFreq = NO_VALUE_INT;
	private int minFreq = NO_VALUE_INT;
	private long id = -1;

	public CpuModel() {
		super();
	}

	public CpuModel(String gov, int maxFreq, int minFreq) {
		this();
		this.gov = gov;
		this.maxFreq = maxFreq;
		this.minFreq = minFreq;
	}

	public CpuModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.profileName = c.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
		this.gov = c.getString(DB.CpuProfile.INDEX_GOVERNOR);
		this.maxFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MAX);
		this.minFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MIN);
	}

	public CpuModel(Bundle bundle) {
		this();
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

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		} else {
			values.put(DB.NAME_ID, -1);
		}

		values.put(DB.CpuProfile.NAME_PROFILE_NAME, getProfileName());
		values.put(DB.CpuProfile.NAME_GOVERNOR, getGov());
		values.put(DB.CpuProfile.NAME_FREQUENCY_MAX, getMaxFreq());
		values.put(DB.CpuProfile.NAME_FREQUENCY_MIN, getMinFreq());
		return values;
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

	public long getDbId() {
		return id;
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
