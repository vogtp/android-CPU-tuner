package ch.amana.android.cputuner.hw;

public class DeviceInformation {

	private static final String NOT_AVAILABLE = "not available";

	private static String getProp(String prop) {
		StringBuilder result = new StringBuilder();
		if (RootHandler.execute("getprop " + prop, result)) {
			return result.toString();
		}
		return NOT_AVAILABLE;
	}

	public static String getAndroidRelease() {
		return getProp("ro.build.version.release");
	}

	public static String getDeviceModel() {
		return getProp("ro.product.model");
	}

	public static String getManufacturer() {
		return getProp("ro.product.manufacturer");
	}

	public static String getRomManagerDeveloperId() {
		return getProp("ro.rommanager.developerid");
	}

	public static String getModVersion() {
		return getProp("ro.modversion");
	}

	public static String getDeviceNick() {
		return getProp("ro.product.device");
	}

	public static String get() {
		return getProp("");
	}
}
