package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.service.BatteryService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			handleBootCompleted(context);
		}
	}

	private void handleBootCompleted(Context context) {
		SettingsStorage storage = SettingsStorage.getInstance();
		if (storage.isApplyOnBoot()) {
			Log.w(Logger.TAG, "Starting CPU tuner on boot");
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				context.startService(new Intent(context, BatteryService.class));
			}
			CpuHandler cpuHandler = new CpuHandler();
			try {
				cpuHandler.applyFromStorage();
			} catch (Throwable e) {
				Log.w(Logger.TAG, "Error appling on boot", e);
			}
		}
	}

}
