package ch.amana.android.cputuner.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.receiver.BatteryReceiver;
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;

public class EventListenerService extends Service {

	public static final String ACTION_START_CPUTUNER = "ch.amana.android.cputuner.ACTION_START_CPUTUNER";
	public static final String ACTION_STOP_CPUTUNER = "ch.amana.android.cputuner.ACTION_STOP_CPUTUNER";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			Logger.w("EventListenerService got null intent");
			return START_NOT_STICKY;
		}
		String serviceAction = intent.getAction();
		if (ACTION_START_CPUTUNER.equals(serviceAction)) {
			startCpuTuner();
		} else if (ACTION_STOP_CPUTUNER.equals(serviceAction)) {
			stopCpuTuner();
		}
		return START_STICKY;
	}

	@Override
	public void onLowMemory() {
	}

	private void startCpuTuner() {
		Context ctx = getApplicationContext();
		Logger.w("Starting cpu tuner services (" + ctx.getString(R.string.version) + ")");
		BatteryReceiver.registerBatteryReceiver(ctx);
		CallPhoneStateListener.register(ctx);
		PowerProfiles.getInstance(ctx).reapplyProfile(true);
		ConfigurationAutoloadService.scheduleNextEvent(ctx);
		if (SettingsStorage.getInstance(ctx).isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER) {
			Notifier.startStatusbarNotifications(ctx);
		}
		SettingsStorage settingsStorage = SettingsStorage.getInstance();
		if (settingsStorage.isEnableLogProfileSwitches()) {
			SwitchLog.start(ctx);
		}
		if (settingsStorage.isRunStatisticsService()) {
			StatisticsService.start(ctx);
		}
	}

	private void stopCpuTuner() {
		Context context = getApplicationContext();
		Logger.w("Stopping cpu tuner services (" + context.getString(R.string.version) + ")");
		Logger.logStacktrace("Stopping cputuner services");
		Context ctx = context.getApplicationContext();
		CallPhoneStateListener.unregister(ctx);
		BatteryReceiver.unregisterBatteryReceiver(ctx);
		ctx.stopService(new Intent(ctx, ConfigurationAutoloadService.class));
		StatisticsService.stop(context);
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
		SwitchLog.stop(ctx);
		stopSelf();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
