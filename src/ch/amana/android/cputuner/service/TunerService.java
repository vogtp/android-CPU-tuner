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
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;

public class TunerService extends IntentService {

	public static final String ACTION_TUNERSERVICE_BATTERY = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_BATTERY";
	public static final String ACTION_TUNERSERVICE_PHONESTATE = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_PHONESTATE";
	public static final String ACTION_PULSE = "ch.amana.android.cputuner.ACTION_PULSE";
	public static final String ACTION_TUNERSERVICE_MANUAL_PROFILE = "ch.amana.android.cputuner.ACTION_TUNERSERVICE_MANUAL_PROFILE";

	public static final String EXTRA_ACTION = "EXTRA_ACTION";
	public static final String EXTRA_PHONE_STATE = "EXTRA_PHONE_STATE";
	public static final String EXTRA_PULSE_ON_OFF = "EXTRA_ON_OFF";
	public static final String EXTRA_IS_MANUAL_PROFILE = "EXTRA_IS_MANUAL_PROFILE";
	public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";
	public static final String EXTRA_PULSE_START = "EXTRA_PULSE_START";
	public static final String EXTRA_PULSE_STOP = "EXTRA_PULSE_STOP";

	private static PowerManager pm;
	private static WakeLock wakeLock = null;

	public TunerService() {
		super("Cpu tuner background worker");
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

		long startTs = -1;
		String serviceAction = "noIntentFound";
		try {
			serviceAction = intent.getAction();
			if (Logger.DEBUG) {
				Logger.d("TunerService got action " + serviceAction);
				startTs = System.currentTimeMillis();
			}
			if (ACTION_TUNERSERVICE_BATTERY.equals(serviceAction)) {
				handleBattery(getApplicationContext(), intent.getStringExtra(EXTRA_ACTION), intent);
			} else if (ACTION_TUNERSERVICE_PHONESTATE.equals(serviceAction)) {
				handlePhoneState(getApplicationContext(), intent.getIntExtra(EXTRA_PHONE_STATE, -1));
			} else if (ACTION_PULSE.equals(serviceAction)) {
				if (intent.getBooleanExtra(EXTRA_PULSE_START, false)) {
					startPulse();
				} else if (intent.getBooleanExtra(EXTRA_PULSE_STOP, false)) {
					stopPulse();
				} else {
					handlePulse(intent.getExtras().getBoolean(EXTRA_PULSE_ON_OFF));
				}
			} else if (ACTION_TUNERSERVICE_MANUAL_PROFILE.equals(serviceAction)) {
				handleManualProfile(intent);
			}
		} finally {
			if (Logger.DEBUG) {
				long delta = System.currentTimeMillis() - startTs;
				Logger.i("TunerService completed handling of action " + serviceAction + " in " + delta + " ms.");
			}
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

	private void startPulse() {
		long delay = SettingsStorage.getInstance().getPulseInitalDelay();
		if (delay < 1) {
			handlePulse(true);
		} else {
			Logger.i("Start pulse service in " + delay + " s");
			schedulePulse(delay, true);
		}
	}

	private void stopPulse() {
		Logger.i("Stopping pulse");
		Context ctx = getApplicationContext();
		Intent intent = new Intent(TunerService.ACTION_PULSE);
		PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.cancel(operation);
	}

	private void handlePulse(boolean on) {
		Logger.i("Do pulse (value: " + on + ")");
		PulseHelper.getInstance(getApplicationContext()).doPulse(on);
		long delay = on ? SettingsStorage.getInstance().getPulseDelayOn() : SettingsStorage.getInstance().getPulseDelayOff();
		delay = delay * 60;
		schedulePulse(delay, !on);
	}

	private void schedulePulse(long delay, boolean b) {
		Logger.i("Next pulse in " + delay + " sec (value: " + b + ")");
		long triggerAtTime = SystemClock.elapsedRealtime() + delay * 1000;
		Intent intent = new Intent(ACTION_PULSE);
		intent.putExtra(EXTRA_PULSE_ON_OFF, b);
		Context ctx = getApplicationContext();
		PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, -1, operation);
		//			am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, 0, operation);
	}

	public static void handlePhoneState(Context context, int state) {
		Logger.v("Got call state: " + state);
		startSpeedUpSwitch();
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// hangup
			PowerProfiles.getInstance(context).setCallInProgress(false);
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			// incomming
			PowerProfiles.getInstance(context).setCallInProgress(true);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// outgoing
			PowerProfiles.getInstance(context).setCallInProgress(true);
			break;

		default:
			break;
		}
		endSpeedUpSwitch();
	}

	public static void handleBattery(Context ctx, String action, Intent intent) {
		if (action == null) {
			Logger.w("BatteryJob got null intent returning");
			return;
		}
		try {
			acquireWakelock(ctx);
			PowerProfiles powerProfiles = PowerProfiles.getInstance(ctx);
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
				startSpeedUpSwitch();
				powerProfiles.setAcPower(true);
				endSpeedUpSwitch();
			} else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
				startSpeedUpSwitch();
				powerProfiles.setAcPower(false);
				endSpeedUpSwitch();
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				startSpeedUpSwitch();
				powerProfiles.setScreenOff(true);
				endSpeedUpSwitch();
			} else if (Intent.ACTION_SCREEN_ON.equals(action)) {
				startSpeedUpSwitch();
				powerProfiles.setScreenOff(false);
				endSpeedUpSwitch();
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				// manage network state on wifi
				int state = SettingsStorage.getInstance().getNetworkStateOnWifi();
				if (state != PowerProfiles.SERVICE_STATE_LEAVE) {
					NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					powerProfiles.setWifiConnected(networkInfo.isConnected());
				}
			}
		} finally {
			releaseWakelock();
		}
	}

	private static void acquireWakelock(Context ctx) {
		if (pm == null) {
			pm = (PowerManager) ctx.getApplicationContext().getSystemService(Context.POWER_SERVICE);
		}
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPU tuner");
		wakeLock.acquire();
	}

	private static void releaseWakelock() {
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	private static void startSpeedUpSwitch() {
		SettingsStorage settings = SettingsStorage.getInstance();
		if (settings.isEnableSwitchCpuSetting()) {
			CpuHandler.getInstance().applyCpuSettings(settings.getSwitchCpuSetting());
		}
	}

	private static void endSpeedUpSwitch() {
		if (SettingsStorage.getInstance().isEnableSwitchCpuSetting()) {
			CpuHandler.getInstance().applyCpuSettings(PowerProfiles.getInstance().getCurrentProfile());
		}
	}
}
