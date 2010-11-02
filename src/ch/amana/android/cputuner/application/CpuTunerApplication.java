package ch.amana.android.cputuner.application;

import android.app.Application;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.receiver.BatteryReceiver;

public class CpuTunerApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SettingsStorage.initInstance(this);
		BatteryReceiver.registerBatteryReceiver(this);
	}
}
