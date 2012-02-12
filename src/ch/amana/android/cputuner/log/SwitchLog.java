package ch.amana.android.cputuner.log;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;

public class SwitchLog extends BroadcastReceiver {
	private static SwitchLog instance;

	private static final String ACTION_ADD_TO_LOG = "ch.amana.android.cputuner.ACTION_ADD_TO_LOG";
	public static final String ACTION_FLUSH_LOG = "ch.amana.android.cputuner.ACTION_FLUSH_LOG";
	public static final String EXTRA_LOG_ENTRY = DB.SwitchLogDB.NAME_MESSAGE;
	public static final String EXTRA_FLUSH_LOG = "EXTRA_FLUSH_LOG";

	private static final long HOURS_IN_MILLIES = 1000 * 60 * 60;

	private final ArrayList<ContentProviderOperation> operations;

	public static void start(Context ctx) {
		if (instance == null) {
			instance = new SwitchLog(ctx);
		}
		instance.onReceive(ctx, getLogIntent(ctx.getString(R.string.log_msg_switchlog_start), false));
		ctx.registerReceiver(instance, new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(ACTION_ADD_TO_LOG));
		ctx.registerReceiver(instance, new IntentFilter(ACTION_FLUSH_LOG));
	}

	public static void stop(final Context ctx) {
		// make sure all messages are received
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (instance != null) {
						instance.onReceive(ctx, getLogIntent(ctx.getString(R.string.log_msg_switchlog_stop), false));
						instance.flushLogToDB(ctx);
						ctx.unregisterReceiver(instance);
					}
				} catch (Throwable e) {
				}
				instance = null;
			}
		}, 2000);
	}

	public SwitchLog(Context ctx) {
		super();
		operations = new ArrayList<ContentProviderOperation>();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!SettingsStorage.getInstance().isEnableLogProfileSwitches()) {
			return;
		}
		if (intent == null) {
			return;
		}
		if (ACTION_FLUSH_LOG.equals(intent.getAction()) || intent.getBooleanExtra(ACTION_FLUSH_LOG, false)) {
			flushLogToDB(context);
			return;
		}
		ContentValues values = new ContentValues();
		values.put(DB.SwitchLogDB.NAME_TIME, System.currentTimeMillis());
		values.put(DB.SwitchLogDB.NAME_MESSAGE, intent.getStringExtra(EXTRA_LOG_ENTRY));
		values.put(DB.SwitchLogDB.NAME_TRIGGER, intent.getStringExtra(DB.SwitchLogDB.NAME_TRIGGER));
		values.put(DB.SwitchLogDB.NAME_PROFILE, intent.getStringExtra(DB.SwitchLogDB.NAME_PROFILE));
		values.put(DB.SwitchLogDB.NAME_VIRTGOV, intent.getStringExtra(DB.SwitchLogDB.NAME_VIRTGOV));
		values.put(DB.SwitchLogDB.NAME_AC, intent.getIntExtra(DB.SwitchLogDB.NAME_AC, -1));
		values.put(DB.SwitchLogDB.NAME_BATTERY, intent.getIntExtra(DB.SwitchLogDB.NAME_BATTERY, -1));
		values.put(DB.SwitchLogDB.NAME_CALL, intent.getIntExtra(DB.SwitchLogDB.NAME_CALL, -1));
		values.put(DB.SwitchLogDB.NAME_HOT, intent.getIntExtra(DB.SwitchLogDB.NAME_HOT, -1));
		values.put(DB.SwitchLogDB.NAME_LOCKED, intent.getIntExtra(DB.SwitchLogDB.NAME_LOCKED, -1));

		Builder opp = ContentProviderOperation.newInsert(DB.SwitchLogDB.CONTENT_URI);
		opp.withValues(values);
		operations.add(opp.build());
		if (operations.size() > 10 || intent.getBooleanExtra(EXTRA_FLUSH_LOG, false)) {
			flushLogToDB(context);
		}

	}

	private void flushLogToDB(Context context) {
		try {
			int logSize = SettingsStorage.getInstance().getProfileSwitchLogSize();
			if (logSize > -1) {
				Builder opp = ContentProviderOperation.newDelete(DB.SwitchLogDB.CONTENT_URI);
				String time = Long.toString(System.currentTimeMillis() - (logSize * HOURS_IN_MILLIES));
				opp.withSelection(DB.SwitchLogDB.SELECTION_BY_TIME, new String[] { time });
				operations.add(opp.build());
			}

			context.getContentResolver().applyBatch(CpuTunerProvider.AUTHORITY, operations);
			operations.clear();
		} catch (Exception e) {
			Logger.w("Cannot flush to switch log");
		}
	}

	public static void addToLog(Context ctx, String msg) {
		addToLog(ctx, msg, false);
	}

	public static void addToLog(Context ctx, String msg, boolean flush) {
		ctx.sendBroadcast(getLogIntent(msg, flush));
	}

	private static Intent getLogIntent(String msg, boolean flush) {
		Intent intent = new Intent(SwitchLog.ACTION_ADD_TO_LOG);
		intent.putExtra(SwitchLog.EXTRA_LOG_ENTRY, msg);
		if (flush) {
			intent.putExtra(SwitchLog.EXTRA_FLUSH_LOG, flush);
		}
		PowerProfiles powerProfiles = PowerProfiles.getInstance();
		if (powerProfiles != null) {
			intent.putExtra(DB.SwitchLogDB.NAME_TRIGGER, powerProfiles.getCurrentTriggerName());
			intent.putExtra(DB.SwitchLogDB.NAME_PROFILE, powerProfiles.getCurrentProfileName());
			intent.putExtra(DB.SwitchLogDB.NAME_VIRTGOV, powerProfiles.getCurrentVirtGovName());
			intent.putExtra(DB.SwitchLogDB.NAME_AC, powerProfiles.isAcPower() ? 1 : 0);
			intent.putExtra(DB.SwitchLogDB.NAME_BATTERY, powerProfiles.getBatteryLevel());
			intent.putExtra(DB.SwitchLogDB.NAME_CALL, powerProfiles.isCallInProgress() ? 1 : 0);
			intent.putExtra(DB.SwitchLogDB.NAME_HOT, powerProfiles.isBatteryHot() ? 1 : 0);
			intent.putExtra(DB.SwitchLogDB.NAME_LOCKED, powerProfiles.isScreenOff() ? 1 : 0);
		}
		return intent;
	}

}
