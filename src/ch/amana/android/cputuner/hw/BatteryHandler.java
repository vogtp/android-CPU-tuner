package ch.amana.android.cputuner.hw;

import java.io.File;

public class BatteryHandler extends HardwareHandler {
	private static final String CURRENT_NOW = "current_now";
	private static final String CURRENT_AVG = "current_avg";
	private static final String BATT_CURRENT = "batt_current";
	private static final String CAPACITY = "capacity";

	public static final String BATTERY_DIR = "/sys/class/power_supply/battery/";
	public static final String BATTERY_CPCAP_DIR = "/sys/devices/platform/cpcap_battery/power_supply/battery/";

	private boolean hasAvgCurrent = true;

	private File CURRENT_NOW_FILE;
	private File CURRENT_AVG_FILE;
	private File CAPACITY_FILE = new File(BATTERY_DIR, CAPACITY);
	private File BATT_CURRENT_FILE;

	private static BatteryHandler instance = null;

	public static BatteryHandler getInstance() {
		if (instance == null) {
			instance = new BatteryHandler();
		}
		return instance;
	}

	private BatteryHandler() {
		CURRENT_NOW_FILE = getBattCurrentFile();
		CURRENT_AVG_FILE = getBattAvgFile();
	}

	public int getBatteryCurrentNow() {
		int current = getIntFromStr(RootHandler.readFile(CURRENT_NOW_FILE));
		if (current == NO_VALUE_INT) {
			return NO_VALUE_INT;
		}
		return Math.abs(current) / 1000;
	}

	public int getBatteryCurrentAverage() {
		int current = getIntFromStr(RootHandler.readFile(CURRENT_AVG_FILE));
		if (current == NO_VALUE_INT) {
			return NO_VALUE_INT;
		}
		return Math.abs(current) / 1000;
	}

	private File getBattCurrentFile() {
		BATT_CURRENT_FILE = getBattCurrentFileInDir(BATTERY_DIR);
		if (!canReadFromBattCurFile(BATT_CURRENT_FILE)) {
			BATT_CURRENT_FILE = getBattCurrentFileInDir(BATTERY_CPCAP_DIR);
		}
		return BATT_CURRENT_FILE;
	}

	private File getBattCurrentFileInDir(String batteryDir) {
		BATT_CURRENT_FILE = new File(batteryDir, CURRENT_NOW);
		if (!canReadFromBattCurFile(BATT_CURRENT_FILE)) {
			BATT_CURRENT_FILE = new File(batteryDir, BATT_CURRENT);
		}
		return BATT_CURRENT_FILE;
	}

	private boolean canReadFromBattCurFile(File batFile) {
		int intFromStr = getIntFromStr(RootHandler.readFile(batFile));
		return intFromStr != NO_VALUE_INT;
	}

	private File getBattAvgFile() {
		CURRENT_AVG_FILE = getBattAvgFileInDir(BATTERY_DIR);
		if (!canReadFromBattAvgFile()) {
			CURRENT_AVG_FILE = getBattAvgFileInDir(BATTERY_CPCAP_DIR);
		}
		if (!canReadFromBattAvgFile()) {
			CURRENT_AVG_FILE = getBattCurrentFile();
			hasAvgCurrent = false;
		}
		return CURRENT_AVG_FILE;
	}

	private File getBattAvgFileInDir(String batteryDir) {
		CURRENT_AVG_FILE = new File(batteryDir, CURRENT_AVG);
		return CURRENT_AVG_FILE;
	}

	private boolean canReadFromBattAvgFile() {
		return getIntFromStr(RootHandler.readFile(CURRENT_AVG_FILE)) != NO_VALUE_INT;
	}

	public int getBatteryLevel() {
		return getIntFromStr(RootHandler.readFile(CAPACITY_FILE));
	}

	public boolean hasAvgCurrent() {
		return hasAvgCurrent;
	}

	public boolean isOnAcPower() {
		// FIXME get it somehow
		return false;
	}

}
