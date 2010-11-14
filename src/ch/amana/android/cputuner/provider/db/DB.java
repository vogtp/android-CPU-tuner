package ch.amana.android.cputuner.provider.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.provider.CpuTunerProvider;

public interface DB {

	public static final String DATABASE_NAME = "cputuner";

	public static final String NAME_ID = "_id";
	public static final int INDEX_ID = 0;

	public class OpenHelper extends SQLiteOpenHelper {

		private static final int DATABASE_VERSION = 1;

		private static final String CREATE_TRIGGERS_TABLE = "create table if not exists " + Trigger.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
				+ DB.Trigger.NAME_TRIGGER_NAME + " text, " + DB.Trigger.NAME_BATTERY_LEVEL + " int," + DB.Trigger.NAME_SCREEN_OFF_PROFILE_ID + " long,"
				+ DB.Trigger.NAME_BATTERY_PROFILE_ID + " long," + DB.Trigger.NAME_POWER_PROFILE_ID + " long)";

		private static final String CREATE_CPUPROFILES_TABLE = "create table if not exists " + CpuProfile.TABLE_NAME + " (" + DB.NAME_ID
				+ " integer primary key, "
				+ DB.CpuProfile.NAME_PROFILE_NAME + " text, " + DB.CpuProfile.NAME_GOVERNOR + " text," + DB.CpuProfile.NAME_FREQUENCY_MAX + " int,"
				+ DB.CpuProfile.NAME_FREQUENCY_MIN + " int)";

		private static final String LOG_TAG = Logger.TAG;

		public OpenHelper(Context context) {
			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TRIGGERS_TABLE);
			db.execSQL(CREATE_CPUPROFILES_TABLE);
			db.execSQL("create index idx_trigger_battery_level on " + Trigger.TABLE_NAME + " (" + Trigger.NAME_BATTERY_LEVEL + "); ");
			db.execSQL("create index idx_cpuprofiles_profilename on " + CpuProfile.TABLE_NAME + " (" + CpuProfile.NAME_PROFILE_NAME + "); ");
			Log.i(LOG_TAG, "Created tables ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 1:
				Log.w(LOG_TAG, "Upgrading to DB Version 2...");
				throw new Error("not implemented");
				// nobreak

			default:
				Log.w(LOG_TAG, "Finished DB upgrading!");
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

		public static final int INDEX_TRIGGER_NAME = 1;
		public static final int INDEX_BATTERY_LEVEL = 2;
		public static final int INDEX_SCREEN_OFF_PROFILE_ID = 3;
		public static final int INDEX_BATTERY_PROFILE_ID = 4;
		public static final int INDEX_POWER_PROFILE_ID = 5;

		public static final String[] colNames = new String[] { NAME_ID, NAME_TRIGGER_NAME, NAME_BATTERY_LEVEL, NAME_SCREEN_OFF_PROFILE_ID,
				NAME_BATTERY_PROFILE_ID,
				NAME_POWER_PROFILE_ID };
		public static final String[] PROJECTION_DEFAULT = colNames;

		public static final String SORTORDER_DEFAULT = NAME_BATTERY_LEVEL + " DESC";

		static final String SORTORDER_REVERSE = NAME_BATTERY_LEVEL + " ASC";

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

		public static final int INDEX_PROFILE_NAME = 1;
		public static final int INDEX_GOVERNOR = 2;
		public static final int INDEX_FREQUENCY_MAX = 3;
		public static final int INDEX_FREQUENCY_MIN = 4;

		public static final String[] colNames = new String[] { NAME_ID, NAME_PROFILE_NAME, NAME_GOVERNOR, NAME_FREQUENCY_MAX,
				NAME_FREQUENCY_MIN };
		public static final String[] PROJECTION_DEFAULT = colNames;
		public static final String[] PROJECTION_PROFILE_NAME = new String[] { NAME_ID, NAME_PROFILE_NAME };

		public static final String SORTORDER_DEFAULT = NAME_FREQUENCY_MAX + " DESC";

		static final String SORTORDER_REVERSE = NAME_PROFILE_NAME + " ASC";

	}
}