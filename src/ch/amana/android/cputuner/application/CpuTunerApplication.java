package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.service.BatteryService;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage.initInstance(ctx);
		PowerProfiles.initInstance(ctx);
		String lang = SettingsStorage.getInstance().getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(ctx, lang);
		}
		if (Logger.DEBUG) {
			Builder threadPolicy = new StrictMode.ThreadPolicy.Builder();
			// threadPolicy.detectDiskReads();
			threadPolicy.detectDiskWrites();
			threadPolicy.detectNetwork();
			// threadPolicy.penaltyLog();
			threadPolicy.penaltyDropBox();
			StrictMode.setThreadPolicy(threadPolicy.build());
		}

		InstallHelper.populateDb(ctx);
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			startService(new Intent(ctx, BatteryService.class));
			PowerProfiles.getInstance().reapplyProfile(true);
		}
	}
}
