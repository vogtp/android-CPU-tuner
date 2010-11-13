package ch.amana.android.cputuner.model;

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

	public TriggerModel() {
		this("", 50, -1, -1, -1);
	}

	public TriggerModel(String name, int batteryLevel, long screenOffProfileId, long batteryProfileId,
			long powerProfileId) {
		super();
		this.name = name;
		this.batteryLevel = batteryLevel;
		this.screenOffProfileId = screenOffProfileId;
		this.batteryProfileId = batteryProfileId;
		this.powerProfileId = powerProfileId;
	}

	public TriggerModel(Cursor c) {
		this(c.getString(DB.Trigger.INDEX_TRIGGER_NAME),
				c.getInt(DB.Trigger.INDEX_BATTERY_LEVEL),
				c.getLong(DB.Trigger.INDEX_SCREEN_OFF_PROFILE_ID),
				c.getLong(DB.Trigger.INDEX_BATTERY_PROFILE_ID),
				c.getLong(DB.Trigger.INDEX_POWER_PROFILE_ID));
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
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		name = bundle.getString(DB.Trigger.NAME_TRIGGER_NAME);
		batteryLevel = bundle.getInt(DB.Trigger.NAME_BATTERY_LEVEL);
		screenOffProfileId = bundle.getLong(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID);
		batteryProfileId = bundle.getLong(DB.Trigger.NAME_BATTERY_PROFILE_ID);
		powerProfileId = bundle.getLong(DB.Trigger.NAME_POWER_PROFILE_ID);
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

	public void setBatteryLevel(int batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public void setScreenOffProfileId(int screenOffProfileId) {
		this.screenOffProfileId = screenOffProfileId;
	}

	public void setBatteryProfileId(int batteryProfileId) {
		this.batteryProfileId = batteryProfileId;
	}

	public void setPowerProfileId(int powerProfileId) {
		this.powerProfileId = powerProfileId;
	}

}
