package ch.amana.android.cputuner.background;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class BatteryJob implements Runnable {

	private WakeLock wakeLock;
	private long startTs;

	Context context;
	//	Intent intent;

	private static BatteryJob job = new BatteryJob();
	private String action;
	private int rawlevel;
	private int scale;
	private int plugged;
	private int health;
	private int temperature;
	private NetworkInfo networkInfo;
	private PowerManager pm;


	public static BatteryJob getJob(Context ctx, Intent intent) {
		if (job == null) {
			job = new BatteryJob();
		}
		job.context = ctx.getApplicationContext();
		if (job.pm == null) {
			job.pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		}
		if (intent != null) {
			job.action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(job.action)) {
				job.rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				job.scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				job.plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				job.health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
				job.temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MAX_VALUE);
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(job.action)) {
				job.networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			}
		} else {
			job.action = null;
		}
		return job;
	}

	@Override
	public void run() {
		if (Logger.DEBUG) {
			startTs = System.currentTimeMillis();
		}
		try {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU tuner");
			wakeLock.acquire();
			handleIntent();
		} catch (Throwable e) {
			Logger.e("BatteryJob got exception", e);
		} finally {
			if (Logger.DEBUG) {
				long delta = System.currentTimeMillis() - startTs;
				Logger.i("Millies to switch profile: " + delta);
			}
			if (wakeLock != null) {
				wakeLock.release();
			}
			wakeLock = null;
			context = null;
		}
	}

	private void handleIntent() {
		if (action == null) {
			Logger.w("BatteryJob got null intent returning");
			return;
		}
		if (Logger.DEBUG) {
			Logger.d("BatteryJob got intent: " + action);
		}

		PowerProfiles powerProfiles = PowerProfiles.getInstance();
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
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
				powerProfiles.setWifiConnected(networkInfo.isConnected());
			}
		}
	}
}