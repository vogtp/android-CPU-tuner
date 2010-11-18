package ch.amana.android.cputuner.hw;

import java.io.BufferedReader;
import java.io.FileReader;

import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.CpuModel;

public class CpuHandler {

	public static final String NOT_AVAILABLE = "not available";

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

	private final Object semaphore = new Object();

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
	}

	public int getCurCpuFreq() {
		int i = -1;
		String intString = readFile(SCALING_CUR_FREQ);
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot parse " + intString + " as interger");
		}
		return i;
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
		int i = -1;
		String intString = readFile(SCALING_MAX_FREQ);
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot parse " + intString + " as interger");
		}
		return i;
	}

	public boolean setUserCpuFreq(int val) {
		return writeFile(SCALING_SETSPEED, val + "");
	}

	public boolean setMaxCpuFreq(int val) {
		return writeFile(SCALING_MAX_FREQ, val + "");
	}

	public int getUserCpuFreq() {
		int i = -1;
		String intString = readFile(SCALING_SETSPEED);
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot parse " + intString + " as interger");
		}
		return i;
	}

	public int getMinCpuFreq() {
		int i = -1;
		String intString = readFile(SCALING_MIN_FREQ);
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot parse " + intString + " as interger");
		}
		return i;
	}

	public boolean setMinCpuFreq(int i) {
		return writeFile(SCALING_MIN_FREQ, i + "");
	}

	public int[] getAvailCpuFreq() {
		return createListInt(readFile(SCALING_AVAILABLE_FREQUENCIES));
	}

	private String[] moveCurListElementTop(String[] list, String topElement) {
		if (list == null || list.length < 2) {
			return list;
		}
		String firstElement = list[0];
		for (int i = 0; i < list.length; i++) {
			if (topElement.equals(list[i])) {
				list[i] = firstElement;
				list[0] = topElement;
			}
		}
		return list;
	}

	private int[] createListInt(String listString) {
		Log.d(Logger.TAG, "Creating array from >" + listString + "<");
		if (NOT_AVAILABLE.equals(listString)) {
			int[] list = new int[1];
			list[0] = -1;
			return list;
		}
		String[] strList = listString.split(" ");
		int[] lst = new int[strList.length];
		for (int i = 0; i < strList.length; i++) {
			try {
				lst[i] = Integer.parseInt(strList[i]);
			} catch (Exception e) {
				lst[i] = -1;
			}
		}
		return lst;
	}

	private String[] createListStr(String listString) {
		Log.d(Logger.TAG, "Creating array from >" + listString + "<");
		if (NOT_AVAILABLE.equals(listString)) {
			String[] list = new String[1];
			list[0] = listString;
			return list;
		}
		return listString.split(" +");
	}

	private String readFile(String filename) {
		synchronized (semaphore) {
			String val = "";
			BufferedReader reader;
			try {
				Log.v(Logger.TAG, "Reading file >" + filename + "<");
				reader = new BufferedReader(new FileReader(CPU_DIR + filename));
				String line = reader.readLine();
				while (line != null && !line.trim().equals("")) {
					Log.v(Logger.TAG, "Read line >" + line + "<");
					val += line;
					line = reader.readLine();
				}
				reader.close();
			} catch (Throwable e) {
				Log.e(Logger.TAG, "Cannot open for reading " + filename, e);
			}
			if (val.trim().equals("")) {
				val = NOT_AVAILABLE;
			}
			return val;
		}
	}

	private boolean writeFile(String filename, String val) {
		if (val == null || val.equals(readFile(filename))) {
			return false;
		}
		synchronized (semaphore) {
			String path = CPU_DIR + filename;
			Log.w(Logger.TAG, "Setting " + path + " to " + val);
			return RootHandler.execute("echo " + val + " > " + path);
		}
	}

	public boolean hasGov() {
		return !NOT_AVAILABLE.equals(getCurCpuGov());
	}

}
