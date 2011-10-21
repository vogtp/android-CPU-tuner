package ch.amana.android.cputuner.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.provider.db.DB;

public class ConfigurationAutoloadService extends IntentService implements BackupRestoreCallback {

	public static final String ACTION_SCEDULE_AUTOLOAD = "ch.amana.android.cputuner.ACTION_SCEDULE_AUTOLOAD";

	private static final String EXTRA_VALUES = "camValuesBundle";

	public ConfigurationAutoloadService() {
		super(ACTION_SCEDULE_AUTOLOAD);
	}

	public static void updateNextExecution(Context ctx) {
		ConfigurationAutoloadService.getModelForNextExecution(ctx);
	}

	private static ConfigurationAutoloadModel getModelForNextExecution(Context ctx) {
		long now = System.currentTimeMillis();
		String selection = null;
		String[] selectionArgs = null;

		Cursor cursor = null;
		long nextExec = Long.MAX_VALUE;
		ConfigurationAutoloadModel nextCam = null;
		try {
			ContentResolver contentResolver = ctx.getContentResolver();
			cursor = contentResolver.query(DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, selection, selectionArgs,
					DB.ConfigurationAutoload.SORTORDER_DEFAULT);
			if (cursor == null) {
				return null;
			}
			while (cursor.moveToNext()) {
				ConfigurationAutoloadModel cam = new ConfigurationAutoloadModel(cursor);
				contentResolver.update(DB.ConfigurationAutoload.CONTENT_URI, cam.getValues(), DB.NAME_ID + "=?", new String[] { Long.toString(cam.getDbId()) });
				long thisExec = cam.getNextExecution();
				if (thisExec < nextExec && thisExec > now) {
					nextCam = cam;
					nextExec = thisExec;
				}
			}
			return nextCam;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				cursor = null;
			}
		}
	}

	public static void scheduleNextEvent(Context context) {
		Context ctx = context.getApplicationContext();
		ConfigurationAutoloadModel nextCam = getModelForNextExecution(ctx);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_SCEDULE_AUTOLOAD);
		if (nextCam != null) {
			Bundle bundle = new Bundle();
			nextCam.saveToBundle(bundle);
			intent.putExtra(EXTRA_VALUES, bundle);
			PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (nextCam.isExactScheduling()) {
				am.setRepeating(AlarmManager.RTC_WAKEUP, nextCam.getNextExecution(), -1, operation);
			} else {
				am.setInexactRepeating(AlarmManager.RTC_WAKEUP, nextCam.getNextExecution(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, operation);
			}
		} else {
			PendingIntent operation = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			am.cancel(operation);
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ACTION_SCEDULE_AUTOLOAD.equals(intent.getAction())) {
			Bundle bundle = intent.getBundleExtra(EXTRA_VALUES);
			if (bundle != null && bundle.size() > 0) {
				ConfigurationAutoloadModel cam = new ConfigurationAutoloadModel(bundle);
				if (cam != null) {
					String configuration = cam.getConfiguration();
					if (!TextUtils.isEmpty(configuration)) {
						try {
							SettingsStorage settings = SettingsStorage.getInstance();
							BackupRestoreHelper brh = new BackupRestoreHelper(this);
							brh.restoreConfiguration(configuration, false, false);
							Logger.addToLog("Loaded configuration " + configuration);
							settings.setCurrentConfiguration(configuration);
						} catch (Exception e) {
							Logger.e("Cannot autoload configuration " + configuration, e);
						}
					}
				}
			}
		}
		scheduleNextEvent(this);
	}

	@Override
	public Context getContext() {
		return getApplicationContext();
	}

	@Override
	public void hasFinished(boolean success) {
		// no need to do something
	}
}
