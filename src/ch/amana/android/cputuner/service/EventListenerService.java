package ch.amana.android.cputuner.service;

import android.app.Notification;
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
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;
import ch.amana.android.cputuner.receiver.EventListenerReceiver;
import ch.amana.android.cputuner.receiver.StatisticsReceiver;

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
		return START_STICKY;// | START_CONTINUATION_MASK;
	}

	@Override
	public void onLowMemory() {
	}

	private void startCpuTuner() {
		Context ctx = getApplicationContext();
		Logger.w("Starting cpu tuner services (" + SettingsStorage.getInstance(ctx).getVersionName() + ")");
		//		ctx.getPackageManager().setComponentEnabledSetting(new ComponentName(ctx, getClass()), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		EventListenerReceiver.registerEventListenerReceiver(ctx);
		CallPhoneStateListener.register(ctx);
		PowerProfiles.getInstance(ctx).reapplyProfile(true);
		ConfigurationAutoloadService.scheduleNextEvent(ctx);
		SettingsStorage settingsStorage = SettingsStorage.getInstance();
		if (false) {
			// TODO remove run in foreground
			if (SettingsStorage.getInstance(ctx).isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER) {
				Notification n = Notifier.startStatusbarNotifications(ctx);
				if (n != null) {
					startForeground(Notifier.NOTIFICATION_ID, n);
				}
			}
		} else {
			if (SettingsStorage.getInstance(ctx).isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER) {
				Notifier.startStatusbarNotifications(ctx);
			}
		}
		if (settingsStorage.isEnableLogProfileSwitches()) {
			SwitchLog.start(ctx);
		}
		if (settingsStorage.isRunStatisticsService()) {
			StatisticsReceiver.register(ctx);
		}
		SwitchLog.addToLog(ctx, ctx.getString(R.string.log_msg_cpu_tuner_started));
	}

	private void stopCpuTuner() {
		Context ctx = getApplicationContext();
		SettingsStorage settings = SettingsStorage.getInstance(ctx);
		Logger.w("Stopping cpu tuner services (" + settings.getVersionName() + ")");
		Logger.logStacktrace("Stopping cputuner services");
		SwitchLog.addToLog(ctx, ctx.getString(R.string.log_msg_cpu_tuner_stoped));
		CallPhoneStateListener.unregister(ctx);
		EventListenerReceiver.unregisterEventListenerReceiver(ctx);
		ctx.stopService(new Intent(ctx, ConfigurationAutoloadService.class));
		StatisticsReceiver.unregister(ctx);
		switch (settings.isStatusbarAddto()) {
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
		// ctx.getPackageManager().setComponentEnabledSetting(new ComponentName(ctx, getClass()), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
