package ch.amana.android.cputuner.hw;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.CpuModel;

public class CpuHandler extends HardwareHandler {

	public static final String GOV_ONDEMAND = "ondemand";
	public static final String GOV_POWERSAVE = "powersave";
	public static final String GOV_CONSERVATIVE = "conservative";
	public static final String GOV_PERFORMANCE = "performance";
	public static final String GOV_INTERACTIVE = "interactive";
	public static final String GOV_USERSPACE = "userspace";

	private static final String CPU_DIR = "/sys/devices/system/cpu/cpu0/cpufreq/";

	private static final String SCALING_GOVERNOR = "scaling_governor";
	private static final String SCALING_MAX_FREQ = "scaling_max_freq";
	private static final String SCALING_MIN_FREQ = "scaling_min_freq";
	private static final String SCALING_SETSPEED = "scaling_setspeed";
	private static final String SCALING_CUR_FREQ = "scaling_cur_freq";
	private static final String SCALING_AVAILABLE_GOVERNORS = "scaling_available_governors";
	private static final String SCALING_AVAILABLE_FREQUENCIES = "scaling_available_frequencies";

	private static final String GOV_TRESHOLD_UP = "up_threshold";
	private static final String GOV_TRESHOLD_DOWN = "down_threshold";

	// FIXME convert to static helper
	private static CpuHandler instance = null;

	public static CpuHandler getInstance() {
		if (instance == null) {
			instance = new CpuHandler();
		}
		return instance;
	}

	public CpuModel getCurrentCpuSettings() {
		return new CpuModel(getCurCpuGov(), getMaxCpuFreq(), getMinCpuFreq());
	}

	public void applyCpuSettings(CpuModel cpu) {
		setCurGov(cpu.getGov());
		if (GOV_USERSPACE.equals(cpu.getGov())) {
			setUserCpuFreq(cpu.getMaxFreq());
		} else {
			setMaxCpuFreq(cpu.getMaxFreq());
			setMinCpuFreq(cpu.getMinFreq());
		}
		setGovThresholdUp(cpu.getGovernorThresholdUp());
		setGovThresholdDown(cpu.getGovernorThresholdDown());
	}

	public int getCurCpuFreq() {
		return getIntFromStr(readFile(SCALING_CUR_FREQ));
	}

	public String getCurCpuGov() {
		return readFile(SCALING_GOVERNOR);
	}

	public boolean setCurGov(String gov) {
		return writeFile(SCALING_GOVERNOR, gov);
	}

	public String[] getAvailCpuGov() {
		return moveCurListElementTop(createListStr(readFile(SCALING_AVAILABLE_GOVERNORS)), getCurCpuGov());
	}

	public boolean hasGovernor(String governor) {
		return readFile(SCALING_AVAILABLE_GOVERNORS).contains(governor);
	}

	public int getMaxCpuFreq() {
		return getIntFromStr(readFile(SCALING_MAX_FREQ));
	}

	public boolean setUserCpuFreq(int val) {
		return writeFile(SCALING_SETSPEED, val + "");
	}

	public boolean setMaxCpuFreq(int val) {
		if (val <= getMinCpuFreq()) {
			return false;
		}
		return writeFile(SCALING_MAX_FREQ, val + "");
	}

	public int getUserCpuFreq() {
		return getIntFromStr(readFile(SCALING_SETSPEED));
	}

	public int getMinCpuFreq() {
		return getIntFromStr(readFile(SCALING_MIN_FREQ));
	}

	public boolean setMinCpuFreq(int i) {
		if (i >= getMaxCpuFreq()) {
			return false;
		}
		return writeFile(SCALING_MIN_FREQ, i + "");
	}

	public int getGovThresholdUp() {
		return getIntFromStr(readFile(CPU_DIR + getCurCpuGov(), GOV_TRESHOLD_UP));
	}

	public int getGovThresholdDown() {
		return getIntFromStr(readFile(CPU_DIR + getCurCpuGov(), GOV_TRESHOLD_DOWN));
	}

	public boolean setGovThresholdUp(int i) {
		if (i < 0 || i > 100 || i <= getGovThresholdDown()) {
			i = 98;
		}
		return writeFile(CPU_DIR + getCurCpuGov(), GOV_TRESHOLD_UP, i + "");
	}

	public boolean setGovThresholdDown(int i) {
		if (i < 0 || i > 100 || i >= getGovThresholdUp()) {
			i = 95;
		}
		return writeFile(CPU_DIR + getCurCpuGov(), GOV_TRESHOLD_DOWN, i + "");
	}

	public int[] getAvailCpuFreq() {
		int[] freqs = createListInt(readFile(SCALING_AVAILABLE_FREQUENCIES));
		if (SettingsStorage.getInstance().isPowerUser()) {
			return freqs;
		}

		List<Integer> freqList = new ArrayList<Integer>(freqs.length);
		for (int i = 0; i < freqs.length; i++) {
			if (freqs[i] > getMinimumSensibleFrequency()) {
				freqList.add(freqs[i]);
			}
		}
		freqs = new int[freqList.size()];
		int i = 0;
		for (Iterator<Integer> iterator = freqList.iterator(); iterator.hasNext();) {
			freqs[i++] = iterator.next();
		}
		return freqs;
	}

	public int getMinimumSensibleFrequency() {
		return 400000;
	}

	private String readFile(String filename) {
		return readFile(CPU_DIR, filename);
	}

	private String readFile(String dir, String filename) {
		return RootHandler.readFile(dir, filename);
	}

	private boolean writeFile(String filename, String val) {
		return writeFile(CPU_DIR, filename, val);
	}

	private boolean writeFile(String dir, String filename, String val) {
		if (val == null || val.equals(readFile(dir, filename))) {
			return false;
		}
		synchronized (filename) {
			String path = dir + "/" + filename;
			Logger.w("Setting " + path + " to " + val);
			return RootHandler.execute("echo " + val + " > " + path);
		}
	}

	public boolean hasGov() {
		return !RootHandler.NOT_AVAILABLE.equals(getCurCpuGov());
	}

}
