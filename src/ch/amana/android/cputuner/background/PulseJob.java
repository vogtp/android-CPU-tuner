package ch.amana.android.cputuner.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.service.PulseService;

public class PulseJob implements Runnable {

	private static final long MIN_TO_MILLIES = 1000 * 60;
	private static int[] lock = new int[0];

	private static PulseJob job = null;

	public static Runnable getJob(Context ctx, Intent intent) {
		if (job == null) {
			job = new PulseJob();
		}
		job.ctx = ctx.getApplicationContext();
		job.on = intent.getExtras().getBoolean(PulseService.EXTRA_ON_OFF);
		return job;
	}

	//	Intent intent;
	Context ctx;
	private boolean on;

	@Override
	public void run() {
		try {
			Logger.i("Do pulse (value: " + on + ")");
			synchronized (lock) {
				PulseHelper.getInstance(ctx).doPulse(on);
				reschedule(!on);
			}
		} catch (Throwable e) {
			Logger.e("PulseJob got exception", e);
		}

	}

	private void reschedule(boolean b) {
		long delay = b ? SettingsStorage.getInstance().getPulseDelayOff() : SettingsStorage.getInstance().getPulseDelayOn();
		Logger.i("Next pulse in " + delay + " min (value: " + b + ")");
		long triggerAtTime = SystemClock.elapsedRealtime() + delay * MIN_TO_MILLIES;
		Intent intent = new Intent(PulseService.ACTION_PULSE);
		intent.putExtra(PulseService.EXTRA_ON_OFF, b);
		PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(PulseService.ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, 0, operation);
	}

};