package ch.amana.android.cputuner.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.RootHandler;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class Cpu {

	public static final String NOT_AVAILABLE = "not available";

	private static final String CPU_DIR = "/sys/devices/system/cpu/cpu0/cpufreq/";

	private static final String SCALING_GOVERNOR = "scaling_governor";
	private static final String SCALING_CUR_FREQ = "scaling_cur_freq";
	private static final String SCALING_AVAILABLE_GOVERNORS = "scaling_available_governors";
	private static final String SCALING_AVAILABLE_FREQUENCIES = "scaling_available_frequencies";

	private static final String SCALING_MAX_FREQ = "scaling_max_freq";
	private static final String SCALING_MIN_FREQ = "scaling_min_freq";

	private Object semaphore = new Object();

	public String getCurCpuFreq() {
		return readFile(SCALING_CUR_FREQ);
	}

	public String getCurCpuGov() {
		return readFile(SCALING_GOVERNOR);
	}

	public boolean setCurGov(String gov) {
		return writeFile(SCALING_GOVERNOR, gov);
	}

	public String[] getAvailCpuGov() {
		return moveCurListElementTop(createList(readFile(SCALING_AVAILABLE_GOVERNORS)), getCurCpuGov());
	}

	public String getMaxCpuFreq() {
		return readFile(SCALING_MAX_FREQ);
	}

	public boolean setMaxCpuFreq(String val) {
		return writeFile(SCALING_MAX_FREQ, val);
	}

	public String getMinCpuFreq() {
		return readFile(SCALING_MIN_FREQ);
	}

	public boolean setMinCpuFreq(String val) {
		return writeFile(SCALING_MIN_FREQ, val);
	}

	public String[] getAvailCpuFreq() {
		return createList(readFile(SCALING_AVAILABLE_FREQUENCIES));
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

	private String[] createList(String listString) {
		if (NOT_AVAILABLE.equals(listString)) {
			String[] list = new String[1];
			list[0] = listString;
			return list;
		}
		return listString.split(" ");
	}

	private String readFile(String filename) {
		synchronized (semaphore) {
			String val = NOT_AVAILABLE;
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(CPU_DIR + filename));
				val = reader.readLine();
				reader.close();
			} catch (IOException e) {
				Log.e(Logger.TAG, "Cannot open for reading " + filename, e);
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
			SettingsStorage.getInstance().writeValue(filename, val);
			Log.w(Logger.TAG, "Setting " + path + " to " + val);
			return RootHandler.execute("echo " + val + " > " + path);
		}
	}

	public boolean hasGov() {
		return !NOT_AVAILABLE.equals(getCurCpuGov());
	}

	public void applyFromStorage() {
		SettingsStorage storage = SettingsStorage.getInstance();
		Set<String> keys = storage.getKeys();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			writeFile(key, storage.getValue(key));
		}
	}

}
