package ch.amana.android.cputuner.model;

import android.content.Context;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.CpuHandlerMulticore;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;

public class HardwareGovernorModel implements IGovernorModel {

	private final CpuHandler cpuHandler;
	private final PowerProfiles powerProfiles;

	public HardwareGovernorModel(Context ctx) {
		this.cpuHandler = CpuHandler.getInstance();
		this.powerProfiles = PowerProfiles.getInstance(ctx);
	}

	@Override
	public int getGovernorThresholdUp() {
		return cpuHandler.getGovThresholdUp();
	}

	@Override
	public int getGovernorThresholdDown() {
		return cpuHandler.getGovThresholdDown();
	}

	@Override
	public void setGov(String gov) {
		cpuHandler.setCurGov(gov);
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
	public void setScript(String string) {
		// not used

	}

	@Override
	public String getGov() {
		return cpuHandler.getCurCpuGov();
	}

	@Override
	public String getScript() {
		// not used
		return "";
	}

	@Override
	public void setGovernorThresholdUp(int i) {
		cpuHandler.setGovThresholdUp(i);
	}

	@Override
	public void setGovernorThresholdDown(int i) {
		cpuHandler.setGovThresholdDown(i);
	}

	@Override
	public void setVirtualGovernor(long id) {
		powerProfiles.getCurrentProfile().setVirtualGovernor(id);
	}

	@Override
	public long getVirtualGovernor() {
		if (powerProfiles == null || powerProfiles.getCurrentProfile() == null) {
			return -1;
		}
		return powerProfiles.getCurrentProfile().getVirtualGovernor();
	}

	@Override
	public void setPowersaveBias(int powersaveBias) {
		cpuHandler.setPowersaveBias(powersaveBias);
	}

	@Override
	public int getPowersaveBias() {
		return cpuHandler.getPowersaveBias();
	}

	@Override
	public boolean hasScript() {
		return false;
	}

	@Override
	public void setUseNumberOfCpus(int position) {
		cpuHandler.setNumberOfActiveCpus(position);
	}

	@Override
	public int getUseNumberOfCpus() {
		return cpuHandler.getNumberOfActiveCpus();
	}

	@Override
	public CharSequence getDescription(Context ctx) {
		if (ctx == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.getString(R.string.labelGovernor)).append(" ").append(getGov());
		int governorThresholdUp = getGovernorThresholdUp();
		if (governorThresholdUp > 0) {
			sb.append("\n").append(ctx.getString(R.string.labelThreshsUp)).append(" ").append(governorThresholdUp);
		}
		int governorThresholdDown = getGovernorThresholdDown();
		if (governorThresholdDown > 0) {
			sb.append(" ").append(ctx.getString(R.string.labelDown)).append(" ").append(governorThresholdDown);
		}
		if (cpuHandler instanceof CpuHandlerMulticore) {
			int useNumberOfCpus = getUseNumberOfCpus();
			int numberOfCpus = cpuHandler.getNumberOfCpus();
			if (useNumberOfCpus < 1 || useNumberOfCpus > numberOfCpus) {
				useNumberOfCpus = numberOfCpus;
			}
			sb.append("\n").append(ctx.getString(R.string.labelActiveCpus)).append(" ").append(useNumberOfCpus);
			sb.append("/").append(numberOfCpus);
		}
		return sb.toString();
	}

}