package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.application.CpuTunerApplication;
import ch.amana.android.cputuner.background.BackgroundThread;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			handleBootCompleted(context);
		}
	}

	private void handleBootCompleted(final Context context) {
		SettingsStorage storage = SettingsStorage.getInstance();
		if (storage.isEnableProfiles()) {
			BackgroundThread.getInstance().queue(new Runnable() {
				@Override
				public void run() {
					Logger.w("Starting CPU tuner on boot");
					CpuTunerApplication.startCpuTuner(context);
				}
			});
		}
	}

}
