package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;
import ch.amana.android.cputuner.service.ConfigurationAutoloadService;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage.initInstance(ctx);
		PowerProfiles.initInstance(ctx);

		// if (Logger.DEBUG) {
		// Builder threadPolicy = new StrictMode.ThreadPolicy.Builder();
		// // threadPolicy.detectDiskReads();
		// threadPolicy.detectDiskWrites();
		// threadPolicy.detectNetwork();
		// threadPolicy.penaltyLog();
		// // threadPolicy.penaltyDropBox();
		// StrictMode.setThreadPolicy(threadPolicy.build());
		// }

		try {
			InstallHelper.populateDb(ctx);
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				startService(new Intent(ctx, BatteryService.class));
				PowerProfiles.getInstance().reapplyProfile(true);
				ConfigurationAutoloadService.scheduleNextEvent(ctx);
			}
		} catch (Throwable e) {
			Logger.e("Cannot update DB", e);
			InstallHelper.magicallyHeal(ctx);
			throw new RuntimeException("Cannot start cpu tuner", e);
		}
	}
}
