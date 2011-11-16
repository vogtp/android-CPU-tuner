package ch.amana.android.cputuner.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class VirtualGovernorModel implements IGovernorModel {

	private long id = -1;

	private String virtualGov = ProfileModel.NO_VALUE_STR;
	private String realGov = ProfileModel.NO_VALUE_STR;
	private int governorThresholdUp = -1;
	private int governorThresholdDown = -1;
	private String script = "";
	private int powersaveBias = 0;
	private int useNumberOfCpus;

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
		this.powersaveBias = c.getInt(DB.VirtualGovernor.INDEX_POWERSEAVE_BIAS);
		this.useNumberOfCpus = c.getInt(DB.VirtualGovernor.INDEX_USE_NUMBER_OF_CPUS);
		if (script == null) {
			// fix equals
			script = "";
		}
	}

	public VirtualGovernorModel(Bundle bundle) {
		this();
		readFromBundle(bundle);
	}

	public VirtualGovernorModel(VirtualGovernorModel virtGov) {
		this();
		Bundle b = new Bundle();
		virtGov.saveToBundle(b);
		readFromBundle(b);
	}

	public void saveToBundle(Bundle bundle) {
		if (id > -1) {
			bundle.putLong(DB.NAME_ID, id);
		} else {
			bundle.putLong(DB.NAME_ID, -1);
		}
		bundle.putString(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, getVirtualGovernorName());
		bundle.putString(DB.VirtualGovernor.NAME_REAL_GOVERNOR, getGov());
		bundle.putInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		bundle.putInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		bundle.putString(DB.VirtualGovernor.NAME_SCRIPT, getScript());
		bundle.putInt(DB.VirtualGovernor.NAME_POWERSEAVE_BIAS, getPowersaveBias());
		bundle.putInt(DB.VirtualGovernor.NAME_USE_NUMBER_OF_CPUS, getUseNumberOfCpus());
	}

	public void readFromBundle(Bundle bundle) {
		id = bundle.getLong(DB.NAME_ID);
		virtualGov = bundle.getString(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME);
		realGov = bundle.getString(DB.VirtualGovernor.NAME_REAL_GOVERNOR);
		governorThresholdUp = bundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = bundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN);
		script = bundle.getString(DB.VirtualGovernor.NAME_SCRIPT);
		powersaveBias = bundle.getInt(DB.VirtualGovernor.NAME_POWERSEAVE_BIAS);
		useNumberOfCpus = bundle.getInt(DB.VirtualGovernor.NAME_USE_NUMBER_OF_CPUS);
		if (script == null) {
			// fix equals
			script = "";
		}
	}

	public void readFromJson(JSONBundle jsonBundle) {
		id = jsonBundle.getLong(DB.NAME_ID);
		virtualGov = jsonBundle.getString(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME);
		realGov = jsonBundle.getString(DB.VirtualGovernor.NAME_REAL_GOVERNOR);
		governorThresholdUp = jsonBundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP);
		governorThresholdDown = jsonBundle.getInt(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN);
		script = jsonBundle.getString(DB.VirtualGovernor.NAME_SCRIPT);
		powersaveBias = jsonBundle.getInt(DB.VirtualGovernor.NAME_POWERSEAVE_BIAS);
		useNumberOfCpus = jsonBundle.getInt(DB.VirtualGovernor.NAME_USE_NUMBER_OF_CPUS);
		if (script == null) {
			// fix equals
			script = "";
		}
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();
		if (id > -1) {
			values.put(DB.NAME_ID, id);
		}

		values.put(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, getVirtualGovernorName());
		values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, getGov());
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, getGovernorThresholdUp());
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, getGovernorThresholdDown());
		values.put(DB.VirtualGovernor.NAME_SCRIPT, getScript());
		values.put(DB.VirtualGovernor.NAME_POWERSEAVE_BIAS, getPowersaveBias());
		values.put(DB.VirtualGovernor.NAME_USE_NUMBER_OF_CPUS, getUseNumberOfCpus());
		return values;
	}

	public void applyToProfile(ProfileModel profile) {
		profile.setGov(realGov);
		profile.setGovernorThresholdUp(governorThresholdUp);
		profile.setGovernorThresholdDown(governorThresholdDown);
		profile.setScript(script);
		profile.setPowersaveBias(powersaveBias);
		profile.setUseNumberOfCpus(useNumberOfCpus);
	}

	@Override
	public String getGov() {
		if (realGov == null) {
			return ProfileModel.NO_VALUE_STR;
		}
		return realGov;
	}

	@Override
	public void setGov(String gov) {
		realGov = gov;
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
		result = prime * result + powersaveBias;
		result = prime * result + ((realGov == null) ? 0 : realGov.hashCode());
		result = prime * result + ((script == null) ? 0 : script.hashCode());
		result = prime * result + useNumberOfCpus;
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
		if (powersaveBias != other.powersaveBias)
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
		if (useNumberOfCpus != other.useNumberOfCpus)
			return false;
		if (virtualGov == null) {
			if (other.virtualGov != null)
				return false;
		} else if (!virtualGov.equals(other.virtualGov))
			return false;
		return true;
	}

	@Override
	public int getGovernorThresholdUp() {
		return governorThresholdUp;
	}

	@Override
	public void setGovernorThresholdUp(int i) {
		if (i > -2 && i < 101) {
			this.governorThresholdUp = i;
		}
	}

	@Override
	public int getGovernorThresholdDown() {
		return governorThresholdDown;
	}

	@Override
	public void setGovernorThresholdDown(int i) {
		if (i > -2 && i < 101) {
			this.governorThresholdDown = i;
		}
	}

	@Override
	public void setGovernorThresholdUp(String string) {
		try {
			setGovernorThresholdUp(Integer.parseInt(string));
		} catch (Exception e) {
			Logger.w("Cannot parse " + string + " as int");
		}
	}

	@Override
	public void setGovernorThresholdDown(String string) {
		try {
			setGovernorThresholdDown(Integer.parseInt(string));
		} catch (Exception e) {
			Logger.w("Cannot parse " + string + " as int");
		}
	}

	@Override
	public boolean hasScript() {
		return script != null && !TextUtils.isEmpty(script.trim());
	}

	@Override
	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public String getScript() {
		return script;
	}

	@Override
	public CharSequence getDescription(Context ctx) {
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.getString(R.string.labelGovernor)).append(" ").append(realGov);
		if (governorThresholdUp > 0) {
			sb.append("\n").append(ctx.getString(R.string.labelThreshsUp)).append(" ").append(governorThresholdUp);
		}
		if (governorThresholdDown > 0) {
			sb.append(" ").append(ctx.getString(R.string.labelDown)).append(" ").append(governorThresholdDown);
		}
		if (!TextUtils.isEmpty(script)) {
			sb.append("\n").append(ctx.getString(R.string.labelScript)).append(" ").append(script);
		}
		return sb.toString();
	}

	@Override
	public void setVirtualGovernor(long id) {
		throw new RuntimeException("VirtualGovernorModel does not support setVirtualGovernor");
	}

	@Override
	public long getVirtualGovernor() {
		throw new RuntimeException("VirtualGovernorModel does not support getVirtualGovernor");
	}

	@Override
	public void setPowersaveBias(int powersaveBias) {
		this.powersaveBias = powersaveBias;
	}

	@Override
	public int getPowersaveBias() {
		return powersaveBias;
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
