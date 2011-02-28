package ch.amana.android.cputuner.provider.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.CpuTunerProvider;

public interface DB {

	public static final String DATABASE_NAME = "cputuner";

	public static final String NAME_ID = "_id";
	public static final int INDEX_ID = 0;

	public class OpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 8;

		private static final String CREATE_TRIGGERS_TABLE = "create table if not exists " + Trigger.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
				+ DB.Trigger.NAME_TRIGGER_NAME + " text, " + DB.Trigger.NAME_BATTERY_LEVEL + " int," + DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_BATTERY_PROFILE_ID + " long," + DB.Trigger.NAME_POWER_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_POW + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_POW + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_BAT + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_BAT + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_LCK + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_LCK + " long,"
				+ DB.Trigger.NAME_HOT_PROFILE_ID + " long default -1," + DB.Trigger.NAME_POWER_CURRENT_SUM_HOT + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_CNT_HOT+ " long,"+ DB.Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_POWER_CURRENT_SUM_CALL + " long," + DB.Trigger.NAME_POWER_CURRENT_CNT_CALL + " long)";

		private static final String CREATE_CPUPROFILES_TABLE = "create table if not exists " + CpuProfile.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.CpuProfile.NAME_PROFILE_NAME + " text, " + DB.CpuProfile.NAME_GOVERNOR + " text," + DB.CpuProfile.NAME_FREQUENCY_MAX + " int,"
				+ DB.CpuProfile.NAME_FREQUENCY_MIN + " int," + DB.CpuProfile.NAME_WIFI_STATE + " int," + DB.CpuProfile.NAME_GPS_STATE + " int,"
				+ DB.CpuProfile.NAME_BLUETOOTH_STATE + " int," + DB.CpuProfile.NAME_MOBILEDATA_STATE + " int,"
				+ DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " int DEFAULT 98," + DB.CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 95,"
				+ DB.CpuProfile.NAME_BACKGROUND_SYNC_STATE + " int, " + DB.CpuProfile.NAME_VIRTUAL_GOVERNOR + " int default -1)";

