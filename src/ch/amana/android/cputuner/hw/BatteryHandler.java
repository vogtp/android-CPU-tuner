package ch.amana.android.cputuner.hw;

public class BatteryHandler extends HardwareHandler {
	// FIXME use batt_current as well
	private static final String CURRENT_NOW = "current_now";
	private static final String CURRENT_AVG = "current_avg";
	private static final String BATT_CURRENT = "batt_current";
	private static final String CAPACITY = "capacity";

	public static final String BATTERY_DIR = "/sys/class/power_supply/battery/";

	public static int getBatteryCurrentNow() {
		int current = getIntFromStr(readFile(CURRENT_NOW));
		if (current == NO_VALUE_INT) {
			current = getIntFromStr(readFile(BATT_CURRENT));
		}
		return Math.abs(current) / 1000;
	}

	public static int getBatteryCurrentAverage() {
		int current = getIntFromStr(readFile(CURRENT_AVG));
		if (current == NO_VALUE_INT) {
			current = getIntFromStr(readFile(BATT_CURRENT));
		}
		return Math.abs(current) / 1000;
	}

	public static int getBatteryLevel() {
		return getIntFromStr(readFile(CAPACITY));
	}

	private static String readFile(String file) {
		return RootHandler.readFile(BATTERY_DIR, file);
	}

	public static boolean isOnAcPower() {
		// FIXME get it somehow
		return false;
	}

}
