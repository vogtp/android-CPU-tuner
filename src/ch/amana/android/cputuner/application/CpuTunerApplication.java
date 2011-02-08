package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.service.BatteryService;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Context ctx = getApplicationContext();
		SettingsStorage.initInstance(ctx);
		PowerProfiles.initInstance(ctx);
		InstallHelper.populateDb(ctx);
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			startService(new Intent(ctx, BatteryService.class));
			try {
				PowerProfiles.getInstance().reapplyProfile(true);
			}catch (Exception e) {
				DB.OpenHelper oh = new DB.OpenHelper(this);
				oh.getWritableDatabase().getVersion();
				oh.close();
				PowerProfiles.getInstance().reapplyProfile(true);
			}
		}
	}
}
