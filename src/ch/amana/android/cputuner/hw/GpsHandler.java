package ch.amana.android.cputuner.hw;

import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;
import ch.amana.android.cputuner.helper.Logger;

public class GpsHandler {

	// private static Boolean enableGps = null;
	//
	// public static boolean isEnableSwitchGps(Context ctx) {
	// if (enableGps == null) {
	// enableGps = new Boolean(checkEnableSwitchGps(ctx));
	// }
	// return enableGps.booleanValue();
	// }

	public static boolean isEnableSwitchGps(Context ctx) {
		// try {
		// ContentResolver resolver = ctx.getContentResolver();
		// Settings.Secure.isLocationProviderEnabled(resolver,
		// Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// return true;
		// } catch (Throwable e) {
		// Log.d(Logger.TAG, "Cannot access GPS by new interface, per 2.2?");
		// }
		return RootHandler.isSystemApp(ctx);
	}

	// public static void enableGps(Context ctx, boolean enabled) {
	// if (!enableGps) {
	// return;
	// }
	// ContentResolver resolver = ctx.getContentResolver();
	// // try {
	// // Settings.Secure.setLocationProviderEnabled(resolver,
	// // Settings.Secure.LOCATION_PROVIDERS_ALLOWED, enabled);
	// // } catch (Throwable e) {
	// enableGpsPre22(resolver, enabled);
	// // }
	// }

	public static void enableGps(Context ctx, boolean enabled) {
		ContentResolver resolver = ctx.getContentResolver();
		String providers = Settings.Secure.getString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		boolean changed = false;

		int idx = providers.indexOf(LocationManager.GPS_PROVIDER);
		if (enabled) {
			if (idx == -1) {
				if (providers.length() != 0) {
					providers += ',';
				}
				providers += LocationManager.GPS_PROVIDER;
				changed = true;
			}
		} else {
			if (idx > -1) {
				String tmp = providers.substring(0, idx);
				int sepIdx = providers.indexOf(',', idx);
				if (sepIdx > -1) {
					tmp += providers.substring(sepIdx + 1);
				}
				if (tmp.endsWith(",")) {
					tmp = tmp.substring(0, tmp.length() - 1);
				}
				providers = tmp;
				changed = true;
			}
		}

		if (changed) {
			try {
				Settings.Secure.putString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, providers);
				Logger.i("Switched GPS to " + enabled);
			} catch (Exception e) {
			}
		}

	}

}
