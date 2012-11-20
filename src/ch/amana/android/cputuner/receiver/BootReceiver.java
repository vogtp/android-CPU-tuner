package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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

			java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
			java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
			StringBuilder lastBootTime = new StringBuilder();
			StringBuilder currentTime = new StringBuilder();
			lastBootTime.append(dateFormat.format(lastBoot)).append(" ").append(timeFormat.format(lastBoot));
			currentTime.append(dateFormat.format(now)).append(" ").append(timeFormat.format(now));
			Logger.i("CPU tuner bootstart: Last boot time " + lastBootTime.toString());
			Logger.i("CPU tuner bootstart: Current time   " + currentTime.toString());
			if (Logger.DEBUG) {
				long deltaMin = milliesSinceBoot / 1000l / 60l;
				Logger.i("CPU tuner bootstart: Min since last boot " + milliesSinceBoot);

			}
			if (milliesSinceBoot < MIN_DELTA_BOOT) {
				Logger.w("CPU tuner bootstart: NOT starting since device is booting to frequent");
				Toast.makeText(context, R.string.msg_not_starting_after_reboot, Toast.LENGTH_LONG).show();
				settings.setEnableProfiles(false);
				return;
			}
			if (settings.isEnableProfiles()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Logger.w("CPU tuner bootstart: starting CPU tuner");
						CpuTunerApplication.startCpuTuner(context);
					}
				}).start();
			}
		}
	}

}
