package ch.amana.android.cputuner.application;

import android.app.Application;
import android.content.Intent;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.service.BatteryService;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SettingsStorage.initInstance(this);
		// BatteryReceiver.registerBatteryReceiver(this);
		startService(new Intent(this, BatteryService.class));
	}
}
