package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.helper.TimeInStateParser;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.provider.DB;
import ch.amana.android.cputuner.provider.DB.TimeInStateIndex;
import ch.amana.android.cputuner.provider.DB.TimeInStateValue;

public class StatisticsReceiver extends BroadcastReceiver {

	public static String BROADCAST_UPDATE_TIMEINSTATE = "ch.amana.android.cputuner.BROADCAST_UPDATE_TIMEINSTATE";
	private static Object lock = new Object();
	private static StatisticsReceiver receiver = null;

	private static TimeInStateParser oldTisParser = null;
	private static String triggerNameOld = null;
	private static String profileNameOld = null;
	private static String virtGovNameOld = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		SettingsStorage settings = SettingsStorage.getInstance(context);
		if (!settings.isRunStatisticsService() || !settings.isAdvancesStatistics()) {
			return;
		}
		final Context ctx = context;

		new Thread(new Runnable() {
			@Override
			public void run() {
				updateStatistics(ctx);
			}

		}).start();

	}

	@SuppressWarnings("null")
	private static void updateStatistics(Context context) {
		try {
			Logger.d("Adding timeinstate to input queue");
			//		SettingsStorage settings = SettingsStorage.getInstance(context);
			String timeinstate = CpuHandler.getInstance().getCpuTimeinstate();
			TimeInStateParser tisParser = new TimeInStateParser(timeinstate);
			String triggerName = triggerNameOld;
			String profileName = profileNameOld;
			String virtGovName = virtGovNameOld;
			triggerNameOld = PowerProfiles.getInstance(context).getCurrentTriggerName();
			profileNameOld = PowerProfiles.getInstance(context).getCurrentProfileName();
			virtGovNameOld = PowerProfiles.getInstance(context).getCurrentVirtGovName();

			if (oldTisParser != null && triggerName != null && profileName != null && virtGovName != null) {
				ContentResolver resolver = context.getContentResolver();

				Cursor indexCursor = resolver.query(TimeInStateIndex.CONTENT_URI, DB.PROJECTION_ID, TimeInStateIndex.SELECTION_TRIGGER_PROFILE_VIRTGOV, new String[] { triggerName,
						profileName, virtGovName }, TimeInStateIndex.SORTORDER_DEFAULT);

				long indexID = Long.MIN_VALUE;
				if (indexCursor.moveToFirst()) {
					indexID = indexCursor.getLong(DB.INDEX_ID);
				} else {
					ContentValues values = new ContentValues();
					values.put(TimeInStateIndex.NAME_TRIGGER, triggerName);
					values.put(TimeInStateIndex.NAME_PROFILE, profileName);
					values.put(TimeInStateIndex.NAME_VIRTGOV, virtGovName);
					Uri uri = resolver.insert(TimeInStateIndex.CONTENT_URI, values);
					indexID = ContentUris.parseId(uri);
				}
				if (indexCursor != null) {
					indexCursor.close();
				}

				tisParser.setBaseline(oldTisParser);

				String idStr = Long.toString(indexID);
				//			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(tisParser.getStates().size());
				//			Builder opp;
				for (Integer state : tisParser.getStates()) {
					Long time = tisParser.getTime(state);
					String[] selection = new String[] { idStr, Integer.toString(state) };
					Cursor c = resolver.query(TimeInStateValue.CONTENT_URI, TimeInStateValue.PROJECTION_DEFAULT, TimeInStateValue.SELECTION_BY_ID_STATE, selection,
							TimeInStateValue.SORTORDER_DEFAULT);
					ContentValues values = new ContentValues();
					if (c.moveToFirst()) {
						time += c.getLong(TimeInStateValue.INDEX_TIME);
						values = new ContentValues();
						values.put(TimeInStateValue.NAME_TIME, time);
						resolver.update(TimeInStateValue.CONTENT_URI, values, TimeInStateValue.SELECTION_BY_ID_STATE, selection);
						//					opp = ContentProviderOperation.newUpdate(TimeInStateValue.CONTENT_URI);
						//					opp.withSelection(TimeInStateValue.SELECTION_BY_ID_STATE, selection);
						//					opp.withValues(values);
					} else {
						values.put(TimeInStateValue.NAME_IDX, indexID);
						values.put(TimeInStateValue.NAME_STATE, state);
						values.put(TimeInStateValue.NAME_TIME, time);
						resolver.insert(TimeInStateValue.CONTENT_URI, values);
						//					opp = ContentProviderOperation.newUpdate(TimeInStateValue.CONTENT_URI);
						//					opp.withValues(values);
					}
					//				operations.add(opp.build());
					if (c != null) {
						c.close();
					}
				}
				//			try {
				//				Logger.w("goint to update state statistics");
				//				ContentProviderResult[] applyBatch = resolver.applyBatch(CpuTunerProvider.AUTHORITY, operations);
				//				Logger.w("updated state statistics");
				//			} catch (Exception e) {
				//				Logger.w("Cannot save time in state statistics", e);
				//			}
			}
			tisParser.setBaseline(null);
			oldTisParser = tisParser;
		} catch (Exception e) {
			Logger.i("Cannot save time in state information", e);
		}
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
