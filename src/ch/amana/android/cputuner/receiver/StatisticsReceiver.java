package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.provider.db.DB;

public class StatisticsReceiver extends BroadcastReceiver {

	public static String BROADCAST_UPDATE_TIMEINSTATE = "ch.amana.android.cputuner.BROADCAST_UPDATE_TIMEINSTATE";
	private static Object lock = new Object();
	private static StatisticsReceiver receiver = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!SettingsStorage.getInstance(context).isRunStatisticsService()) {
			return;
		}
		final Context ctx = context;
		final Intent i = intent;

		new Thread(new Runnable() {
			@Override
			public void run() {
				final Bundle extras = i.getExtras();
				updateStatisticsInputQueue(ctx, extras);
			}

		}).start();


	}

	public static void updateStatisticsInputQueue(Context context, Bundle extras) {
		Logger.d("Adding timeinstate to input queue");
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		if (extras == null) {
			extras = new Bundle();
		}
		String triggerName = extras.getString(DB.SwitchLogDB.NAME_TRIGGER);
		String profileName = extras.getString(DB.SwitchLogDB.NAME_PROFILE);
		String virtGovName = extras.getString(DB.SwitchLogDB.NAME_VIRTGOV);
		if (TextUtils.isEmpty(triggerName)) {
			triggerName = PowerProfiles.getInstance(context).getCurrentTriggerName();
		}
		if (TextUtils.isEmpty(profileName)) {
			profileName = PowerProfiles.getInstance(context).getCurrentProfileName();
		}
		if (TextUtils.isEmpty(virtGovName)) {
			virtGovName = PowerProfiles.getInstance(context).getCurrentVirtGovName();
		}
		ContentResolver contentResolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(DB.TimeInStateInput.NAME_TIS_END, timeinstate);
		int count = contentResolver.update(DB.TimeInStateInput.CONTENT_URI, values, DB.TimeInStateInput.SELECTION_NOT_FINISHED, null);
		if (count != 1) {
			Logger.w("Should only update 1 statistics record! Updated: " + count);
		}
		values.put(DB.TimeInStateInput.NAME_TIME, System.currentTimeMillis());
		values.put(DB.TimeInStateInput.NAME_TRIGGER, triggerName);
		values.put(DB.TimeInStateInput.NAME_PROFILE, profileName);
		values.put(DB.TimeInStateInput.NAME_VIRTGOV, virtGovName);
		values.put(DB.TimeInStateInput.NAME_TIS_START, timeinstate);
		values.remove(DB.TimeInStateInput.NAME_TIS_END);
		contentResolver.insert(DB.TimeInStateInput.CONTENT_URI, values);
	}

	public static void register(Context context) {
		synchronized (lock) {
			if (receiver == null) {
				receiver = new StatisticsReceiver();
				context.registerReceiver(receiver, new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(BROADCAST_UPDATE_TIMEINSTATE));
				Logger.w("Registered StatisticsReceiver");

			} else {
				if (Logger.DEBUG) {
					Logger.i("StatisticsReceiver allready registered, not registering again");
				}
			}
		}
	}

	public static void unregister(Context context) {
		synchronized (lock) {
			Logger.w("Request to unegistered StatisticsReceiver");
			if (receiver != null) {
				try {
					context.unregisterReceiver(receiver);
					receiver = null;
					Logger.w("Unegistered StatisticsReceiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister StatisticsReceiver", e);
				}
			}
		}
	}
}