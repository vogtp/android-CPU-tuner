package ch.amana.android.cputuner.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.service.TunerService;

public class EventListenerReceiver extends BroadcastReceiver {

	private static Object lock = new Object();
	private static EventListenerReceiver receiver = null;

	public static void registerEventListenerReceiver(Context context) {
		synchronized (lock) {
			context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, EventListenerReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
			if (receiver == null) {
				receiver = new EventListenerReceiver();
				// context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
				context.registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
				// bt: ok wifi: ok airplaine: ok md: yes 3g: yes
				// snyc: ?  
				// gps: not supported
				context.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
				//				context.registerReceiver(receiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
				Logger.w("Registered EventListenerReceiver");

			} else {
				if (Logger.DEBUG) {
					Logger.i("EventListenerReceiver allready registered, not registering again");
				}
			}
		}
	}

	public static void unregisterEventListenerReceiver(Context context) {
		synchronized (lock) {
			Logger.w("Request to unegistered EventListenerReceiver");
			if (receiver != null) {
				try {
					context.unregisterReceiver(receiver);
					receiver = null;
					Logger.w("Unegistered BatteryReceiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister EventListenerReceiver", e);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (Logger.DEBUG) {
				String action = intent == null ? "null intent" : intent.getAction();
				Logger.i("EventListenerReceiver got intent with action " + action);
				//if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				//	Logger.logIntentExtras(intent);
				//}
			}
			if (SettingsStorage.getInstance().isRunSwitchInBackground()) {
				Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_BATTERY);
				i.putExtra(TunerService.EXTRA_ACTION, intent.getAction());
				i.putExtras(intent);
				context.startService(i);
			} else {
				TunerService.handleBattery(context, intent.getAction(), intent);
			}
		} catch (Exception e) {
			String action = intent == null ? "null intent" : intent.getAction();
			Logger.e("Error executing action " + action + " in background in EventListenerReceiver", e);
		}
	}

}
