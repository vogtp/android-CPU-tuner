package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class VirtualGovernorModel {

	public static final String NO_VALUE_STR = "None";

	public static final int NO_VALUE_INT = -1;

	private long id = -1;

	private String virtualGov = NO_VALUE_STR;
	private String realGov = NO_VALUE_STR;
	private int governorThresholdUp = 98;
	private int governorThresholdDown = 95;
	private String script = "";

	public VirtualGovernorModel() {
		super();
	}

	public VirtualGovernorModel(Cursor c) {
		this();
		this.id = c.getLong(DB.INDEX_ID);
		this.virtualGov = c.getString(DB.VirtualGovernor.INDEX_VIRTUAL_GOVERNOR_NAME);
		this.realGov = c.getString(DB.VirtualGovernor.INDEX_REAL_GOVERNOR);
		this.governorThresholdUp = c.getInt(DB.VirtualGovernor.INDEX_GOVERNOR_THRESHOLD_UP);
		this.governorThresholdDown = c.getInt(DB.VirtualGovernor.INDEX_GOVERNOR_THRESHOLD_DOWN);
		this.script = c.getString(DB.VirtualGovernor.INDEX_SCRIPT);
	}

	public VirtualGovernorModel(Bundle bundle) {
		this();
		readFromBundle(bundle);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putString(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, getVirtualGovernorName());
		bundle.putString(DB.VirtualGovernor.NAME_REAL_GOVERNOR, getRealGovernor());
		bundle.putInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		bundle.putInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		bundle.putString(DB.VirtualGovernor.NAME_SCRIPT, getScript());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		virtualGov = bundle.getString(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME);
		realGov = bundle.getString(DB.VirtualGovernor.NAME_REAL_GOVERNOR);
		governorThresholdUp = bundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = bundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN);
		script = bundle.getString(DB.VirtualGovernor.NAME_SCRIPT);
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}

		values.put(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, getVirtualGovernorName());
		values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, getRealGovernor());
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		values.put(DB.VirtualGovernor.NAME_SCRIPT, getScript());
		return values;
	}

	public String getRealGovernor() {
		return realGov;
	}

	public String getVirtualGovernorName() {
		return virtualGov;
	}

	public void setVirtualGovernorName(String virtualGovernorName) {
		this.virtualGov = virtualGovernorName;
	}

	public long getDbId() {
		return id;
	}

	@Override
	public String toString() {
		return virtualGov + "; " + realGov + " ( " + governorThresholdDown + ", " + governorThresholdUp + ")";
	}

	public void setDbId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + governorThresholdDown;
		result = prime * result + governorThresholdUp;
		result = prime * result + ((realGov == null) ? 0 : realGov.hashCode());
		result = prime * result + ((script == null) ? 0 : script.hashCode());
		result = prime * result + ((virtualGov == null) ? 0 : virtualGov.hashCode());
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
		VirtualGovernorModel other = (VirtualGovernorModel) obj;
		if (governorThresholdDown != other.governorThresholdDown)
			return false;
		if (governorThresholdUp != other.governorThresholdUp)
			return false;
		if (realGov == null) {
			if (other.realGov != null)
				return false;
		} else if (!realGov.equals(other.realGov))
			return false;
		if (script == null) {
			if (other.script != null)
				return false;
		} else if (!script.equals(other.script))
			return false;
		if (virtualGov == null) {
			if (other.virtualGov != null)
				return false;
		} else if (!virtualGov.equals(other.virtualGov))
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

	public void setScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

}
