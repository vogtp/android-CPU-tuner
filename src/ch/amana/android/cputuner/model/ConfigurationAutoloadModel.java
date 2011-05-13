package ch.amana.android.cputuner.model;

import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class ConfigurationAutoloadModel {


	private long id = -1;

	private int hour;
	private int minute;
	private int weekday;
	private String configuration;

	private long nextExecution = -1;

	public ConfigurationAutoloadModel() {
		super();
	}

	public ConfigurationAutoloadModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.hour = c.getInt(DB.ConfigurationAutoload.INDEX_HOUR);
		this.minute = c.getInt(DB.ConfigurationAutoload.INDEX_MINUTE);
		this.weekday = c.getInt(DB.ConfigurationAutoload.INDEX_WEEKDAY);
		this.configuration = c.getString(DB.ConfigurationAutoload.INDEX_CONFIGURATION);
		this.nextExecution = c.getLong(DB.ConfigurationAutoload.INDEX_NEXT_EXEC);
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
		bundle.putString(DB.ConfigurationAutoload.NAME_CONFIGURATION, getConfiguration());
		bundle.putLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC, getNextExecution());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		hour = bundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = bundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		weekday = bundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY);
		configuration = bundle.getString(DB.ConfigurationAutoload.NAME_CONFIGURATION);
		nextExecution = bundle.getLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC);
	}

	public void readFromJson(JSONBundle jsonBundle) {
		id = jsonBundle.getLong(DB.NAME_ID);
		hour = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_HOUR);
		minute = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_MINUTE);
		weekday = jsonBundle.getInt(DB.ConfigurationAutoload.NAME_WEEKDAY);
		configuration = jsonBundle.getString(DB.ConfigurationAutoload.NAME_CONFIGURATION);
		nextExecution = jsonBundle.getLong(DB.ConfigurationAutoload.NAME_NEXT_EXEC);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}
		values.put(DB.ConfigurationAutoload.NAME_HOUR, getHour());
		values.put(DB.ConfigurationAutoload.NAME_MINUTE, getMinute());
		values.put(DB.ConfigurationAutoload.NAME_WEEKDAY, getWeekday());
		values.put(DB.ConfigurationAutoload.NAME_CONFIGURATION, getConfiguration());
		values.put(DB.ConfigurationAutoload.NAME_NEXT_EXEC, getNextExecution());
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

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public String getConfiguration() {
		return configuration;
	}

	public long getNextExecution() {
		if (nextExecution <= System.currentTimeMillis()) {
			calcNextExecution();
		}
		return nextExecution ;
	}

	public void calcNextExecution() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		long delta = -1;
		while (delta < 0) {
			delta = cal.getTimeInMillis() - System.currentTimeMillis();
			if (delta < 0) {
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		Logger.d("Next execution: " + cal.toString());
		nextExecution = cal.getTimeInMillis();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + hour;
		result = prime * result + minute;
		result = prime * result + weekday;
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
		ConfigurationAutoloadModel other = (ConfigurationAutoloadModel) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		if (weekday != other.weekday)
			return false;
		return true;
	}
}
