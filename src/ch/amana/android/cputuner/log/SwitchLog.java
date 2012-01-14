package ch.amana.android.cputuner.log;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;

public class SwitchLog extends BroadcastReceiver {
	private static SwitchLog instance;

	public static final String ACTION_ADD_TO_LOG = "ch.amana.android.cputuner.ACTION_ADD_TO_LOG";
	public static final String ACTION_FLUSH_LOG = "ch.amana.android.cputuner.ACTION_FLUSH_LOG";
	public static final String EXTRA_LOG_ENTRY = DB.SwitchLogDB.NAME_MESSAGE;
	public static final String EXTRA_FLUSH_LOG = "EXTRA_FLUSH_LOG";

	private static final long HOURS_IN_MILLIES = 1000 * 60 * 60;

	private final ArrayList<ContentProviderOperation> operations;

	public static void start(Context ctx) {
		if (instance == null) {
			instance = new SwitchLog(ctx);
		}
		ctx.registerReceiver(instance, new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(ACTION_ADD_TO_LOG));
		ctx.registerReceiver(instance, new IntentFilter(ACTION_FLUSH_LOG));
	}

	public static void stop(Context ctx) {
		try {
			instance.flushLogToDB(ctx);
			ctx.unregisterReceiver(instance);
		} catch (Throwable e) {
		}
		instance = null;
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
		if (ACTION_FLUSH_LOG.equals(intent.getAction())) {
			flushLogToDB(context);
		}
		String logEntry = intent.getStringExtra(EXTRA_LOG_ENTRY);
		if (logEntry != null) {
			long now = System.currentTimeMillis();
			ContentValues values = new ContentValues();
			values.put(DB.SwitchLogDB.NAME_TIME, now);
			values.put(DB.SwitchLogDB.NAME_MESSAGE, logEntry);
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

}
