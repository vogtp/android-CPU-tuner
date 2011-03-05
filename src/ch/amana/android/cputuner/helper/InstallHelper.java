package ch.amana.android.cputuner.helper;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class InstallHelper {

	static class CpuGovernorSettings {
		String gov;
		int upThreshold = -1;
		int downThreshold = -1;
		public long virtGov;

	}

	private static final String SORT_ORDER = DB.NAME_ID + " DESC";
	private static final int VERSION = 1;

	public static void resetToDefault(final Context ctx) {
		Builder alertBuilder = new AlertDialog.Builder(ctx);
		alertBuilder.setTitle(R.string.title_reset_to_default);
		alertBuilder.setMessage(R.string.msg_reset_to_default);
		alertBuilder.setCancelable(false);
		alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContentResolver resolver = ctx.getContentResolver();
				resolver.delete(DB.Trigger.CONTENT_URI, null, null);
				resolver.delete(DB.CpuProfile.CONTENT_URI, null, null);
				resolver.delete(DB.VirtualGovernor.CONTENT_URI, null, null);
				updateDefaultProfiles(ctx);

			}
		});
		alertBuilder.setNegativeButton(R.string.no, null);
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private static void updateDefaultProfiles(Context ctx) {
		try {
			PowerProfiles.setUpdateTrigger(false);

			ContentResolver resolver = ctx.getContentResolver();

			Cursor cP = resolver.query(DB.CpuProfile.CONTENT_URI, new String[] { DB.NAME_ID }, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
			if (cP == null || cP.getCount() < 1) {
				Cursor cT = resolver.query(DB.Trigger.CONTENT_URI, new String[] { DB.NAME_ID }, null, null, SORT_ORDER);
				if (cT == null || cT.getCount() < 1) {
					Toast.makeText(ctx, R.string.msg_loading_default_profiles, Toast.LENGTH_SHORT).show();
					CpuHandler cpuHandler = CpuHandler.getInstance();
					int freqMax = cpuHandler.getMaxCpuFreq();
					int freqMin = cpuHandler.getMinCpuFreq();
					if (freqMin < cpuHandler.getMinimumSensibleFrequency()) {
						int[] availCpuFreq = cpuHandler.getAvailCpuFreq();
						if (availCpuFreq != null && availCpuFreq.length > 0) {
							freqMin = availCpuFreq[0];
						}
					}
					String gov = cpuHandler.getCurCpuGov();

					List<String> availGov = Arrays.asList(cpuHandler.getAvailCpuGov());

					long profilePerformance = createCpuProfile(resolver, ctx.getString(R.string.profilename_performance), getPowerGov(ctx, resolver, availGov, gov), freqMax,
							freqMin, 0, 0, 0, 0, 1);
					long profileGood = createCpuProfile(resolver, ctx.getString(R.string.profilename_good), getNormalGov(ctx, resolver, availGov, gov), freqMax, freqMin, 0, 0, 0,
							0, 1);
					long profileNormal = createCpuProfile(resolver, ctx.getString(R.string.profilename_normal), getNormalGov(ctx, resolver, availGov, gov), freqMax, freqMin);
					long profileScreenOff = createCpuProfile(resolver, ctx.getString(R.string.profilename_screen_off), getExtremSaveGov(ctx, resolver, availGov, gov), freqMax,
							freqMin);
					long profilePowersave = createCpuProfile(resolver, "Powersave", getSaveGov(ctx, resolver, availGov, gov), freqMax, freqMin, 0, 0, 0, 1, 0);
					long profileExtremPowersave = createCpuProfile(resolver, ctx.getString(R.string.profilename_extreme_powersave), getExtremSaveGov(ctx, resolver, availGov, gov),
							freqMax, freqMin,
							2, 2, 2,
							1, 2);

					createTrigger(resolver, ctx.getString(R.string.triggername_battery_full), 100, profileScreenOff, profileGood, profilePerformance);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_used), 85, profileScreenOff, profileGood, profileGood);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_low), 65, profileScreenOff, profilePowersave, profileNormal);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_empty), 45, profileExtremPowersave, profilePowersave, profilePowersave);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_critical), 25, profileExtremPowersave, profileExtremPowersave, profilePowersave);

				}

				if (cP != null) {
					cP.close();
				}
				if (cT != null) {
					cT.close();
				}
			}
			Cursor cT = resolver.query(DB.CpuProfile.CONTENT_URI, new String[] { DB.NAME_ID }, DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + "<1", null,
					SORT_ORDER);
			if (cT != null && cT.getCount() > 1) {
				OpenHelper oh = new OpenHelper(ctx);
				oh.getReadableDatabase().execSQL(
						"update " + DB.CpuProfile.TABLE_NAME + " set " + DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " = 98 where "
								+ DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " < 1");
				oh.getReadableDatabase().execSQL(
						"update " + DB.CpuProfile.TABLE_NAME + " set " + DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " = 95 where "
								+ DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " < 1");
			}
			if (cT != null) {
				cT.close();
			}
			SettingsStorage.getInstance().setDefaultProfilesVersion(VERSION);
		} finally {
			PowerProfiles.setUpdateTrigger(true);
		}
	}

	private static long createVirtualGovernor(ContentResolver resolver, String name, CpuGovernorSettings cgs) {
		ContentValues values = new ContentValues();
		values.put(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, name);
		values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, cgs.gov);
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, cgs.downThreshold);
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, cgs.upThreshold);
		return insertOrUpdate(resolver, DB.VirtualGovernor.CONTENT_URI, values);
	}

	private static void createTrigger(ContentResolver resolver, String name, int batLevel, long screenOff, long battery, long power) {
		ContentValues values = new ContentValues();
		values.put(DB.Trigger.NAME_TRIGGER_NAME, name);
		values.put(DB.Trigger.NAME_BATTERY_LEVEL, batLevel);
		values.put(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, screenOff);
		values.put(DB.Trigger.NAME_BATTERY_PROFILE_ID, battery);
		values.put(DB.Trigger.NAME_POWER_PROFILE_ID, power);
		insertOrUpdate(resolver, DB.Trigger.CONTENT_URI, values);
	}

	private static long createCpuProfile(ContentResolver resolver, String name, CpuGovernorSettings gov, int freqMax, int freqMin) {
		return createCpuProfile(resolver, name, gov, freqMax, freqMin, 0, 0, 0, 0, 0);
	}

	private static long createCpuProfile(ContentResolver resolver, String name, CpuGovernorSettings gov, int freqMax, int freqMin,
			int wifiState, int gpsState, int btState, int mbState, int bsState) {

		ContentValues values = new ContentValues();
		values.put(DB.CpuProfile.NAME_PROFILE_NAME, name);
		values.put(DB.CpuProfile.NAME_GOVERNOR, gov.gov);
		values.put(DB.CpuProfile.NAME_VIRTUAL_GOVERNOR, gov.virtGov);
		values.put(DB.CpuProfile.NAME_FREQUENCY_MAX, freqMax);
		values.put(DB.CpuProfile.NAME_FREQUENCY_MIN, freqMin);
		values.put(DB.CpuProfile.NAME_WIFI_STATE, wifiState);
		values.put(DB.CpuProfile.NAME_GPS_STATE, gpsState);
		values.put(DB.CpuProfile.NAME_BLUETOOTH_STATE, btState);
		values.put(DB.CpuProfile.NAME_MOBILEDATA_3G_STATE, mbState);
		values.put(DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE, bsState);
		if (gov.upThreshold > -1) {
			values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP,
					gov.upThreshold);
		}
		if (gov.downThreshold > -1) {
			values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN,
					gov.downThreshold);
		}
		return insertOrUpdate(resolver, DB.CpuProfile.CONTENT_URI, values);
	}

	private static CpuGovernorSettings getPowerGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		CpuGovernorSettings cgs = new CpuGovernorSettings();
		if (list == null || list.size() < 1) {
			cgs.gov = gov;
		} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
			cgs.gov = CpuHandler.GOV_ONDEMAND;
			cgs.upThreshold = 85;
		} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
			cgs.gov = CpuHandler.GOV_CONSERVATIVE;
			cgs.upThreshold = 85;
			cgs.downThreshold = 40;
		} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
			cgs.gov = CpuHandler.GOV_INTERACTIVE;
		}
		cgs.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_full_speed), cgs);
		return cgs;
	}

	private static CpuGovernorSettings getNormalGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		CpuGovernorSettings cgs = new CpuGovernorSettings();
		if (list == null || list.size() < 1) {
			cgs.gov = gov;
		} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
			cgs.gov = CpuHandler.GOV_ONDEMAND;
			cgs.upThreshold = 90;
		} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
			cgs.gov = CpuHandler.GOV_CONSERVATIVE;
			cgs.upThreshold = 90;
			cgs.downThreshold = 50;
		} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
			cgs.gov = CpuHandler.GOV_INTERACTIVE;
		}
		cgs.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_normal), cgs);
		return cgs;
	}

	private static CpuGovernorSettings getSaveGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		CpuGovernorSettings cgs = new CpuGovernorSettings();
		if (list == null || list.size() < 1) {
			cgs.gov = gov;
		}
		if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
			cgs.gov = CpuHandler.GOV_CONSERVATIVE;
			cgs.upThreshold = 95;
			cgs.downThreshold = 80;
		} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
			cgs.gov = CpuHandler.GOV_ONDEMAND;
			cgs.upThreshold = 94;
		} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
			cgs.gov = CpuHandler.GOV_INTERACTIVE;
		}
		cgs.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_save_battery), cgs);
		return cgs;
	}

	private static CpuGovernorSettings getExtremSaveGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		CpuGovernorSettings cgs = new CpuGovernorSettings();
		if (list == null || list.size() < 1) {
			cgs.gov = gov;
		} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
			cgs.gov = CpuHandler.GOV_CONSERVATIVE;
			cgs.upThreshold = 98;
			cgs.downThreshold = 95;
		} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
			cgs.gov = CpuHandler.GOV_ONDEMAND;
			cgs.upThreshold = 97;
		} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
			cgs.gov = CpuHandler.GOV_INTERACTIVE;
		}
		cgs.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_extrem_save_battery), cgs);
		return cgs;
	}

	public static long insertOrUpdate(ContentResolver resolver, Uri contentUri, ContentValues values) {
		String selection = DB.NAME_ID + "=" + values.getAsString(DB.NAME_ID);
		Cursor c = resolver.query(contentUri, new String[] { DB.NAME_ID }, selection, null, SORT_ORDER);
		long id;
		if (c != null && c.moveToFirst()) {
			id = resolver.update(contentUri, values, selection, null);
		} else {
			values.remove(DB.NAME_ID);
			Uri uri = resolver.insert(contentUri, values);
			id = ContentUris.parseId(uri);
		}
		if (c != null && !c.isClosed()) {
			c.close();
		}
		return id;
	}

	public static void populateDb(Context ctx) {
		if (VERSION > SettingsStorage.getInstance().getDefaultProfilesVersion()) {
			try{
				updateDefaultProfiles(ctx);
			}catch(Exception e){
				Logger.e("Cannot create profiles",e);
			}
		}
	}

}
