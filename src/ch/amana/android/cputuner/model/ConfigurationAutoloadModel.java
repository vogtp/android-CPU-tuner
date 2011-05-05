package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.provider.db.DB;

public class ConfigurationAutoloadModel {

	private long id = -1;

	private int hour;
	private int minute;
	private int weekday;
	private long profile;

	public ConfigurationAutoloadModel() {
		super();
	}

	public ConfigurationAutoloadModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.hour = c.getInt(DB.ConfigurationAutoload.INDEX_HOUR);
		this.minute = c.getInt(DB.ConfigurationAutoload.INDEX_MINUTE);
		this.weekday = c.getInt(DB.ConfigurationAutoload.INDEX_WEEKDAY);
		this.profile = c.getLong(DB.ConfigurationAutoload.INDEX_PROFILE);
	}

	public ConfigurationAutoloadModel(Bundle bundle) {
		this();
		readFromBundle(bundle);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putInt(DB.ConfigurationAutoload.NAME_HOUR, getHour());
		bundle.putInt(DB.ConfigurationAutoload.NAME_MINUTE, getMinute());
		bundle.putInt(DB.ConfigurationAutoload.NAME_WEEKDAY, getWeekday());
		bundle.putLong(DB.ConfigurationAutoload.NAME_PROFILE, getProfile());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		hour = bundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = bundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		weekday = bundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY);
		profile = bundle.getLong(DB.ConfigurationAutoload.NAME_PROFILE);
	}

	public void readFromJson(JSONBundle jsonBundle) {
		id = jsonBundle.getLong(DB.NAME_ID);
		hour = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		weekday = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY);
		profile = jsonBundle.getLong(DB.ConfigurationAutoload.NAME_PROFILE);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(DB.ConfigurationAutoload.NAME_HOUR, getHour());
		values.put(DB.ConfigurationAutoload.NAME_MINUTE, getMinute());
		values.put(DB.ConfigurationAutoload.NAME_WEEKDAY, getWeekday());
		values.put(DB.ConfigurationAutoload.NAME_PROFILE, getProfile());
		return values;
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

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getWeekday() {
		return weekday;
	}

	public void setWeekday(int weekday) {
		this.weekday = weekday;
	}

	public long getProfile() {
		return profile;
	}

	public void setProfile(long profile) {
		this.profile = profile;
	}

}
