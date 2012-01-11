package ch.amana.android.cputuner.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.receiver.StatisticsReceiver;

public class StatisticsService extends IntentService {


	public StatisticsService() {
		super("statistics service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.d("StatisticsService got called");
		if (!SettingsStorage.getInstance(getApplicationContext()).isRunStatisticsService()) {
			return;
		}
		Logger.d("Adding timeinstate to input queue");
		String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
		Bundle extras = intent.getExtras();
		String triggerName = extras.getString(DB.SwitchLogDB.NAME_TRIGGER);
		String profileName = extras.getString(DB.SwitchLogDB.NAME_PROFILE);
		String virtGovName = extras.getString(DB.SwitchLogDB.NAME_VIRTGOV);
		ContentResolver contentResolver = getContentResolver();
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

	static public void start(Context ctx) {
		StatisticsReceiver.register(ctx.getApplicationContext());
	}

	static public void stop(Context ctx) {
		StatisticsReceiver.unregister(ctx.getApplicationContext());
	}

}
