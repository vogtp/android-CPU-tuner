package ch.amana.android.cputuner.hw;

public class BatteryHandler extends HardwareHandler {
	private static final String CURRENT_NOW = "current_now";
	private static final String CURRENT_AVG = "current_avg";
	private static final String CAPACITY = "capacity";

	private static final String BATTERY_DIR = "/sys/class/power_supply/battery/";

	public static int getBatteryCurrentNow() {
		return Math.abs(getIntFromStr(readFile(CURRENT_NOW)) / 1000);
	}

	public static int getBatteryCurrentAverage() {
		return Math.abs(getIntFromStr(readFile(CURRENT_AVG)) / 1000);
	}

	public static int getBatteryLevel() {
		return getIntFromStr(readFile(CAPACITY));
	}

	private static String readFile(String file) {
		return RootHandler.readFile(BATTERY_DIR, file);
	}

	public static boolean isOnAcPower() {
		// TODO Auto-generated method stub
		return false;
	}

}