		private static final String CREATE_VIRTUAL_GOVERNOR_TABLE = "create table if not exists " + VirtualGovernor.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME + " text, " + DB.VirtualGovernor.NAME_REAL_GOVERNOR + " text,"
				+ DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP + " int DEFAULT 98," + DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 95)";

		public OpenHelper(Context context) {
			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TRIGGERS_TABLE);
			db.execSQL(CREATE_CPUPROFILES_TABLE);
			db.execSQL(CREATE_VIRTUAL_GOVERNOR_TABLE);
			db.execSQL("create index idx_trigger_battery_level on " + Trigger.TABLE_NAME + " (" + Trigger.NAME_BATTERY_LEVEL + "); ");
			db.execSQL("create index idx_cpuprofiles_profilename on " + CpuProfile.TABLE_NAME + " (" + CpuProfile.NAME_PROFILE_NAME + "); ");
			Logger.i("Created tables ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				Logger.w("Upgrading to DB Version 2...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_WIFI_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GPS_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_BLUETOOTH_STATE + " int;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_MOBILEDATA_STATE + " int;");
				// nobreak

			case 2:
				Logger.w("Upgrading to DB Version 3...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_POW + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_POW + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_BAT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_BAT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_LCK + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_LCK + " long;");

			case 3:
				Logger.w("Upgrading to DB Version 4...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GOVERNOR_THRESHOLD_UP + " int DEFAULT 98;");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_GOVERNOR_THRESHOLD_DOWN + " int DEFAULT 95;");

			case 4:
				Logger.w("Upgrading to DB Version 5...");
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + CpuProfile.NAME_BACKGROUND_SYNC_STATE + " int default 0;");

			case 5:
				Logger.w("Upgrading to DB Version 6...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_HOT_PROFILE_ID + " long default -1;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_HOT + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_HOT + " long;");

			case 6:
				Logger.w("Upgrading to DB Version 7...");
				db.execSQL(CREATE_VIRTUAL_GOVERNOR_TABLE);
				db.execSQL("alter table " + CpuProfile.TABLE_NAME + " add column " + DB.CpuProfile.NAME_VIRTUAL_GOVERNOR + " int default -1;");

			case 7:
				Logger.w("Upgrading to DB Version 8...");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_CALL_IN_PROGRESS_PROFILE_ID + " long default -1;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_SUM_CALL + " long;");
				db.execSQL("alter table " + Trigger.TABLE_NAME + " add column " + Trigger.NAME_POWER_CURRENT_CNT_CALL + " long;");
			default:
				Logger.w("Finished DB upgrading!");
				break;
			}
		}
	}

	public interface Trigger {

		static final String TABLE_NAME = "triggers";

		public static final String CONTENT_ITEM_NAME = "trigger";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_TRIGGER_NAME = "triggerName";
		public static final String NAME_BATTERY_LEVEL = "batteryLevel";
		public static final String NAME_SCREEN_OFF_PROFILE_ID = "screenOffProfileId";
		public static final String NAME_BATTERY_PROFILE_ID = "batteryProfileId";
		public static final String NAME_POWER_PROFILE_ID = "powerProfileId";
		public static final String NAME_POWER_CURRENT_SUM_POW = "powerCurrentSumPower";
		public static final String NAME_POWER_CURRENT_CNT_POW = "powerCurrentCntPower";
		public static final String NAME_POWER_CURRENT_SUM_BAT = "powerCurrentSumBattery";
		public static final String NAME_POWER_CURRENT_CNT_BAT = "powerCurrentCntBattery";
		public static final String NAME_POWER_CURRENT_SUM_LCK = "powerCurrentSumLocked";
		public static final String NAME_POWER_CURRENT_CNT_LCK = "powerCurrentCntLocked";
		public static final String NAME_HOT_PROFILE_ID = "hotProfileId";
		public static final String NAME_POWER_CURRENT_SUM_HOT = "powerCurrentSumHot";
		public static final String NAME_POWER_CURRENT_CNT_HOT = "powerCurrentCntHot";
		public static final String NAME_CALL_IN_PROGRESS_PROFILE_ID = "callInProgressProfileId";
		public static final String NAME_POWER_CURRENT_SUM_CALL = "powerCurrentSumCall";
		public static final String NAME_POWER_CURRENT_CNT_CALL = "powerCurrentCntCall";

		public static final int INDEX_TRIGGER_NAME = 1;
		public static final int INDEX_BATTERY_LEVEL = 2;
		public static final int INDEX_SCREEN_OFF_PROFILE_ID = 3;
		public static final int INDEX_BATTERY_PROFILE_ID = 4;
		public static final int INDEX_POWER_PROFILE_ID = 5;
		public static final int INDEX_POWER_CURRENT_SUM_POW = 6;
		public static final int INDEX_POWER_CURRENT_CNT_POW = 7;
		public static final int INDEX_POWER_CURRENT_SUM_BAT = 8;
		public static final int INDEX_POWER_CURRENT_CNT_BAT = 9;
		public static final int INDEX_POWER_CURRENT_SUM_LCK = 10;
		public static final int INDEX_POWER_CURRENT_CNT_LCK = 11;
		public static final int INDEX_HOT_PROFILE_ID = 12;
		public static final int INDEX_POWER_CURRENT_SUM_HOT = 13;
		public static final int INDEX_POWER_CURRENT_CNT_HOT = 14;
		public static final int INDEX_CALL_IN_PROGRESS_PROFILE_ID = 15;
		public static final int INDEX_POWER_CURRENT_SUM_CALL = 16;
		public static final int INDEX_POWER_CURRENT_CNT_CALL = 17;

		public static final String[] colNames = new String[] { NAME_ID, NAME_TRIGGER_NAME, NAME_BATTERY_LEVEL, NAME_SCREEN_OFF_PROFILE_ID,
				NAME_BATTERY_PROFILE_ID, NAME_POWER_PROFILE_ID, NAME_POWER_CURRENT_SUM_POW, NAME_POWER_CURRENT_CNT_POW, NAME_POWER_CURRENT_SUM_BAT,
				NAME_POWER_CURRENT_CNT_BAT, NAME_POWER_CURRENT_SUM_LCK, NAME_POWER_CURRENT_CNT_LCK, NAME_HOT_PROFILE_ID, NAME_POWER_CURRENT_SUM_HOT,
				NAME_POWER_CURRENT_CNT_HOT,  NAME_CALL_IN_PROGRESS_PROFILE_ID, NAME_POWER_CURRENT_SUM_CALL, NAME_POWER_CURRENT_CNT_CALL};
		public static final String[] PROJECTION_DEFAULT = colNames;

		static final String[] PROJECTION_MINIMAL_HOT_PROFILE = new String[] { NAME_HOT_PROFILE_ID };

		public static final String SORTORDER_DEFAULT = NAME_BATTERY_LEVEL + " DESC";

		static final String SORTORDER_REVERSE = NAME_BATTERY_LEVEL + " ASC";

		static final String SORTORDER_MINIMAL_HOT_PROFILE = NAME_HOT_PROFILE_ID + " ASC";

	}

	public interface CpuProfile {

		static final String TABLE_NAME = "cpuProfiles";

		public static final String CONTENT_ITEM_NAME = "cpuProfile";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_PROFILE_NAME = "profileName";
		public static final String NAME_GOVERNOR = "governor";
		public static final String NAME_FREQUENCY_MAX = "frequencyMax";
		public static final String NAME_FREQUENCY_MIN = "frequencyMin";
		public static final String NAME_WIFI_STATE = "wifiState";
		public static final String NAME_GPS_STATE = "gpsState";
		public static final String NAME_BLUETOOTH_STATE = "bluetoothState";
		public static final String NAME_MOBILEDATA_STATE = "mobiledataState";
		public static final String NAME_GOVERNOR_THRESHOLD_UP = "governorThresholdUp";
		public static final String NAME_GOVERNOR_THRESHOLD_DOWN = "governorThresholdDown";
		public static final String NAME_BACKGROUND_SYNC_STATE = "backgroundSyncState";
		public static final String NAME_VIRTUAL_GOVERNOR = "virtualGovernor";

		public static final int INDEX_PROFILE_NAME = 1;
		public static final int INDEX_GOVERNOR = 2;
		public static final int INDEX_FREQUENCY_MAX = 3;
		public static final int INDEX_FREQUENCY_MIN = 4;
		public static final int INDEX_WIFI_STATE = 5;
		public static final int INDEX_GPS_STATE = 6;
		public static final int INDEX_BLUETOOTH_STATE = 7;
		public static final int INDEX_MOBILEDATA_STATE = 8;
		public static final int INDEX_GOVERNOR_THRESHOLD_UP = 9;
		public static final int INDEX_GOVERNOR_THRESHOLD_DOWN = 10;
		public static final int INDEX_BACKGROUND_SYNC_STATE = 11;
		public static final int INDEX_VIRTUAL_GOVERNOR = 12;

		public static final String[] colNames = new String[] { NAME_ID, NAME_PROFILE_NAME, NAME_GOVERNOR, NAME_FREQUENCY_MAX,
				NAME_FREQUENCY_MIN, NAME_WIFI_STATE, NAME_GPS_STATE, NAME_BLUETOOTH_STATE, NAME_MOBILEDATA_STATE, NAME_GOVERNOR_THRESHOLD_UP,
				NAME_GOVERNOR_THRESHOLD_DOWN, NAME_BACKGROUND_SYNC_STATE, NAME_VIRTUAL_GOVERNOR };
		public static final String[] PROJECTION_DEFAULT = colNames;
		public static final String[] PROJECTION_PROFILE_NAME = new String[] { NAME_ID, NAME_PROFILE_NAME };

		public static final String SORTORDER_DEFAULT = NAME_FREQUENCY_MAX + " DESC";

		static final String SORTORDER_REVERSE = NAME_PROFILE_NAME + " ASC";

	}

	public interface VirtualGovernor {

		static final String TABLE_NAME = "virtualGovernor";

		public static final String CONTENT_ITEM_NAME = "virtualGovernor";
		public static String CONTENT_URI_STRING = "content://" + CpuTunerProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CpuTunerProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;

		public static final String NAME_VIRTUAL_GOVERNOR_NAME = "virtualGovernor";
		public static final String NAME_REAL_GOVERNOR = "governor";
		public static final String NAME_GOVERNOR_THRESHOLD_UP = "governorThresholdUp";
		public static final String NAME_GOVERNOR_THRESHOLD_DOWN = "governorThresholdDown";

		public static final int INDEX_VIRTUAL_GOVERNOR_NAME = 1;
		public static final int INDEX_REAL_GOVERNOR = 2;
		public static final int INDEX_GOVERNOR_THRESHOLD_UP = 3;
		public static final int INDEX_GOVERNOR_THRESHOLD_DOWN = 4;

		public static final String[] colNames = new String[] { NAME_ID, NAME_VIRTUAL_GOVERNOR_NAME, NAME_REAL_GOVERNOR,
				NAME_GOVERNOR_THRESHOLD_UP, NAME_GOVERNOR_THRESHOLD_DOWN };
		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_VIRTUAL_GOVERNOR_NAME + " DESC";

		static final String SORTORDER_REVERSE = NAME_VIRTUAL_GOVERNOR_NAME + " ASC";

	}
}