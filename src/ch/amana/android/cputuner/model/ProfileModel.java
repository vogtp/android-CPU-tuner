package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfileModel implements IGovernorModel {

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
	private int mobiledata3GState = 0;
	private int mobiledataConnectionState = 0;
	private int backgroundSyncState = 0;
	private int governorThresholdUp = 0;
	private int governorThresholdDown = 0;
	private long virtualGovernor = -1;
	private String script = "";
	private int powersaveBias = 0;
	private int airplainemodeState = 0;

	private int useNumberOfCpus;

	public ProfileModel() {
		super();
	}

	public ProfileModel(String gov, int maxFreq, int minFreq, int threshUp, int threshDown, int powersaveBias) {
		this();
		this.gov = gov;
		this.maxFreq = maxFreq;
		this.minFreq = minFreq;
		this.governorThresholdUp = threshUp;
		this.governorThresholdDown = threshDown;
		this.powersaveBias = powersaveBias;
	}

	public ProfileModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.profileName = c.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
		this.gov = c.getString(DB.CpuProfile.INDEX_GOVERNOR);
		this.maxFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MAX);
		this.minFreq = c.getInt(DB.CpuProfile.INDEX_FREQUENCY_MIN);
		this.wifiState = c.getInt(DB.CpuProfile.INDEX_WIFI_STATE);
		this.gpsState = c.getInt(DB.CpuProfile.INDEX_GPS_STATE);
		this.bluetoothState = c.getInt(DB.CpuProfile.INDEX_BLUETOOTH_STATE);
		this.mobiledata3GState = c.getInt(DB.CpuProfile.INDEX_MOBILEDATA_3G_STATE);
		this.mobiledataConnectionState = c.getInt(DB.CpuProfile.INDEX_MOBILEDATA_CONNECTION_STATE);
		this.governorThresholdUp = c.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_UP);
		this.governorThresholdDown = c.getInt(DB.CpuProfile.INDEX_GOVERNOR_THRESHOLD_DOWN);
		this.backgroundSyncState = c.getInt(DB.CpuProfile.INDEX_BACKGROUND_SYNC_STATE);
		this.virtualGovernor = c.getLong(DB.CpuProfile.INDEX_VIRTUAL_GOVERNOR);
		this.script = c.getString(DB.CpuProfile.INDEX_SCRIPT);
		this.powersaveBias = c.getInt(DB.CpuProfile.INDEX_POWERSEAVE_BIAS);
		this.airplainemodeState = c.getInt(DB.CpuProfile.INDEX_AIRPLANEMODE_STATE);
		this.useNumberOfCpus = c.getInt(DB.CpuProfile.INDEX_USE_NUMBER_OF_CPUS);
	}

	public ProfileModel(Bundle bundle) {
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
		bundle.putInt(DB.CpuProfile.NAME_MOBILEDATA_3G_STATE, getMobiledata3GState());
		bundle.putInt(DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE, getMobiledataConnectionState());
		bundle.putInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		bundle.putInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		bundle.putInt(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE, getBackgroundSyncState());
		bundle.putLong(DB.CpuProfile.NAME_VIRTUAL_GOVERNOR, getVirtualGovernor());
		bundle.putString(DB.CpuProfile.NAME_SCRIPT, getScript());
		bundle.putInt(DB.CpuProfile.NAME_POWERSEAVE_BIAS, getPowersaveBias());
		bundle.putInt(DB.CpuProfile.NAME_AIRPLANEMODE_STATE, getAirplainemodeState());
		bundle.putInt(DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS, getUseNumberOfCpus());
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
		mobiledata3GState = bundle.getInt(DB.CpuProfile.NAME_MOBILEDATA_3G_STATE);
		mobiledataConnectionState = bundle.getInt(DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE);
		governorThresholdUp = bundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = bundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN);
		backgroundSyncState = bundle.getInt(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE);
		virtualGovernor = bundle.getLong(DB.CpuProfile.NAME_VIRTUAL_GOVERNOR);
		script = bundle.getString(DB.CpuProfile.NAME_SCRIPT);
		powersaveBias = bundle.getInt(DB.CpuProfile.NAME_POWERSEAVE_BIAS);
		airplainemodeState = bundle.getInt(DB.CpuProfile.NAME_AIRPLANEMODE_STATE);
		useNumberOfCpus = bundle.getInt(DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS);
	}

	public void readFromJson(JSONBundle jsonBundle) {
		id = jsonBundle.getLong(DB.NAME_ID);
		profileName = jsonBundle.getString(DB.CpuProfile.NAME_PROFILE_NAME);
		gov = jsonBundle.getString(DB.CpuProfile.NAME_GOVERNOR);
		maxFreq = jsonBundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MAX);
		minFreq = jsonBundle.getInt(DB.CpuProfile.NAME_FREQUENCY_MIN);
		wifiState = jsonBundle.getInt(DB.CpuProfile.NAME_WIFI_STATE);
		gpsState = jsonBundle.getInt(DB.CpuProfile.NAME_GPS_STATE);
		bluetoothState = jsonBundle.getInt(DB.CpuProfile.NAME_BLUETOOTH_STATE);
		mobiledata3GState = jsonBundle.getInt(DB.CpuProfile.NAME_MOBILEDATA_3G_STATE);
		mobiledataConnectionState = jsonBundle.getInt(DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE);
		governorThresholdUp = jsonBundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = jsonBundle.getInt(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN);
		backgroundSyncState = jsonBundle.getInt(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE);
		virtualGovernor = jsonBundle.getLong(DB.CpuProfile.NAME_VIRTUAL_GOVERNOR);
		script = jsonBundle.getString(DB.CpuProfile.NAME_SCRIPT);
		powersaveBias = jsonBundle.getInt(DB.CpuProfile.NAME_POWERSEAVE_BIAS);
		airplainemodeState = jsonBundle.getInt(DB.CpuProfile.NAME_AIRPLANEMODE_STATE);
		useNumberOfCpus = jsonBundle.getInt(DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS);
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
		values.put(DB.CpuProfile.NAME_MOBILEDATA_3G_STATE, getMobiledata3GState());
		values.put(DB.CpuProfile.NAME_MOBILEDATA_CONNECTION_STATE, getMobiledataConnectionState());
		values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		values.put(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE, getBackgroundSyncState());
		values.put(DB.CpuProfile.NAME_VIRTUAL_GOVERNOR, getVirtualGovernor());
		values.put(DB.CpuProfile.NAME_SCRIPT, getScript());
		values.put(DB.CpuProfile.NAME_POWERSEAVE_BIAS, getPowersaveBias());
		values.put(DB.CpuProfile.NAME_AIRPLANEMODE_STATE, getAirplainemodeState());
		values.put(DB.CpuProfile.NAME_USE_NUMBER_OF_CPUS, getUseNumberOfCpus());
		return values;
	}

	public String getGov() {
		if (gov == null) {
			return NO_VALUE_STR;
		}
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

	public void setMobiledata3GState(int mobiledata3GState) {
		this.mobiledata3GState = mobiledata3GState;
	}

	public int getMobiledata3GState() {
		return mobiledata3GState;
	}

	public void setDbId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + airplainemodeState;
		result = prime * result + backgroundSyncState;
		result = prime * result + bluetoothState;
		result = prime * result + ((gov == null) ? 0 : gov.hashCode());
		result = prime * result + governorThresholdDown;
		result = prime * result + governorThresholdUp;
		result = prime * result + gpsState;
		result = prime * result + maxFreq;
		result = prime * result + minFreq;
		result = prime * result + mobiledata3GState;
		result = prime * result + mobiledataConnectionState;
		result = prime * result + powersaveBias;
		result = prime * result + ((profileName == null) ? 0 : profileName.hashCode());
		result = prime * result + ((script == null) ? 0 : script.hashCode());
		result = prime * result + useNumberOfCpus;
		result = prime * result + (int) (virtualGovernor ^ (virtualGovernor >>> 32));
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
		ProfileModel other = (ProfileModel) obj;
		if (airplainemodeState != other.airplainemodeState)
			return false;
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
		if (mobiledata3GState != other.mobiledata3GState)
			return false;
		if (mobiledataConnectionState != other.mobiledataConnectionState)
			return false;
		if (powersaveBias != other.powersaveBias)
			return false;
		if (profileName == null) {
			if (other.profileName != null)
				return false;
		} else if (!profileName.equals(other.profileName))
			return false;
		if (script == null) {
			if (other.script != null)
				return false;
		} else if (!script.equals(other.script))
			return false;
		if (useNumberOfCpus != other.useNumberOfCpus)
			return false;
		if (virtualGovernor != other.virtualGovernor)
			return false;
		if (wifiState != other.wifiState)
			return false;
		return true;
	}

	public int getGovernorThresholdUp() {
		return governorThresholdUp;
	}

	public void setGovernorThresholdUp(int i) {
		if (i < 101) {
			this.governorThresholdUp = i;
		}
	}

	public int getGovernorThresholdDown() {
		return governorThresholdDown;
	}

	public void setGovernorThresholdDown(int i) {
		if (i < 101) {
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

	public void setVirtualGovernor(long virtualGovernor) {
		this.virtualGovernor = virtualGovernor;
	}

	public long getVirtualGovernor() {
		return virtualGovernor;
	}

	public void setMobiledataConnectionState(int mobiledataConnectionState) {
		this.mobiledataConnectionState = mobiledataConnectionState;
	}

	public int getMobiledataConnectionState() {
		return mobiledataConnectionState;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

	public boolean hasScript() {
		return script != null && !TextUtils.isEmpty(script.trim());
	}

	public void setPowersaveBias(int powersaveBias) {
		this.powersaveBias = powersaveBias;
	}

	public int getPowersaveBias() {
		return powersaveBias;
	}

	public void setAirplainemodeState(int airplainemodeState) {
		this.airplainemodeState = airplainemodeState;
	}

	public int getAirplainemodeState() {
		return airplainemodeState;
	}

	@Override
	public void setUseNumberOfCpus(int useNumberOfCpus) {
		this.useNumberOfCpus = useNumberOfCpus;
	}

	@Override
	public int getUseNumberOfCpus() {
		return useNumberOfCpus;
	}
}
