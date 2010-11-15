package ch.amana.android.cputuner.hw;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class ServicesHandler {

	public static void wifi(Context ctx, boolean enabled) {
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(enabled);
		Log.i(Logger.TAG, "Switched Wifi to " + enabled);
	}

	public static void gps(Context ctx, boolean enabled) {
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

	public static void bluetooth(Context ctx, boolean enabled) {
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

	public static void mobiledata(Context context, boolean b) {
		// TODO Auto-generated method stub

	}
}
