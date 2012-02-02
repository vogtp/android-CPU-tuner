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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.activity.ConfigurationManageActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;

public class InstallHelper {

	private static final int VERSION = 5;

	static class CpuGovernorSettings {
		String gov;
		int upThreshold = -1;
		int downThreshold = -1;
		public long virtGov;
		public String script;
		public int powersaveBias;

	}

	private static final String SORT_ORDER = DB.NAME_ID + " DESC";
	private static CpuGovernorSettings cgsPower;
	private static CpuGovernorSettings cgsNormal;
	private static CpuGovernorSettings cgsSave;
	private static CpuGovernorSettings cgsExtremSave;

	public static void initialise(final Context ctx) {
		SettingsStorage settings = SettingsStorage.getInstance(ctx);
		int defaultProfilesVersion = SettingsStorage.getInstance().getDefaultProfilesVersion();
		switch (defaultProfilesVersion) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
			Logger.i("Initalising cpu tuner to level 5");
			CpuHandler cpuHandler = CpuHandler.getInstance();
			int minCpuFreq = cpuHandler.getMinCpuFreq();
			int maxCpuFreq = cpuHandler.getMaxCpuFreq();
			if (minCpuFreq == maxCpuFreq) {
				int[] availCpuFreq = cpuHandler.getAvailCpuFreq(true);
				minCpuFreq = availCpuFreq[0];
				maxCpuFreq = availCpuFreq[availCpuFreq.length - 1];
			}
			settings.setMinFrequencyDefault(minCpuFreq);
			settings.setMaxFrequencyDefault(maxCpuFreq);

		}
		settings.migrateSettings();
		settings.setDefaultProfilesVersion(VERSION);
	}

	public static void ensureSetup(final Context ctx) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);
		alertBuilder.setTitle(R.string.msg_title_grant_root);
		alertBuilder.setMessage(R.string.msg_grant_root);
		alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ensureRoot(ctx);
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private static void ensureRoot(final Context ctx) {
		if (!RootHandler.isRoot()) {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);
			alertBuilder.setTitle("Root access failed");
			alertBuilder.setMessage("Cpu tuner will not work unless it has root access.");
			alertBuilder.setPositiveButton("Try again!", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ensureRoot(ctx);
				}
			});
			if (hasConfig(ctx)) {
				alertBuilder.setNeutralButton("Configure superuser", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.noshufou.android.su");
						ctx.startActivity(intent);
					}
				});
			}
			alertBuilder.setNegativeButton("continue", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					ensureConfiguration(ctx, true);
				}
			});
			AlertDialog alert = alertBuilder.create();
			alert.show();
		} else {
			ensureConfiguration(ctx, true);
		}
	}

	public static void ensureConfiguration(Context ctx, boolean startMain) {
		if (!hasConfig(ctx)) {
			Intent intent = new Intent(ctx, ConfigurationManageActivity.class);
			intent.putExtra(ConfigurationManageActivity.EXTRA_CLOSE_ON_LOAD, true);
			intent.putExtra(ConfigurationManageActivity.EXTRA_FIRST_RUN, true);
			intent.putExtra(ConfigurationManageActivity.EXTRA_ASK_LOAD_CONFIRMATION, false);
			intent.putExtra(ConfigurationManageActivity.EXTRA_TITLE, ctx.getString(R.string.title_load_configuration));
			ctx.startActivity(intent);
		} else {
			SettingsStorage.getInstance().firstRunDone();
			if (startMain) {
				ctx.startActivity(CpuTunerViewpagerActivity.getStartIntent(ctx));
			}
		}
	}

	public static boolean hasConfig(Context ctx) {
		boolean ret = true;
		ContentResolver resolver = ctx.getContentResolver();
		try {
			ret = ret && checkCursor(resolver, DB.CpuProfile.CONTENT_URI);
			ret = ret && checkCursor(resolver, DB.Trigger.CONTENT_URI);
			if (SettingsStorage.getInstance().isUseVirtualGovernors()) {
				ret = ret && checkCursor(resolver, DB.VirtualGovernor.CONTENT_URI);
			}
		} catch (Throwable e) {
			return false;
		}
		return ret;
	}

	private static boolean checkCursor(ContentResolver resolver, Uri contentUri) {
		Cursor c = null;
		try {
			c = resolver.query(contentUri, DB.PROJECTION_ID, null, null, null);
			if (c == null) {
				return false;
			}
			return c.moveToFirst();
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
	}

	public static void resetToDefault(final Context ctx) {
		Builder alertBuilder = new AlertDialog.Builder(ctx);
		alertBuilder.setTitle(R.string.title_reset_to_default);
		alertBuilder.setMessage(R.string.msg_reset_to_default);
		alertBuilder.setCancelable(false);
		alertBuilder.setPositiveButton(R.string.yes, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				CpuTunerProvider.deleteAllTables(ctx, true);
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

			Cursor cP = resolver.query(DB.CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
			if (cP == null || cP.getCount() < 1) {
				Cursor cT = resolver.query(DB.Trigger.CONTENT_URI, new String[] { DB.NAME_ID }, null, null, SORT_ORDER);
				if (cT == null || cT.getCount() < 1) {
					Toast.makeText(ctx, R.string.msg_loading_default_profiles, Toast.LENGTH_SHORT).show();
					CpuHandler cpuHandler = CpuHandler.getInstance();
					int freqMax = cpuHandler.getMaxCpuFreq();
					int freqMin = cpuHandler.getMinCpuFreq();
					if (freqMax < cpuHandler.getMinimumSensibleFrequency()) {
						int[] availCpuFreq = cpuHandler.getAvailCpuFreq(false);
						if (availCpuFreq != null && availCpuFreq.length > 0) {
							freqMax = availCpuFreq[0];
						}
					}
					String gov = cpuHandler.getCurCpuGov();

					List<String> availGov = Arrays.asList(cpuHandler.getAvailCpuGov());

					long profilePerformance = createCpuProfile(resolver, ctx.getString(R.string.profilename_performance), getPowerGov(ctx, resolver, availGov, gov), freqMax,
							freqMin);
					long profileGood = createCpuProfile(resolver, ctx.getString(R.string.profilename_good), getNormalGov(ctx, resolver, availGov, gov), freqMax, freqMin);
					long profileNormal = createCpuProfile(resolver, ctx.getString(R.string.profilename_normal), getNormalGov(ctx, resolver, availGov, gov), freqMax, freqMin);
					long profileScreenOff = createCpuProfile(resolver, ctx.getString(R.string.profilename_screen_off), getExtremSaveGov(ctx, resolver, availGov, gov), freqMax,
							freqMin);
					long profilePowersave = createCpuProfile(resolver, "Powersave", getSaveGov(ctx, resolver, availGov, gov), freqMax, freqMin);
					long profileExtremPowersave = createCpuProfile(resolver, ctx.getString(R.string.profilename_extreme_powersave), getExtremSaveGov(ctx, resolver, availGov, gov),
							freqMax, freqMin);

					createTrigger(resolver, ctx.getString(R.string.triggername_battery_full), 100, profileScreenOff, profileGood, profilePerformance, profilePerformance);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_used), 85, profileScreenOff, profileNormal, profileGood, profilePerformance);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_low), 65, profileScreenOff, profilePowersave, profileNormal, profileGood);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_empty), 45, profileExtremPowersave, profilePowersave, profilePowersave, profileGood);
					createTrigger(resolver, ctx.getString(R.string.triggername_battery_critical), 25, profileExtremPowersave, profileExtremPowersave, profilePowersave,
							profileNormal);

				}

				if (cT != null) {
					cT.close();
				}
			} else {
				// migrate to triggers
				if (cP != null) {

					while (cP.moveToNext()) {
						ProfileModel profileModel = new ProfileModel(cP);
						long virtGovId = migrateToVirtualGov(ctx, resolver, profileModel);
						profileModel.setVirtualGovernor(virtGovId);
						insertOrUpdate(resolver, DB.CpuProfile.CONTENT_URI, profileModel.getValues());
					}
				}
			}
			if (cP != null) {
				cP.close();
			}

		} finally {
			PowerProfiles.setUpdateTrigger(true);
		}
	}

	private static long migrateToVirtualGov(Context ctx, ContentResolver resolver, ProfileModel profileModel) {
		StringBuilder sb = new StringBuilder();
		sb.append(VirtualGovernor.NAME_REAL_GOVERNOR).append("='").append(profileModel.getGov()).append("'");
		sb.append(" & ").append(VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN).append("=").append(profileModel.getGovernorThresholdDown());
		sb.append(" & ").append(VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP).append("=").append(profileModel.getGovernorThresholdUp());
		String script = profileModel.getScript();
		if (!TextUtils.isEmpty(script)) {
			sb.append(" && ").append(VirtualGovernor.NAME_SCRIPT).append("=").append(script);
		}
		sb.append(" && ").append(VirtualGovernor.NAME_POWERSEAVE_BIAS).append("=").append(profileModel.getPowersaveBias());
		try {
			Cursor cVH = resolver.query(DB.VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, sb.toString(), null, DB.VirtualGovernor.SORTORDER_DEFAULT);
			if (cVH != null && cVH.getCount() > 1) {
				cVH.moveToFirst();
				return cVH.getLong(DB.INDEX_ID);
			}
			if (cVH != null) {
				cVH.close();
			}
		} catch (Throwable e) {
			Logger.i("Cannot upgrade virtual governor", e);
		}

		CpuGovernorSettings cgs = new CpuGovernorSettings();
		cgs.gov = profileModel.getGov();
		cgs.downThreshold = profileModel.getGovernorThresholdDown();
		cgs.upThreshold = profileModel.getGovernorThresholdUp();
		cgs.script = script;
		cgs.powersaveBias = profileModel.getPowersaveBias();
		return createVirtualGovernor(resolver, profileModel.getProfileName() + " " + ctx.getString(R.string.virtual_governor), cgs);
	}

	private static long createVirtualGovernor(ContentResolver resolver, String name, CpuGovernorSettings cgs) {
		ContentValues values = new ContentValues();
		values.put(DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, name);
		values.put(DB.VirtualGovernor.NAME_REAL_GOVERNOR, cgs.gov);
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, cgs.downThreshold);
		values.put(DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP, cgs.upThreshold);
		values.put(DB.VirtualGovernor.NAME_SCRIPT, cgs.script);
		values.put(DB.VirtualGovernor.NAME_POWERSEAVE_BIAS, cgs.powersaveBias);
		return insertOrUpdate(resolver, DB.VirtualGovernor.CONTENT_URI, values);
	}

	private static void createTrigger(ContentResolver resolver, String name, int batLevel, long screenOff, long battery, long power, long call) {
		ContentValues values = new ContentValues();
		values.put(DB.Trigger.NAME_TRIGGER_NAME, name);
		values.put(DB.Trigger.NAME_BATTERY_LEVEL, batLevel);
		values.put(DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID, screenOff);
		values.put(DB.Trigger.NAME_BATTERY_PROFILE_ID, battery);
		values.put(DB.Trigger.NAME_POWER_PROFILE_ID, power);
		values.put(DB.Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID, call);
		insertOrUpdate(resolver, DB.Trigger.CONTENT_URI, values);
	}

	private static long createCpuProfile(ContentResolver resolver, String name, CpuGovernorSettings gov, int freqMax, int freqMin) {
		return createCpuProfile(resolver, name, gov, freqMax, freqMin, 0, 0, 0, 0, 0);
	}

	private static long createCpuProfile(ContentResolver resolver, String name, CpuGovernorSettings gov, int freqMax, int freqMin, int wifiState, int gpsState, int btState,
			int mbState, int bsState) {

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
			values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP, gov.upThreshold);
		}
		if (gov.downThreshold > -1) {
			values.put(DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN, gov.downThreshold);
		}
		return insertOrUpdate(resolver, DB.CpuProfile.CONTENT_URI, values);
	}

	private static CpuGovernorSettings getPowerGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		if (cgsPower == null) {
			cgsPower = new CpuGovernorSettings();
			if (list == null || list.size() < 1) {
				cgsPower.gov = gov;
			} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
				cgsPower.gov = CpuHandler.GOV_ONDEMAND;
				cgsPower.upThreshold = 85;
			} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
				cgsPower.gov = CpuHandler.GOV_CONSERVATIVE;
				cgsPower.upThreshold = 85;
				cgsPower.downThreshold = 40;
			} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
				cgsPower.gov = CpuHandler.GOV_INTERACTIVE;
			}
			cgsPower.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_full_speed), cgsPower);
		}
		return cgsPower;
	}

	private static CpuGovernorSettings getNormalGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		if (cgsNormal == null) {
			cgsNormal = new CpuGovernorSettings();
			if (list == null || list.size() < 1) {
				cgsNormal.gov = gov;
			} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
				cgsNormal.gov = CpuHandler.GOV_ONDEMAND;
				cgsNormal.upThreshold = 90;
			} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
				cgsNormal.gov = CpuHandler.GOV_CONSERVATIVE;
				cgsNormal.upThreshold = 90;
				cgsNormal.downThreshold = 50;
			} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
				cgsNormal.gov = CpuHandler.GOV_INTERACTIVE;
			}
			cgsNormal.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_normal), cgsNormal);
		}
		return cgsNormal;
	}

	private static CpuGovernorSettings getSaveGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		if (cgsSave == null) {
			cgsSave = new CpuGovernorSettings();
			if (list == null || list.size() < 1) {
				cgsSave.gov = gov;
			}
			if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
				cgsSave.gov = CpuHandler.GOV_CONSERVATIVE;
				cgsSave.upThreshold = 95;
				cgsSave.downThreshold = 80;
			} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
				cgsSave.gov = CpuHandler.GOV_ONDEMAND;
				cgsSave.upThreshold = 94;
			} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
				cgsSave.gov = CpuHandler.GOV_INTERACTIVE;
			}
			cgsSave.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_save_battery), cgsSave);
		}
		return cgsSave;
	}

	private static CpuGovernorSettings getExtremSaveGov(Context ctx, ContentResolver resolver, List<String> list, String gov) {
		if (cgsExtremSave == null) {
			cgsExtremSave = new CpuGovernorSettings();
			if (list == null || list.size() < 1) {
				cgsExtremSave.gov = gov;
			} else if (list.contains(CpuHandler.GOV_CONSERVATIVE)) {
				cgsExtremSave.gov = CpuHandler.GOV_CONSERVATIVE;
				cgsExtremSave.upThreshold = 98;
				cgsExtremSave.downThreshold = 95;
			} else if (list.contains(CpuHandler.GOV_ONDEMAND)) {
				cgsExtremSave.gov = CpuHandler.GOV_ONDEMAND;
				cgsExtremSave.upThreshold = 97;
			} else if (list.contains(CpuHandler.GOV_INTERACTIVE)) {
				cgsExtremSave.gov = CpuHandler.GOV_INTERACTIVE;
			}
			cgsExtremSave.virtGov = createVirtualGovernor(resolver, ctx.getString(R.string.virtgovname_extrem_save_battery), cgsExtremSave);
		}
		return cgsExtremSave;
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

	public static void updateProfilesFromVirtGovs(Context ctx) {
		ContentResolver contentResolver = ctx.getContentResolver();
		Cursor cursorVirtGov = contentResolver.query(DB.VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, null, null, VirtualGovernor.SORTORDER_DEFAULT);
		if (cursorVirtGov == null) {
			return;
		}
		while (cursorVirtGov.moveToNext()) {
			VirtualGovernorModel virtualGovModel = new VirtualGovernorModel(cursorVirtGov);
			Cursor c = contentResolver.query(DB.CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_DEFAULT, CpuProfile.NAME_VIRTUAL_GOVERNOR + "=?",
					new String[] { virtualGovModel.getDbId() + "" },
					VirtualGovernor.SORTORDER_DEFAULT);
			while (c.moveToNext()) {
				ProfileModel profile = new ProfileModel(c);
				virtualGovModel.applyToProfile(profile);
			}
		}
	}

}
