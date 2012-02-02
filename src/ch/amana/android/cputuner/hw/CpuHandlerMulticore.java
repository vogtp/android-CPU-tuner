package ch.amana.android.cputuner.hw;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.IGovernorModel;
import ch.amana.android.cputuner.model.ProfileModel;

public class CpuHandlerMulticore extends CpuHandler {

	private static final String CPU_ONLINE = "online";
	private final String[] cpus;

	private final Map<String, File[]> fileMap = new WeakHashMap<String, File[]>();

	public CpuHandlerMulticore(String[] cpus) {
		super();
		this.cpus = cpus;
	}

	@Override
	public void applyCpuSettings(ProfileModel profile) {
		super.applyCpuSettings(profile);
		setNumberOfActiveCpus(profile.getUseNumberOfCpus());
	}

	@Override
	public void applyGovernorSettings(IGovernorModel governor) {
		super.applyGovernorSettings(governor);
		setNumberOfActiveCpus(governor.getUseNumberOfCpus());
	}

	@Override
	public boolean setCurGov(String gov) {
		Logger.i("Setting multicore governor to " + gov);
		return writeFiles(getFiles(SCALING_GOVERNOR), gov);
	}

	@Override
	public boolean setUserCpuFreq(int val) {
		Logger.i("Setting  multicore user frequency to " + val);
		return writeFiles(getFiles(SCALING_SETSPEED), val + "");
	}

	@Override
	public boolean setMaxCpuFreq(int val) {
		Logger.i("Setting multicore max frequency to " + val);
		return writeFiles(getFiles(SCALING_MAX_FREQ), Integer.toString(val));
	}

	@Override
	public boolean setMinCpuFreq(int i) {
		Logger.i("Setting multicore min frequency to " + i);
		return writeFiles(getFiles(SCALING_MIN_FREQ), Integer.toString(i));
	}

	@Override
	public boolean setGovSamplingRate(int i) {
		return writeFiles(getFiles(GOV_SAMPLING_RATE, getCurCpuGov()), i + "");
	}

	@Override
	public boolean setPowersaveBias(int i) {
		if (i < 0) {
			return false;
		}
		return writeFiles(getFiles(POWERSAVE_BIAS, getCurCpuGov()), i + "");
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
		return writeFiles(getFiles(GOV_TRESHOLD_UP, getCurCpuGov()), i + "");
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
		return writeFiles(getFiles(GOV_TRESHOLD_DOWN, getCurCpuGov()), i + "");
	}

	@Override
	public int getNumberOfCpus() {
		return cpus.length;
	}

	@Override
	public void setNumberOfActiveCpus(int activeCpus) {
		if (activeCpus < 1) {
			activeCpus = getNumberOfCpus();
		}
		int i;
		File[] cpuOnlineFiles = getFiles(CPU_ONLINE);
		for (i = 0; i < activeCpus; i++) {
			Logger.i("Switching on cpu" + i);
			writeFiles(cpuOnlineFiles, "1", i);
		}
		for (int j = i; j < getNumberOfCpus(); j++) {
			Logger.i("Switching off cpu" + j);
			writeFiles(cpuOnlineFiles, "0", j);
		}
	}

	@Override
	public int getNumberOfActiveCpus() {
		int count = 0;
		for (int i = 0; i < getNumberOfCpus(); i++) {
			StringBuilder path = new StringBuilder(CPU_BASE_DIR);
			path.append("/").append(cpus[i]);
			String online = RootHandler.readFile(getFiles(CPU_ONLINE)[i]);
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

	protected File[] getFiles(String name) {
		return getFiles(name, "");
	}

	protected File[] getFiles(String name, String subDir) {
		String idx = name + subDir;
		File[] files = fileMap.get(idx);
		if (files == null) {
			File file;
			file = new File(CPU_BASE_DIR + cpus[0] + subDir, name);
			if (fileOk(file)) {
				// get the other cpu files
				files = new File[cpus.length];
				files[0] = file;
				for (int i = 1; i < cpus.length; i++) {
					file = new File(CPU_BASE_DIR + cpus[i] + subDir, name);
					if (fileOk(file)) {
						files[i] = file;
					} else {
						files[i] = CpuHandler.DUMMY_FILE;
					}
				}
			} else {
				file = new File(CPU_BASE_DIR + CPUFREQ_DIR + subDir, name);
				if (fileOk(file)) {
					files = new File[1];
					files[0] = file;
				} else {
					file = new File(CPU_BASE_DIR + subDir, name);
					if (fileOk(file)) {
						files = new File[1];
						files[0] = file;
					} else {
						file = new File(CPU_BASE_DIR + cpus[0] + CPUFREQ_DIR + subDir, name);
						if (fileOk(file)) {
							// get the other cpu files
							files = new File[cpus.length];
							files[0] = file;
							for (int i = 1; i < cpus.length; i++) {
								file = new File(CPU_BASE_DIR + cpus[i] + CPUFREQ_DIR + subDir, name);
								if (fileOk(file)) {
									files[i] = file;
								} else {
									files[i] = CpuHandler.DUMMY_FILE;
								}
							}
						}

					}
				}
			}

			if (files == null) {
				files = new File[0];
			}

			fileMap.put(idx, files);
		}
		if (Logger.DEBUG) {
			String s = "";
			for (int i = 0; i < files.length; i++) {
				s = s + " " + files[i].getAbsolutePath();
			}
			Logger.w("Files for " + name + ": " + s);
		}
		return files;
	}

	private boolean fileOk(File file) {
		if (file == null || !file.exists()) {
			return false;
		}
		return !"/".equals(file.getAbsolutePath());
	}

	private boolean writeFiles(File[] files, String value) {
		boolean ret = false;
		for (int i = 0; i < files.length; i++) {
			if (RootHandler.writeFile(files[i], value)) {
				ret = true;
			}
		}
		return ret;
	}

	private boolean writeFiles(File[] files, String value, int i) {
		return RootHandler.writeFile(files[i], value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (File[] files : fileMap.values()) {
			for (int i = 0; i < files.length; i++) {
				sb.append(files[i].getAbsolutePath()).append("\n");
			}
		}
		return sb.toString();
	}
}
