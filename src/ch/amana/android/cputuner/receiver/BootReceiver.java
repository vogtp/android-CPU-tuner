package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.application.CpuTunerApplication;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;

public class BootReceiver extends BroadcastReceiver {

	private static final long MIN_DELTA_BOOT = 1000 * 60 * 7;

	@Override
	public void onReceive(final Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SettingsStorage settings = SettingsStorage.getInstance();
			long lastBoot = settings.getLastBoot();
			long now = System.currentTimeMillis();
			settings.setLastBoot(now);
			long milliesSinceBoot = now - lastBoot;
			if (milliesSinceBoot > MIN_DELTA_BOOT) {
				Toast.makeText(context, R.string.msg_not_starting_after_reboot, Toast.LENGTH_LONG).show();
			}
			if (settings.isEnableProfiles()) {
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
