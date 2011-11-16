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
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.receiver.BatteryReceiver;
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;

public class TunerService extends IntentService {

	public static final String ACTION_START_CPUTUNER = "ch.amana.android.cputuner.ACTION_START_CPUTUNER";
	public static final String ACTION_STOP_CPUTUNER = "ch.amana.android.cputuner.ACTION_STOP_CPUTUNER";
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

		long startTs = -1;
		String serviceAction = "noIntentFound";
		try {
			serviceAction = intent.getAction();
			if (Logger.DEBUG) {
				Logger.d("TunerService got action " + serviceAction);
				startTs = System.currentTimeMillis();
			}
			if (ACTION_START_CPUTUNER.equals(serviceAction)) {
				startCpuTuner();
			} else if (ACTION_STOP_CPUTUNER.equals(serviceAction)) {
				stopCpuTuner();
			} else if (ACTION_TUNERSERVICE_BATTERY.equals(serviceAction)) {
				handleBattery(intent);
			} else if (ACTION_TUNERSERVICE_PHONESTATE.equals(serviceAction)) {
				handlePhoneState(intent);
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

	private void startCpuTuner() {
		Context context = getApplicationContext();
		Logger.i("Starting cpu tuner services (" + context.getString(R.string.version) + ")");
		Context ctx = context.getApplicationContext();
		BatteryReceiver.registerBatteryReceiver(ctx);
		CallPhoneStateListener.register(ctx);
		PowerProfiles.getInstance(ctx).reapplyProfile(true);
		ConfigurationAutoloadService.scheduleNextEvent(ctx);
		if (SettingsStorage.getInstance(ctx).isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER) {
			Notifier.startStatusbarNotifications(ctx);
		}
	}

	private void stopCpuTuner() {
		Context context = getApplicationContext();
		Logger.i("Stopping cpu tuner services (" + context.getString(R.string.version) + ")");
		Logger.logStacktrace("Stopping cputuner services");
		Context ctx = context.getApplicationContext();
		CallPhoneStateListener.unregister(ctx);
		BatteryReceiver.unregisterBatteryReceiver(ctx);
		ctx.stopService(new Intent(ctx, ConfigurationAutoloadService.class));
		switch (SettingsStorage.getInstance(ctx).isStatusbarAddto()) {
		case SettingsStorage.STATUSBAR_RUNNING:
			Notifier.stopStatusbarNotifications(ctx);
			break;
		case SettingsStorage.STATUSBAR_ALWAYS:
			Notifier.startStatusbarNotifications(ctx);
			break;

		default:
			break;
		}
		//context.stopService(new Intent(ctx, TunerService.class));
		stopSelf();
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

	private void handlePhoneState(Intent intent) {
		int state = intent.getIntExtra(EXTRA_PHONE_STATE, -1);
		Logger.v("Got call state: " + state);
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE:
			// hangup
			PowerProfiles.getInstance(getApplicationContext()).setCallInProgress(false);
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			// incomming
			PowerProfiles.getInstance(getApplicationContext()).setCallInProgress(true);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			// outgoing
			PowerProfiles.getInstance(getApplicationContext()).setCallInProgress(true);
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
		try {
			acquireWakelock();
			PowerProfiles powerProfiles = PowerProfiles.getInstance(getApplicationContext());
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
