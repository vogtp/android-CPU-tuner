package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class BatteryReceiver extends BroadcastReceiver {

	private static Object lock = new Object();
	private static BatteryReceiver receiver = null;

	public static void registerBatteryReceiver(Context context) {
		synchronized (lock) {
			if (receiver == null) {
				IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				receiver = new BatteryReceiver();
				context.registerReceiver(receiver, batteryLevelFilter);
				Log.w(Logger.TAG, "Registered BatteryReceiver");
			} else {
				Log.i(Logger.TAG, "BatteryReceiver allready registered, not registering again");
			}
		}
	}

	public static void unregisterBatteryReceiver(Context context) {
		synchronized (lock) {
			if (receiver != null) {
				context.unregisterReceiver(receiver);
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Logger.TAG, "BatteryReceiver got intent: " + intent.getAction());
		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			Log.d(Logger.TAG, "Battery Level Remaining: " + level + "%");
			if (level > -1) {
				// handle battery event
			}
		}
	}
}
