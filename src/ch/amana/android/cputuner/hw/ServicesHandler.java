package ch.amana.android.cputuner.hw;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;
import ch.amana.android.cputuner.helper.Logger;

public class ServicesHandler {

	private static final int MODE_GSM_ONLY = 1;
	private static final int MODE_GSM_WCDMA_PREFERRD = 0;
	private static final String MODIFY_NETWORK_MODE = "com.android.internal.telephony.MODIFY_NETWORK_MODE";
	private static final String NETWORK_MODE = "networkMode";
	private static WifiManager wifi;

	public static void enableWifi(Context ctx, boolean enabled) {
		if (wifi == null) {
			wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		}
		wifi.setWifiEnabled(enabled);
		Logger.i("Switched Wifi to " + enabled);
	}

	public static boolean isWifiEnabaled(Context ctx) {
		if (wifi == null) {
			wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		}
		return wifi.isWifiEnabled();
	}

	public static boolean isGpsEnabled(Context ctx) {
		return GpsHandler.isGpxEnabled(ctx);
	}

	public static void enableGps(Context ctx, boolean enabled) {
		GpsHandler.enableGps(ctx, enabled);
	}

	public static boolean isBlutoothEnabled() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return false;
		}
		return bluetoothAdapter.isEnabled();
	}

	public static void enableBluetooth(boolean enabled) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return;
		}
		if (bluetoothAdapter.isEnabled() == enabled) {
			Logger.i("Allready correct bluethooth state ");
			return;
		}
		if (enabled) {
			bluetoothAdapter.enable();
		} else {
			bluetoothAdapter.disable();
		}
		Logger.i("Switched bluethooth to " + enabled);
	}

	public static boolean is2gOnlyEnabled(Context context) {
		return getMobiledataStae(context) == MODE_GSM_ONLY;
	}

	// From:
	// /cyanogen/frameworks/base/services/java/com/android/server/status/widget/NetworkModeButton.java
	// line 97
	public static void enable2gOnly(Context context, boolean b) {
		// FIXME check if correct state first
		/**
		 * The preferred network mode 7 = Global 6 = EvDo only 5 = CDMA w/o EvDo
		 * 4 = CDMA / EvDo auto 3 = GSM / WCDMA auto 2 = WCDMA only 1 = GSM only
		 * 0 = GSM / WCDMA preferred
		 * 
		 */
		int state = -7;
		if (b) {
			state = MODE_GSM_ONLY;
		} else {
			state = MODE_GSM_WCDMA_PREFERRD;
		}
		if (state == getMobiledataStae(context)) {
			Logger.i("Not switching 2G/3G since it's already in correct state.");
			return;
		}
		Intent intent = new Intent(MODIFY_NETWORK_MODE);
		intent.putExtra(NETWORK_MODE, state);
		context.sendBroadcast(intent);
		Logger.i("Switched 2G/3G to " + b);
	}

	private static int getMobiledataStae(Context context) {
		int state = 99;
		try {
			state = android.provider.Settings.Secure.getInt(context
					.getContentResolver(), "preferred_network_mode");
		} catch (SettingNotFoundException e) {
		}
		return state;
	}

	public static boolean isBackgroundSyncEnabled(Context context) {
		return ContentResolver.getMasterSyncAutomatically();
	}

	public static void enableBackgroundSync(Context context, boolean b) {
		if (ContentResolver.getMasterSyncAutomatically() == b) {
			Logger.i("Not switched background syc state is correct");
			return;
		}
		if (b) {
			ContentResolver.setMasterSyncAutomatically(true);
		} else {
			ContentResolver.setMasterSyncAutomatically(false);
		}
		Logger.i("Switched background syc to " + b);
	}
}
