package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class BatteryReceiver extends BroadcastReceiver {

	private static Object lock = new Object();
	private static BatteryReceiver receiver = null;

	private class SetProfileTask extends AsyncTask<Intent, Void, Void> {

		private final Context ctx;

		public SetProfileTask(Context ctx) {
			super();
			this.ctx = ctx;
		}

		@Override
		protected Void doInBackground(Intent... params) {
			if (params == null || params.length < 1) {
				return null;
			}
			// TODO remove logging after switching
			Logger.v("Using new interface to switch profile");
			BatteryReceiver.handleIntent(ctx, params[0]);
			return null;
		}
	}

	public static void registerBatteryReceiver(Context context) {
		synchronized (lock) {
			if (receiver == null) {
				IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				IntentFilter screenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
				IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
				receiver = new BatteryReceiver();
				context.registerReceiver(receiver, batteryLevelFilter);
				context.registerReceiver(receiver, screenOnFilter);
				context.registerReceiver(receiver, screenOffFilter);
				Notifier.notifyProfile("Initalising");
				Logger.w("Registered BatteryReceiver");
			} else {
				Logger.i("BatteryReceiver allready registered, not registering again");
			}
		}
	}

	public static void unregisterBatteryReceiver(Context context) {
		synchronized (lock) {
			if (receiver != null) {
				try {
					context.unregisterReceiver(receiver);
					receiver = null;
				} catch (Throwable e) {
					Logger.w("Could not unregister BatteryReceiver", e);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (SettingsStorage.getInstance().isNewProfileSwitchTask()) {
			SetProfileTask spt = new SetProfileTask(context.getApplicationContext());
			spt.execute(intent);
		} else {
			handleIntent(context, intent);
		}
	}

	private static void handleIntent(Context context, Intent intent) {
		String action = intent.getAction();
		Logger.d("BatteryReceiver got intent: " + action);
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			if (plugged > -1) {
				PowerProfiles.setAcPower(plugged > 0);
			}
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			Logger.d("Battery Level Remaining: " + level + "%");
			if (level > -1) {
				// handle battery event
				PowerProfiles.setBatteryLevel(level);
			}
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				context.startService(new Intent(context, BatteryService.class));
			}
		} else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
			Notifier.notify(context, "CPU tuner: Power connected", 2);
			PowerProfiles.setAcPower(true);
		} else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
			Notifier.notify(context, "CPU tuner: Power disconnected", 2);
			PowerProfiles.setAcPower(false);
		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			Notifier.notify(context, "Screen turned off", 2);
			PowerProfiles.setScreenOff(true);
		} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
			Notifier.notify(context, "Screen turned on", 2);
			PowerProfiles.setScreenOff(false);
		}
	}
}
