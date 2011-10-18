package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class BatteryReceiver extends BroadcastReceiver {

	private static Object lock = new Object();
	private static BatteryReceiver receiver = null;
	private static PhoneStateListener phoneStateListener;

	private class SetProfileTask extends AsyncTask<Intent, Void, Void> {

		private final Context ctx;
		private final WakeLock wakeLock;

		private final long startTs;

		public SetProfileTask(Context ctx) {
			super();
			startTs = System.currentTimeMillis();
			this.ctx = ctx;
			PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU tuner");
			wakeLock.acquire();
		}

		@Override
		protected Void doInBackground(Intent... params) {
			try {
				if (params == null || params.length < 1) {
					return null;
				}
				BatteryReceiver.handleIntent(ctx, params[0]);
			} finally {
				long delta = System.currentTimeMillis() - startTs;
				Logger.i("Millies to switch profile: " + delta);
				wakeLock.release();
			}
			return null;
		}
	}


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
				Notifier.notifyProfile("Initalising");
				Logger.w("Registered BatteryReceiver");
				if (SettingsStorage.getInstance().isEnableCallInProgressProfile()) {
					TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					phoneStateListener = new CallPhoneStateListener();
					tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
				}
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
					if (SettingsStorage.getInstance().isEnableCallInProgressProfile()) {
						TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
						tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
						phoneStateListener = null;
						PowerProfiles.getInstance().setCallInProgress(false);
					}
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
			SetProfileTask spt = new SetProfileTask(context.getApplicationContext());
			spt.execute(intent);
		} catch (Exception e) {
			String action = intent == null ? "null intent" : intent.getAction();
			Logger.e("Error executing action " + action + " in background in battery receiver", e);
		}
	}

	private static void handleIntent(Context context, Intent intent) {
		String action = intent.getAction();
		Logger.d("BatteryReceiver got intent: " + action);

		PowerProfiles powerProfiles = PowerProfiles.getInstance();
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
			int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MAX_VALUE);
			int level = -1;
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}
			Logger.d("Battery Level Remaining: " + level + "%");
			if (level > -1) {
				// handle battery event
				powerProfiles.setBatteryLevel(level);
			}
			powerProfiles.setBatteryTemperature(temperature / 10);
			powerProfiles.setBatteryHot(health == BatteryManager.BATTERY_HEALTH_OVERHEAT);

			if (plugged > -1) {
				powerProfiles.setAcPower(plugged > 0);
			}
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				context.startService(new Intent(context, BatteryService.class));
			}
		} else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
			powerProfiles.setAcPower(true);
		} else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
			powerProfiles.setAcPower(false);
		} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
			powerProfiles.setScreenOff(true);
		} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
			powerProfiles.setScreenOff(false);
		} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
			// manage network state on wifi
			int state = SettingsStorage.getInstance().getNetworkStateOnWifi();
			if (state != PowerProfiles.SERVICE_STATE_LEAVE) {
				NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				powerProfiles.setWifiConnected(ni.isConnected());
			}
		}
	}
}
