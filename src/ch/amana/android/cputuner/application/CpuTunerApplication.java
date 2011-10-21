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

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage.initInstance(ctx);
		PowerProfiles.initInstance(ctx);

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
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				startCpuTuner(ctx);
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
		PowerProfiles.getInstance().reapplyProfile(true);
		ConfigurationAutoloadService.scheduleNextEvent(ctx);
		if (SettingsStorage.getInstance().isStatusbarAddto()) {
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
		Notifier.stopStatusbarNotifications(ctx);
		context.stopService(new Intent(ctx, TunerService.class));
	}
}
