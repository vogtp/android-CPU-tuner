package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class CpuModel {

	public static final String NO_VALUE_STR = "None";

	public static final int NO_VALUE_INT = -1;

	private String profileName = NO_VALUE_STR;

	private long id = -1;

	private String gov = NO_VALUE_STR;
	private int maxFreq = NO_VALUE_INT;
	private int minFreq = NO_VALUE_INT;

	private int wifiState = 0;
	private int gpsState = 0;
	private int bluetoothState = 0;
	private int mobiledataState = 0;
	private int backgroundSyncState = 0;
	private int governorThresholdUp = 98;
	private int governorThresholdDown = 95;

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
		this.wifiState = c.getInt(DB.CpuProfile.INDEX_WIFI_STATE);
		this.gpsState = c.getInt(DB.CpuProfile.INDEX_GPS_STATE);
		this.bluetoothState = c.getInt(DB.CpuProfile.INDEX_BLUETOOTH_STATE);
		this.mobiledataState = c.getInt(DB.CpuProfile.INDEX_MOBILEDATA_STATE);
		this.governorThresholdUp = c.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_UP);
		this.governorThresholdDown = c.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_DOWN);
		this.backgroundSyncState = c.getInt(DB.CpuProfile.INDEX_BACKGROUND_SYNC_STATE);
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
		bundle.putInt(DB.CpuProfile.NAME_WIFI_STATE, getWifiState());
		bundle.putInt(DB.CpuProfile.NAME_GPS_STATE, getGpsState());
		bundle.putInt(DB.CpuProfile.NAME_BLUETOOTH_STATE, getBluetoothState());
		bundle.putInt(DB.CpuProfile.NAME_MOBILEDATA_STATE, getMobiledataState());
		bundle.putInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		bundle.putInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		bundle.putInt(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE, getBackgroundSyncState());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		profileName = bundle.getString(DB.CpuProfile.NAME_PROFILE_NAME);
		gov = bundle.getString(DB.CpuProfile.NAME_GOVERNOR);
		maxFreq = bundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MAX);
		minFreq = bundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MIN);
		wifiState = bundle.getInt(DB.CpuProfile.NAME_WIFI_STATE);
		gpsState = bundle.getInt(DB.CpuProfile.NAME_GPS_STATE);
		bluetoothState = bundle.getInt(DB.CpuProfile.NAME_BLUETOOTH_STATE);
		mobiledataState = bundle.getInt(DB.CpuProfile.NAME_MOBILEDATA_STATE);
		governorThresholdUp = bundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = bundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN);
		backgroundSyncState = bundle.getInt(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}

		values.put(DB.CpuProfile.NAME_PROFILE_NAME, getProfileName());
		values.put(DB.CpuProfile.NAME_GOVERNOR, getGov());
		values.put(DB.CpuProfile.NAME_FREQUENCY_MAX, getMaxFreq());
		values.put(DB.CpuProfile.NAME_FREQUENCY_MIN, getMinFreq());
		values.put(DB.CpuProfile.NAME_WIFI_STATE, getWifiState());
		values.put(DB.CpuProfile.NAME_GPS_STATE, getGpsState());
		values.put(DB.CpuProfile.NAME_BLUETOOTH_STATE, getBluetoothState());
		values.put(DB.CpuProfile.NAME_MOBILEDATA_STATE, getMobiledataState());
		values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		values.put(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE, getBackgroundSyncState());
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
			Logger.w("Cannot convert freq", e);
		}
		return "NaN";
	}

	public void setWifiState(int wifiState) {
		this.wifiState = wifiState;
	}

	public int getWifiState() {
		return wifiState;
	}

	public void setGpsState(int gpsState) {
		this.gpsState = gpsState;
	}

	public int getGpsState() {
		return gpsState;
	}

	public void setBluetoothState(int bluetoothState) {
		this.bluetoothState = bluetoothState;
	}

	public int getBluetoothState() {
		return bluetoothState;
	}

	public void setMobiledataState(int mobiledataState) {
		this.mobiledataState = mobiledataState;
	}

	public int getMobiledataState() {
		return mobiledataState;
	}

	public void setDbId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + backgroundSyncState;
		result = prime * result + bluetoothState;
		result = prime * result + ((gov == null) ? 0 : gov.hashCode());
		result = prime * result + governorThresholdDown;
		result = prime * result + governorThresholdUp;
		result = prime * result + gpsState;
		result = prime * result + maxFreq;
		result = prime * result + minFreq;
		result = prime * result + mobiledataState;
		result = prime * result + ((profileName == null) ? 0 : profileName.hashCode());
		result = prime * result + wifiState;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CpuModel other = (CpuModel) obj;
		if (backgroundSyncState != other.backgroundSyncState)
			return false;
		if (bluetoothState != other.bluetoothState)
			return false;
		if (gov == null) {
			if (other.gov != null)
				return false;
		} else if (!gov.equals(other.gov))
			return false;
		if (governorThresholdDown != other.governorThresholdDown)
			return false;
		if (governorThresholdUp != other.governorThresholdUp)
			return false;
		if (gpsState != other.gpsState)
			return false;
		if (maxFreq != other.maxFreq)
			return false;
		if (minFreq != other.minFreq)
			return false;
		if (mobiledataState != other.mobiledataState)
			return false;
		if (profileName == null) {
			if (other.profileName != null)
				return false;
		} else if (!profileName.equals(other.profileName))
			return false;
		if (wifiState != other.wifiState)
			return false;
		return true;
	}

	public int getGovernorThresholdUp() {
		return governorThresholdUp;
	}

	public void setGovernorThresholdUp(int i) {
		if (i > -1 && i < 101) {
			this.governorThresholdUp = i;
		}
	}

	public int getGovernorThresholdDown() {
		return governorThresholdDown;
	}

	public void setGovernorThresholdDown(int i) {
		if (i > -1 && i < 101) {
			this.governorThresholdDown = i;
		}
	}

	public void setGovernorThresholdUp(String string) {
		try {
			setGovernorThresholdUp(Integer.parseInt(string));
		} catch (Exception e) {
			Logger.w("Cannot parse " + string + " as int");
		}
	}

	public void setGovernorThresholdDown(String string) {
		try {
			setGovernorThresholdDown(Integer.parseInt(string));
		} catch (Exception e) {
			Logger.w("Cannot parse " + string + " as int");
		}
	}

	public void setBackgroundSyncState(int backgroundSyncState) {
		this.backgroundSyncState = backgroundSyncState;
	}

	public int getBackgroundSyncState() {
		return backgroundSyncState;
	}
}
