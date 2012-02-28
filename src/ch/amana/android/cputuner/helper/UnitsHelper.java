package ch.amana.android.cputuner.helper;

import ch.amana.android.cputuner.log.Logger;

public class UnitsHelper {

	public static final String UNITS_DEFAULT = "1";
	private static final String UNITS_IMPERIAL = "2";
	private static final String UNITS_SI = "3";

	public static String temperature(int temperature) {
		String unitSystem = SettingsStorage.getInstance().getUnitSystem();
		if (UNITS_IMPERIAL.equals(unitSystem)) {
			return c2f(temperature) + " °F";
		} else if (UNITS_SI.equals(unitSystem)) {
			return c2si(temperature) + " K";
		}
		return temperature + " °C";
	}

	private static int c2si(int temperature) {
		return temperature + 273;
	}

	private static String c2f(int temperature) {
		try {
			float f = temperature;
			f = f * 1.8f;
			f = f + 32f;
			// Float will be rounded here and changed to an integer
			int i = Math.round(f);
			return Integer.toString(i);
		} catch (Exception e) {
			Logger.e("Cannot convert celsius to farenheit", e);
			return "error";
		}
	}


}
