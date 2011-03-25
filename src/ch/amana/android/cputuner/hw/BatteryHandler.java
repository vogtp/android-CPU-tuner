package ch.amana.android.cputuner.hw;

import java.io.File;

public class BatteryHandler extends HardwareHandler {
	private static final String CURRENT_NOW = "current_now";
	private static final String CURRENT_AVG = "current_avg";
	private static final String BATT_CURRENT = "batt_current";
	private static final String CAPACITY = "capacity";

	public static final String BATTERY_DIR = "/sys/class/power_supply/battery/";
	public static final String BATTERY_CPCAP_DIR = "/sys/devices/platform/cpcap_battery/power_supply/battery/";
	private static final File CURRENT_NOW_FILE = new File(BATTERY_DIR, CURRENT_NOW);
	private static final File CURRENT_AVG_FILE = new File(BATTERY_DIR, CURRENT_AVG);
	private static final File CAPACITY_FILE = new File(BATTERY_DIR, CAPACITY);
	private static File BATT_CURRENT_FILE;

	public static int getBatteryCurrentNow() {
		int current = getIntFromStr(RootHandler.readFile(CURRENT_NOW_FILE));
		if (current == NO_VALUE_INT) {
			current = getIntFromStr(RootHandler.readFile(getBattCurrentFile()));
		}
		if (current == NO_VALUE_INT) {
			return NO_VALUE_INT;
		}
		return Math.abs(current) / 1000;
	}

	public static int getBatteryCurrentAverage() {
		int current = getIntFromStr(RootHandler.readFile(CURRENT_AVG_FILE));
		if (current == NO_VALUE_INT) {
			current = getIntFromStr(RootHandler.readFile(getBattAvgFile()));
		}
		if (current == NO_VALUE_INT) {
			return NO_VALUE_INT;
		}
		return Math.abs(current) / 1000;
	}

	public static File getBattCurrentFile() {
		if (!canReadFromBattCurFile()) {
			BATT_CURRENT_FILE = new File(BATTERY_DIR, BATT_CURRENT);
		}
		if (!canReadFromBattCurFile()) {
			BATT_CURRENT_FILE = new File(BATTERY_CPCAP_DIR, CURRENT_NOW);
		}
		if (!canReadFromBattCurFile()) {
			BATT_CURRENT_FILE = new File(BATTERY_CPCAP_DIR, BATT_CURRENT);
		}
		return BATT_CURRENT_FILE;
	}

	private static boolean canReadFromBattCurFile() {
		return getIntFromStr(RootHandler.readFile(CURRENT_NOW_FILE)) != NO_VALUE_INT;
	}

	public static File getBattAvgFile() {
		if (!canReadFromBattAvgFile()) {
			BATT_CURRENT_FILE = new File(BATTERY_CPCAP_DIR, CURRENT_AVG);
		}
		if (!canReadFromBattAvgFile()) {
			BATT_CURRENT_FILE = new File(BATTERY_CPCAP_DIR, BATT_CURRENT);
		}
		return BATT_CURRENT_FILE;
	}

	private static boolean canReadFromBattAvgFile() {
		return getIntFromStr(RootHandler.readFile(CURRENT_NOW_FILE)) != NO_VALUE_INT;
	}

	public static int getBatteryLevel() {
		return getIntFromStr(RootHandler.readFile(CAPACITY_FILE));
	}

	public static boolean isOnAcPower() {
		// FIXME get it somehow
		return false;
	}

}
