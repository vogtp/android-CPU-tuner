package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.service.TunerService;

public class BatteryReceiver extends BroadcastReceiver {

	private static Object lock = new Object();
	private static BatteryReceiver receiver = null;

	public static void registerBatteryReceiver(Context context) {
		synchronized (lock) {
			if (receiver == null) {
				receiver = new BatteryReceiver();
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
				context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
				context.registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
				Logger.w("Registered BatteryReceiver");

			} else {
				if (Logger.DEBUG) {
					Logger.i("BatteryReceiver allready registered, not registering again");
				}
			}
		}
	}

	public static void unregisterBatteryReceiver(Context context) {
		synchronized (lock) {
			Logger.w("Request to unegistered BatteryReceiver");
			if (receiver != null) {
				try {
					context.unregisterReceiver(receiver);
					receiver = null;
					Logger.w("Unegistered BatteryReceiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister BatteryReceiver", e);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (Logger.DEBUG) {
				String action = intent == null ? "null intent" : intent.getAction();
				Logger.i("Battery receiver got intent with action " + action);
			}
			Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_BATTERY);
			i.putExtra(TunerService.EXTRA_ACTION, intent.getAction());
			i.putExtras(intent);
			context.startService(i);
		} catch (Exception e) {
			String action = intent == null ? "null intent" : intent.getAction();
			Logger.e("Error executing action " + action + " in background in battery receiver", e);
		}
	}

}
