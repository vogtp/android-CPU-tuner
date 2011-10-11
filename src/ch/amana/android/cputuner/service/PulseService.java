package ch.amana.android.cputuner.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class PulseService extends Service {

	private static boolean isPulsing = false;

	private static final long MIN_TO_MILLIES = 1000 * 60;

	public static final String ACTION_PULSE = "ch.amana.android.cputuner.ACTION_PULSE";
	public static final String EXTRA_ON_OFF = "EXTRA_ON_OFF";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (ACTION_PULSE.equals(intent.getAction())) {
			boolean on = intent.getExtras().getBoolean(EXTRA_ON_OFF);
			Logger.i("Do pulse (value: " + on + ")");
			PulseHelper.getInstance(getApplicationContext()).doPulse(on);
			reschedule(!on);
		}
		return START_NOT_STICKY;
	}
	
	private void reschedule(boolean b) {
		long delay = b ? SettingsStorage.getInstance().getPulseDelayOff() : SettingsStorage.getInstance().getPulseDelayOn();
		Logger.i("Next pulse in " + delay + " min (value: " + b + ")");
		long triggerAtTime = SystemClock.elapsedRealtime() + delay * MIN_TO_MILLIES;
		Intent intent = new Intent(ACTION_PULSE);
		intent.putExtra(EXTRA_ON_OFF, b);
		PendingIntent operation = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, 0, operation);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void startService(Context ctx) {
		synchronized (ACTION_PULSE) {
			if (isPulsing) {
				return;
			}
			isPulsing = true;
		}
		Logger.i("Start pulsing");
		Intent i = new Intent(ACTION_PULSE);
		i.putExtra(EXTRA_ON_OFF, true);
		ctx.startService(i);
	}

	public static void stopService(Context ctx) {
		synchronized (ACTION_PULSE) {
			isPulsing = false;
		}
		Logger.i("Stop pulsing");
		Intent intent = new Intent(ACTION_PULSE);
		PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		am.cancel(operation);
		ctx.stopService(new Intent(ACTION_PULSE));
	}

}
