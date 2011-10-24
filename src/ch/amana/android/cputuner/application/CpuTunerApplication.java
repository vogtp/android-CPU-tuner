package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.receiver.BatteryReceiver;
import ch.amana.android.cputuner.receiver.CallPhoneStateListener;
import ch.amana.android.cputuner.service.ConfigurationAutoloadService;
import ch.amana.android.cputuner.service.TunerService;
import ch.amana.android.cputuner.view.activity.FirstRunActivity;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage settings = SettingsStorage.getInstance(ctx);

		//		if (Logger.DEBUG) {
		//			Builder threadPolicy = new StrictMode.ThreadPolicy.Builder();
		//			// threadPolicy.detectDiskReads();
		//			threadPolicy.detectDiskWrites();
		//			threadPolicy.detectNetwork();
		//			threadPolicy.penaltyLog();
		//			// threadPolicy.penaltyDropBox();
		//			StrictMode.setThreadPolicy(threadPolicy.build());
		//		}

		try {
			InstallHelper.initialise(ctx);

			if (settings.isFirstRun()) {
				startActivity(new Intent(getApplicationContext(), FirstRunActivity.class));
			}
			if (settings.isEnableProfiles()) {
				startCpuTuner(ctx);
			} else {
				if (SettingsStorage.getInstance(ctx).isStatusbarAddto() == SettingsStorage.STATUSBAR_ALWAYS) {
					Notifier.startStatusbarNotifications(ctx);
				}
			}
		} catch (Throwable e) {
			Logger.e("Cannot update DB", e);
		}
	}

	public static void startCpuTuner(Context context) {
		Logger.i("Starting cpu tuner services");
		Context ctx = context.getApplicationContext();
		BatteryReceiver.registerBatteryReceiver(ctx);
		CallPhoneStateListener.register(ctx);
		PowerProfiles.getInstance(ctx).reapplyProfile(true);
		ConfigurationAutoloadService.scheduleNextEvent(ctx);
		if (SettingsStorage.getInstance(ctx).isStatusbarAddto() != SettingsStorage.STATUSBAR_NEVER) {
			Notifier.startStatusbarNotifications(ctx);
		}
	}

	public static void stopCpuTuner(Context context) {
		Logger.i("Stopping cpu tuner services");
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
		context.stopService(new Intent(ctx, TunerService.class));
	}
}
