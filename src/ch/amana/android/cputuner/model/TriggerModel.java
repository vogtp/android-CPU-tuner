package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggerModel {

	private String name;
	private int batteryLevel;
	private long screenOffProfileId;
	private long batteryProfileId;
	private long powerProfileId;
	private long id = -1;
	private long powerCurrentSumPower;
	private long powerCurrentCntPower;
	private long powerCurrentSumBattery;
	private long powerCurrentCntBattery;
	private long powerCurrentSumScreenLocked;
	private long powerCurrentCntScreenLocked;

	public TriggerModel() {
		this("", 100, -1, -1, -1, 0, 0, 0, 0, 0, 0);
	}

	public TriggerModel(String name, int batteryLevel, long screenOffProfileId, long batteryProfileId,
			long powerProfileId, long powercurrentSumPower, long powercurrentCntPower,
			long powercurrentSumBattery, long powercurrentCntBattery, long powercurrentSumScreenLocked, long powercurrentCntScreenLocked) {
		super();
		this.name = name;
		this.batteryLevel = batteryLevel;
		this.screenOffProfileId = screenOffProfileId;
		this.batteryProfileId = batteryProfileId;
		this.powerProfileId = powerProfileId;
		this.powerCurrentSumPower = powercurrentSumPower;
		this.powerCurrentCntPower = powercurrentCntPower;
		this.powerCurrentSumBattery = powercurrentSumBattery;
		this.powerCurrentCntBattery = powercurrentCntBattery;
		this.powerCurrentSumScreenLocked = powercurrentSumScreenLocked;
		this.powerCurrentCntScreenLocked = powercurrentCntScreenLocked;
	}

	public TriggerModel(Cursor c) {
		this(c.getString(DB.Trigger.INDEX_TRIGGER_NAME),
				c.getInt(DB.Trigger.INDEX_BATTERY_LEVEL),
				c.getLong(DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID),
				c.getLong(DB.Trigger.INDEX_BATTERY_PROFILE_ID),
				c.getLong(DB.Trigger.INDEX_POWER_PROFILE_ID),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_POW),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_POW),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_BAT),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_BAT),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_SUM_LCK),
				c.getLong(DB.Trigger.INDEX_POWER_CURRENT_CNT_LCK));
		id = c.getLong(DB.INDEX_ID);
	}

	public TriggerModel(Bundle bundle) {
		readFromBundle(bundle);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putString(DB.Trigger.NAME_TRIGGER_NAME, getName());
		bundle.putInt(DB.Trigger.NAME_BATTERY_LEVEL, getBatteryLevel());
		bundle.putLong(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, getScreenOffProfileId());
		bundle.putLong(DB.Trigger.NAME_BATTERY_PROFILE_ID, getBatteryProfileId());
		bundle.putLong(DB.Trigger.NAME_POWER_PROFILE_ID, getPowerProfileId());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_SUM_POW, getPowerCurrentSumPower());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_CNT_POW, getPowerCurrentCntPower());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_SUM_BAT, getPowerCurrentSumBattery());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_CNT_BAT, getPowerCurrentCntBattery());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_SUM_LCK, getPowerCurrentSumScreenLocked());
		bundle.putLong(DB.Trigger.NAME_POWER_CURRENT_CNT_LCK, getPowerCurrentCntScreenLocked());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		name = bundle.getString(DB.Trigger.NAME_TRIGGER_NAME);
		batteryLevel = bundle.getInt(DB.Trigger.NAME_BATTERY_LEVEL);
		screenOffProfileId = bundle.getLong(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID);
		batteryProfileId = bundle.getLong(DB.Trigger.NAME_BATTERY_PROFILE_ID);
		powerProfileId = bundle.getLong(DB.Trigger.NAME_POWER_PROFILE_ID);
		powerCurrentSumPower = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_SUM_POW);
		powerCurrentCntPower = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_CNT_POW);
		powerCurrentSumBattery = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_SUM_BAT);
		powerCurrentCntBattery = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_CNT_BAT);
		powerCurrentSumScreenLocked = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_SUM_LCK);
		powerCurrentCntScreenLocked = bundle.getLong(DB.Trigger.NAME_POWER_CURRENT_CNT_LCK);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(DB.Trigger.NAME_TRIGGER_NAME, getName());
		values.put(DB.Trigger.NAME_BATTERY_LEVEL, getBatteryLevel());
		values.put(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, getScreenOffProfileId());
		values.put(DB.Trigger.NAME_BATTERY_PROFILE_ID, getBatteryProfileId());
		values.put(DB.Trigger.NAME_POWER_PROFILE_ID, getPowerProfileId());
		values.put(DB.Trigger.NAME_POWER_CURRENT_SUM_POW, getPowerCurrentSumPower());
		values.put(DB.Trigger.NAME_POWER_CURRENT_CNT_POW, getPowerCurrentCntPower());
		values.put(DB.Trigger.NAME_POWER_CURRENT_SUM_BAT, getPowerCurrentSumBattery());
		values.put(DB.Trigger.NAME_POWER_CURRENT_CNT_BAT, getPowerCurrentCntBattery());
		values.put(DB.Trigger.NAME_POWER_CURRENT_SUM_LCK, getPowerCurrentSumScreenLocked());
		values.put(DB.Trigger.NAME_POWER_CURRENT_CNT_LCK, getPowerCurrentCntScreenLocked());
		return values;
	}

	public String getName() {
		return name;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}

	public long getScreenOffProfileId() {
		return screenOffProfileId;
	}

	public long getBatteryProfileId() {
		return batteryProfileId;
	}

	public long getPowerProfileId() {
		return powerProfileId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBatteryLevel(int batLevel) {
		if (batLevel > 100) {
			batLevel = 100;
		} else if (batLevel < 0) {
			batLevel = 0;
		}
		this.batteryLevel = batLevel;
	}

	public void setScreenOffProfileId(long screenOffProfileId) {
		powerCurrentSumScreenLocked = 0;
		powerCurrentCntScreenLocked = 0;
		this.screenOffProfileId = screenOffProfileId;
	}

	public void setBatteryProfileId(long batteryProfileId) {
		powerCurrentSumBattery = 0;
		powerCurrentCntBattery = 0;
		this.batteryProfileId = batteryProfileId;
	}

	public void setPowerProfileId(long powerProfileId) {
		powerCurrentSumPower = 0;
		powerCurrentCntPower = 0;
		this.powerProfileId = powerProfileId;
	}

	public long getDbId() {
		return id;
	}

	public void setDbId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPowerCurrentSumPower() {
		return powerCurrentSumPower;
	}

	public void setPowerCurrentSumPower(long powerCurrentSumPower) {
		this.powerCurrentSumPower = powerCurrentSumPower;
	}

	public long getPowerCurrentCntPower() {
		return powerCurrentCntPower;
	}

	public void setPowerCurrentCntPower(long powerCurrentCntPower) {
		this.powerCurrentCntPower = powerCurrentCntPower;
	}

	public long getPowerCurrentSumBattery() {
		return powerCurrentSumBattery;
	}

	public void setPowerCurrentSumBattery(long powerCurrentSumBattery) {
		this.powerCurrentSumBattery = powerCurrentSumBattery;
	}

	public long getPowerCurrentCntBattery() {
		return powerCurrentCntBattery;
	}

	public void setPowerCurrentCntBattery(long powerCurrentCntBattery) {
		this.powerCurrentCntBattery = powerCurrentCntBattery;
	}

	public long getPowerCurrentSumScreenLocked() {
		return powerCurrentSumScreenLocked;
	}

	public void setPowerCurrentSumScreenLocked(long powerCurrentSumScreenLocked) {
		this.powerCurrentSumScreenLocked = powerCurrentSumScreenLocked;
	}

	public long getPowerCurrentCntScreenLocked() {
		return powerCurrentCntScreenLocked;
	}

	public void setPowerCurrentCntScreenLocked(long powerCurrentCntScreenLocked) {
		this.powerCurrentCntScreenLocked = powerCurrentCntScreenLocked;
	}

	public void clearPowerCurrent() {
		powerCurrentSumPower = 0;
		powerCurrentCntPower = 0;
		powerCurrentSumBattery = 0;
		powerCurrentCntBattery = 0;
		powerCurrentSumScreenLocked = 0;
		powerCurrentCntScreenLocked = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + batteryLevel;
		result = prime * result + (int) (batteryProfileId ^ (batteryProfileId >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (powerCurrentCntBattery ^ (powerCurrentCntBattery >>> 32));
		result = prime * result + (int) (powerCurrentCntPower ^ (powerCurrentCntPower >>> 32));
		result = prime * result + (int) (powerCurrentCntScreenLocked ^ (powerCurrentCntScreenLocked >>> 32));
		result = prime * result + (int) (powerCurrentSumBattery ^ (powerCurrentSumBattery >>> 32));
		result = prime * result + (int) (powerCurrentSumPower ^ (powerCurrentSumPower >>> 32));
		result = prime * result + (int) (powerCurrentSumScreenLocked ^ (powerCurrentSumScreenLocked >>> 32));
		result = prime * result + (int) (powerProfileId ^ (powerProfileId >>> 32));
		result = prime * result + (int) (screenOffProfileId ^ (screenOffProfileId >>> 32));
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
		TriggerModel other = (TriggerModel) obj;
		if (batteryLevel != other.batteryLevel)
			return false;
		if (batteryProfileId != other.batteryProfileId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (powerCurrentCntBattery != other.powerCurrentCntBattery)
			return false;
		if (powerCurrentCntPower != other.powerCurrentCntPower)
			return false;
		if (powerCurrentCntScreenLocked != other.powerCurrentCntScreenLocked)
			return false;
		if (powerCurrentSumBattery != other.powerCurrentSumBattery)
			return false;
		if (powerCurrentSumPower != other.powerCurrentSumPower)
			return false;
		if (powerCurrentSumScreenLocked != other.powerCurrentSumScreenLocked)
			return false;
		if (powerProfileId != other.powerProfileId)
			return false;
		if (screenOffProfileId != other.screenOffProfileId)
			return false;
		return true;
	}
}
