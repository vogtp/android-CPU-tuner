package ch.amana.android.cputuner.hw;

import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ProfileModel;

public class CpuHandlerMulticore extends CpuHandler {

	private static final String CPU_ONLINE = "online";
	private final String[] cpus;

	public CpuHandlerMulticore(String[] cpus) {
		super();
		this.cpus = cpus;
	}

	@Override
	public void applyCpuSettings(ProfileModel profile) {
		super.applyCpuSettings(profile);
		setNumberOfActiveCpus(profile.getUseNumberOfCpus());
	}

	private boolean writeFile(String subDir, String file, String value) {
		if (writeCpuFile(subDir, file, value, -1)) {
			return true;
		}
		for (int i = 0; i < cpus.length; i++) {
			if (!writeCpuFile(subDir, file, value, i)) {
				return false;
			}
		}
		return true;
	}

	private boolean writeCpuFile(String subDir, String file, String value, int i) {
		StringBuilder path = new StringBuilder(CPU_BASE_DIR);
		if (i < -1) {
			path.append("/").append(cpus[i]);
		}
		if (subDir != null) {
			path.append("/").append(subDir);
		}
		return RootHandler.writeFile(getFile(path.toString(), file), value);
	}

	@Override
	public boolean setCurGov(String gov) {
		Logger.i("Setting multicore governor to " + gov);
		return writeFile(CPUFREQ_DIR, SCALING_GOVERNOR, gov);
	}


	@Override
	public boolean setUserCpuFreq(int val) {
		Logger.i("Setting  multicore user frequency to " + val);
		return writeFile(CPUFREQ_DIR, SCALING_SETSPEED, val + "");
	}

	@Override
	public boolean setMaxCpuFreq(int val) {
		Logger.i("Setting multicore max frequency to " + val);
		return writeFile(CPUFREQ_DIR, SCALING_MAX_FREQ, Integer.toString(val));
	}

	@Override
	public boolean setMinCpuFreq(int i) {
		Logger.i("Setting multicore min frequency to " + i);
		return writeFile(CPUFREQ_DIR, SCALING_MIN_FREQ, Integer.toString(i));
	}

	@Override
	public boolean setGovSamplingRate(int i) {
		return writeFile(CPUFREQ_DIR + getCurCpuGov(), GOV_SAMPLING_RATE, i + "");
	}

	@Override
	public boolean setPowersaveBias(int i) {
		if (i < 0) {
			return false;
		}
		return writeFile(CPUFREQ_DIR + getCurCpuGov(), POWERSAVE_BIAS, i + "");
	}

	@Override
	public boolean setGovThresholdUp(int i) {
		if (i < 1) {
			return false;
		}
		if (i > 100) {
			i = 98;
		}
		Logger.i("Setting multicore threshold up to " + i);
		return writeFile(CPUFREQ_DIR + getCurCpuGov(), GOV_TRESHOLD_UP, i + "");
	}

	@Override
	public boolean setGovThresholdDown(int i) {
		if (i < 1) {
			return false;
		}
		if (i > 100) {
			i = 95;
		}
		Logger.i("Setting multicore threshold down to " + i);
		return writeFile(CPUFREQ_DIR + getCurCpuGov(), GOV_TRESHOLD_DOWN, i + "");
	}

	@Override
	public int getNumberOfCpus() {
		return cpus.length;
	}

	@Override
	public void setNumberOfActiveCpus(int activeCpus) {
		if (activeCpus < 1) {
			return;
		}
		int i;
		for (i = 0; i < activeCpus; i++) {
			Logger.i("Switching on cpu"+i);
			writeCpuFile(null, CPU_ONLINE, "1", i);
		}
		for (int j = i; j < getNumberOfCpus(); j++) {
			Logger.i("Switching off cpu" + j);
			writeCpuFile(null, CPU_ONLINE, "0", j);
		}
	}

	@Override
	public int getNumberOfActiveCpus() {
		int count = 0;
		for (int i = 0; i < getNumberOfCpus(); i++) {
			StringBuilder path = new StringBuilder(CPU_BASE_DIR);
			path.append("/").append(cpus[i]);
			String online = RootHandler.readFile(getFile(path.toString(), CPU_ONLINE));
			if ("1".equals(online)) {
				count++;
				if (Logger.DEBUG) {
					Logger.d("CPU" + i + " is online");
				}
			}
		}
		Logger.d("Found " + count + " online cpus");
		return count;
	}
}
