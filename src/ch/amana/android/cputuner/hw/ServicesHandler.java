package ch.amana.android.cputuner.hw;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;

public class ServicesHandler {

	/**
	 * The preferred network mode 7 = Global 6 = EvDo only 5 = CDMA w/o EvDo 4 =
	 * CDMA / EvDo auto 3 = GSM / WCDMA auto 2 = WCDMA only 1 = GSM only 0 = GSM
	 * / WCDMA preferred
	 * 
	 */
	public static final int MODE_2G_3G_PREFERRD = 0;// GSM / WCDMA preferred
	public static final int MODE_2G_ONLY = 1;// GSM only
	public static final int MODE_3G_ONLY = 2;// WCDMA only

	private static final String MODIFY_NETWORK_MODE = "com.android.internal.telephony.MODIFY_NETWORK_MODE";
	private static final String NETWORK_MODE = "networkMode";
	private static WifiManager wifi;

	private static WifiManager getWifiManager(Context ctx) {
		if (wifi == null) {
			wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		}
		return wifi;
	}

	public static void enableWifi(Context ctx, boolean enable) {
		if (!enable && !SettingsStorage.getInstance().isSwitchWifiOnConnectedNetwork() && isWifiConnected(ctx)) {
			Logger.i("Not switching wifi since we are connected!");
			return;
		}
		if (getWifiManager(ctx).setWifiEnabled(enable)) {
			Logger.i("Switched Wifi to " + enable);
		}
	}

	public static boolean isWifiConnected(Context ctx) {
		return getWifiManager(ctx).getConnectionInfo().getNetworkId() > -1;
	}


	public static boolean isWifiEnabaled(Context ctx) {
		return getWifiManager(ctx).isWifiEnabled();
	}

	public static boolean isGpsEnabled(Context ctx) {
		return GpsHandler.isGpxEnabled(ctx);
	}

	public static void enableGps(Context ctx, boolean enable) {
		GpsHandler.enableGps(ctx, enable);
	}

	public static boolean isBlutoothEnabled() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			return false;
		}
		return bluetoothAdapter.isEnabled();
	}

	public static void enableBluetooth(boolean enable) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Logger.i("Not switching bluetooth since its not present");
			return;
		}
		if (bluetoothAdapter.isEnabled() == enable) {
			Logger.i("Allready correct bluethooth state ");
			return;
		}
		if (enable) {
			bluetoothAdapter.enable();
		} else {
			bluetoothAdapter.disable();
		}
		Logger.i("Switched bluethooth to " + enable);
	}

	public static int whichMobiledata3G(Context context) {
		switch (getMobiledataState(context)) {
		case MODE_2G_ONLY:
			return PowerProfiles.SERVICE_STATE_2G;
		case MODE_2G_3G_PREFERRD:
			return PowerProfiles.SERVICE_STATE_2G_3G;
		case MODE_3G_ONLY:
			return PowerProfiles.SERVICE_STATE_3G;
		default:
			return Integer.MAX_VALUE;
		}
	
	}

	public static boolean isPhoneIdle(Context context) {
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getCallState() == TelephonyManager.CALL_STATE_IDLE;
	}

	// From:
	// /cyanogen/frameworks/base/services/java/com/android/server/status/widget/NetworkModeButton.java
	// line 97
	public static void enable2gOnly(Context context, int profileState) {
		/**
		 * The preferred network mode 7 = Global 6 = EvDo only 5 = CDMA w/o EvDo
		 * 4 = CDMA / EvDo auto 3 = GSM / WCDMA auto 2 = WCDMA only 1 = GSM only
		 * 0 = GSM / WCDMA preferred
		 * 
		 */

		if (!isPhoneIdle(context)) {
			Logger.w("Phone not idle, not switching moble data");
			return;
		}

		int state = -7;
		switch (profileState) {
		case PowerProfiles.SERVICE_STATE_2G:
			state = MODE_2G_ONLY;
			break;
		case PowerProfiles.SERVICE_STATE_2G_3G:
			state = MODE_2G_3G_PREFERRD;
			break;
		case PowerProfiles.SERVICE_STATE_3G:
			state = MODE_3G_ONLY;
			break;
		
		}
		
		if (state == getMobiledataState(context)) {
			Logger.i("Not switching 2G/3G since it's already in correct state.");
			return;
		}
		Intent intent = new Intent(MODIFY_NETWORK_MODE);
		intent.putExtra(NETWORK_MODE, state);
		context.sendBroadcast(intent);
		Logger.i("Switched 2G/3G to " + state);
	}

	private static int getMobiledataState(Context context) {
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
		try {
			if (ContentResolver.getMasterSyncAutomatically() == b) {
				Logger.i("Not switched background syc state is correct");
				return;
			}
			ContentResolver.setMasterSyncAutomatically(b);
		} catch (Throwable e) {
			Logger.e("Cannot switch background sync", e);
		}
		Logger.i("Switched background syc to " + b);
	}

	public static boolean isMobiledataConnectionEnabled(Context context) {
		return MobiledataWrapper.getInstance(context).getMobileDataEnabled();
	}

	 public static void enableMobileData(Context context, boolean enable) {
		try {
			MobiledataWrapper mdw = MobiledataWrapper.getInstance(context);
			if (!mdw.canUse()) {
				return;
			}
			if (mdw.getMobileDataEnabled() == enable) {
				Logger.i("Not switched mobiledata state is correct");
				return;
			}
			mdw.setMobileDataEnabled(enable);
		} catch (Throwable e) {
			Logger.e("Cannot switch mobiledata ", e);
		}
		Logger.i("Switched mobiledata to " + enable);
	 }

	// private void setBrightness(Activity context) {
	// // TODO Auto-generated method stub
	// WindowManager.LayoutParams lp = context.getWindow().getAttributes();
	// lp.screenBrightness = 100 / 100.0f;
	// context.getWindow().setAttributes(lp);
	// }
}
