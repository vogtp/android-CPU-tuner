package ch.amana.android.cputuner.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;

public class TunerService extends IntentService {

	public static final String ACTION_TUNERSERVICE_BATTERY = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_BATTERY";
	public static final String ACTION_TUNERSERVICE_PHONESTATE = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_PHONESTATE";
	public static final String ACTION_PULSE = "ch.amana.android.cputuner.ACTION_PULSE";
	public static final String ACTION_TUNERSERVICE_MANUAL_PROFILE = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_MANUAL_PROFILE";

	public static final String EXTRA_ACTION = "EXTRA_ACTION";
	public static final String EXTRA_PHONE_STATE = "EXTRA_PHONE_STATE";
	public static final String EXTRA_ON_OFF = "EXTRA_ON_OFF";
	public static final String EXTRA_IS_MANUAL_PROFILE = "EXTRA_IS_MANUAL_PROFILE";
	public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";

	private static final long MIN_TO_MILLIES = 1000 * 60;

	private PowerManager pm;
	private WakeLock wakeLock = null;

	public TunerService() {
		super("Tuner Background Service");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			Logger.w("TunerService got null intent");
			return;
		}
		try {
			String serviceAction = intent.getAction();
			if (ACTION_TUNERSERVICE_BATTERY.equals(serviceAction)) {
				handleBattery(intent);
			} else if (ACTION_TUNERSERVICE_PHONESTATE.equals(serviceAction)) {
				handlePhoneState(intent);
			} else if (ACTION_PULSE.equals(serviceAction)) {
				handlePulse(intent);
			} else if (ACTION_TUNERSERVICE_MANUAL_PROFILE.equals(serviceAction)) {
				handleManualProfile(intent);
			}
		} finally {
			releaseWakelock();
		}
	}

	private void handleManualProfile(Intent intent) {
		PowerProfiles powerProfiles = PowerProfiles.getInstance();
		if (intent.getBooleanExtra(EXTRA_IS_MANUAL_PROFILE, false)) {
			long id = intent.getLongExtra(EXTRA_PROFILE_ID, powerProfiles.getCurrentAutoProfileId());
			powerProfiles.setManualProfile(id);
			powerProfiles.applyProfile(id);
		} else {
			if (powerProfiles.isManualProfile()) {
				powerProfiles.setManualProfile(PowerProfiles.AUTOMATIC_PROFILE);
				powerProfiles.reapplyProfile(true);
			}
		}
	}

	private void handlePulse(Intent intent) {
		boolean on = intent.getExtras().getBoolean(EXTRA_ON_OFF);
		Logger.i("Do pulse (value: " + on + ")");
		PulseHelper.getInstance(getApplicationContext()).doPulse(on);
		reschedulePulse(!on);
	}

	private void reschedulePulse(boolean b) {
		long delay = b ? SettingsStorage.getInstance().getPulseDelayOff() : SettingsStorage.getInstance().getPulseDelayOn();
		Logger.i("Next pulse in " + delay + " min (value: " + b + ")");
		long triggerAtTime = SystemClock.elapsedRealtime() + delay * MIN_TO_MILLIES;
		Intent intent = new Intent(ACTION_PULSE);
		intent.putExtra(EXTRA_ON_OFF, b);
		Context ctx = getApplicationContext();
		PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, 0, operation);
	}

	private void handlePhoneState(Intent intent) {
		int state = intent.getIntExtra(EXTRA_PHONE_STATE, -1);
		Logger.v("Got call state: " + state);
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// hangup
			PowerProfiles.getInstance().setCallInProgress(false);
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			// incomming
			PowerProfiles.getInstance().setCallInProgress(true);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// outgoing
			PowerProfiles.getInstance().setCallInProgress(true);
			break;

		default:
			break;
		}
	}

	private void handleBattery(Intent intent) {
		String action = intent.getStringExtra(EXTRA_ACTION);
		if (action == null) {
			Logger.w("BatteryJob got null intent returning");
			return;
		}
		long startTs = -1;
		if (Logger.DEBUG) {
			Logger.d("BatteryJob got intent: " + action);
			startTs = System.currentTimeMillis();
		}
		try {
			acquireWakelock();
			PowerProfiles powerProfiles = PowerProfiles.getInstance();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				int level = -1;
				int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
				int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
				int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MAX_VALUE);
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
					NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					powerProfiles.setWifiConnected(networkInfo.isConnected());
				}
			}
		} finally {
			if (Logger.DEBUG) {
				long delta = System.currentTimeMillis() - startTs;
				Logger.i("Millies to switch profile: " + delta);
			}
			releaseWakelock();
		}
	}

	private void acquireWakelock() {
		if (pm == null) {
			pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		}
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU tuner");
		wakeLock.acquire();
	}

	private void releaseWakelock() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}
