package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.application.CpuTunerApplication;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SettingsStorage storage = SettingsStorage.getInstance();
			if (storage.isEnableProfiles()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Logger.w("Starting CPU tuner on boot");
						CpuTunerApplication.startCpuTuner(context);
					}
				}).start();
			}
		}
	}

}
