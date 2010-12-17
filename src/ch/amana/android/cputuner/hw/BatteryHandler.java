package ch.amana.android.cputuner.hw;

import java.io.File;

public class BatteryHandler extends HardwareHandler {
	private static final String CURRENT_NOW = "current_now";
	private static final String CURRENT_AVG = "current_avg";
	private static final String BATT_CURRENT = "batt_current";
	private static final String CAPACITY = "capacity";

	public static final String BATTERY_DIR = "/sys/class/power_supply/battery/";
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
			current = getIntFromStr(RootHandler.readFile(getBattCurrentFile()));
		}
		if (current == NO_VALUE_INT) {
			return NO_VALUE_INT;
		}
		return Math.abs(current) / 1000;
	}

	public static File getBattCurrentFile() {
		if (BATT_CURRENT_FILE == null) {
			BATT_CURRENT_FILE = new File(BATTERY_DIR, BATT_CURRENT);
		}
		return BATT_CURRENT_FILE;
	}

	public static int getBatteryLevel() {
		return getIntFromStr(RootHandler.readFile(CAPACITY_FILE));
	}

	public static boolean isOnAcPower() {
		// FIXME get it somehow
		return false;
	}

}
