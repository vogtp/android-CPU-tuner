package ch.amana.android.cputuner.hw;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class ServicesHandler {

	private static final int MODE_GSM_ONLY = 1;
	private static final int MODE_GSM_WCDMA_PREFERRD = 0;
	private static final String MODIFY_NETWORK_MODE = "com.android.internal.telephony.MODIFY_NETWORK_MODE";
	private static final String NETWORK_MODE = "networkMode";

	public static void enableWifi(Context ctx, boolean enabled) {
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(enabled);
		Log.i(Logger.TAG, "Switched Wifi to " + enabled);
	}

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
			}
		}

		if (changed) {
			try {
				Settings.Secure.putString(resolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, providers);
				Log.i(Logger.TAG, "Switched GPS to " + enabled);
			} catch (Exception e) {
			}
		}

	}

	public static void enableBluetooth(Context ctx, boolean enabled) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return;
		}
		if (enabled) {
			bluetoothAdapter.enable();
		} else {
			bluetoothAdapter.disable();
		}
		Log.i(Logger.TAG, "Switched bluethooth to " + enabled);
	}

	// From:
	// /cyanogen/frameworks/base/services/java/com/android/server/status/widget/NetworkModeButton.java
	// line 97
	public static void enable2gOnly(Context context, boolean b) {

		/**
		 * The preferred network mode 7 = Global 6 = EvDo only 5 = CDMA w/o EvDo
		 * 4 = CDMA / EvDo auto 3 = GSM / WCDMA auto 2 = WCDMA only 1 = GSM only
		 * 0 = GSM / WCDMA preferred
		 * 
		 */
		Intent intent = new Intent(MODIFY_NETWORK_MODE);
		if (b) {
			intent.putExtra(NETWORK_MODE, MODE_GSM_ONLY);
		} else {
			intent.putExtra(NETWORK_MODE, MODE_GSM_WCDMA_PREFERRD);
		}
		context.sendBroadcast(intent);
		Log.i(Logger.TAG, "Switched 2G/3G to " + b);
	}

	public static void enableBackgroundSync(Context context, boolean b) {
		if (b) {
			ContentResolver.setMasterSyncAutomatically(true);
		} else {
			ContentResolver.setMasterSyncAutomatically(false);
		}
	}

}
