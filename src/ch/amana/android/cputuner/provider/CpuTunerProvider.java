package ch.amana.android.cputuner.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.DB.OpenHelper;

public class CpuTunerProvider extends ContentProvider {

	public static class UriTableMapping {

		public final Uri contenUri;
		public final String tableName;
		public final String contentItemName;
		public final String contentType;
		public final String contentItemType;
		public boolean notifyOnChange;
		public final String specialWhere;
		public String groupBy;
		public final String idTableName;
		public boolean distinct;

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange) {
			this(contenUri, tableName, contentItemName, contentType, contentItemType, notifyOnChange, null, null, "", false);
		}

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange, boolean distinct) {
			this(contenUri, tableName, contentItemName, contentType, contentItemType, notifyOnChange, null, null, "", distinct);
		}

		public UriTableMapping(Uri contenUri, String tableName, String contentItemName, String contentType, String contentItemType, boolean notifyOnChange, String specialWhere,
				String groupBy, String idTableName, boolean distinct) {
			super();
			this.contenUri = contenUri;
			this.tableName = tableName;
			this.contentItemName = contentItemName;
			this.contentType = contentType;
			this.contentItemType = contentItemType;
			this.notifyOnChange = notifyOnChange;
			this.specialWhere = specialWhere;
			this.groupBy = groupBy;
			this.idTableName = idTableName;
			this.distinct = distinct;
		}
	}


	private static final int CONTENT = 1;
	private static final int CONTENT_ITEM = 2;

	private static final UriMatcher uriTableMatcher;

	private static UriMatcher uriContentTypeMatcher;
	private static boolean notifyChanges = true;

	private OpenHelper openHelper;

	@Override
	public boolean onCreate() {
		openHelper = new OpenHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		UriTableMapping utm = getUriTableMap(uri);
		SQLiteDatabase db = openHelper.getWritableDatabase();
		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			count = db.delete(utm.tableName, selection, selectionArgs);
			break;

		case CONTENT_ITEM:
			String id = uri.getPathSegments().get(1);
			count = db.delete(utm.tableName, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		notifyChange(uri);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		Uri ret;
		// Validate the requested uri
		if (uriContentTypeMatcher.match(uri) != CONTENT) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		UriTableMapping utm = getUriTableMap(uri);
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(utm.tableName, DB.NAME_ID, values);
		if (rowId > 0) {
			ret = ContentUris.withAppendedId(uri, rowId);
		} else {
			throw new SQLException("Failed to insert row into " + uri);
		}

		notifyChange(uri);
		return ret;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		UriTableMapping utm = getUriTableMap(uri);
		qb.setTables(utm.tableName);

		if (utm.specialWhere != null) {
			qb.appendWhere(utm.specialWhere);
		}

		int match = uriContentTypeMatcher.match(uri);
		switch (match) {
		case CONTENT:
			break;

		case CONTENT_ITEM:
			qb.appendWhere(utm.idTableName + DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		qb.setDistinct(utm.distinct);

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, utm.groupBy, null, sortOrder);
		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		UriTableMapping utm = getUriTableMap(uri);
		SQLiteDatabase db = openHelper.getWritableDatabase();
		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			count = db.update(utm.tableName, values, selection, selectionArgs);
			break;

		case CONTENT_ITEM:
			String id = uri.getPathSegments().get(1);
			count = db.update(utm.tableName, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		notifyChange(uri);
		return count;
	}

	private void notifyChange(Uri uri) {
		if (notifyChanges && SettingsStorage.getInstance().isEnableCpuTuner() && getUriTableMap(uri).notifyOnChange) {
			PowerProfiles.getInstance(getContext()).reapplyProfile(true);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		// BackupRestoreHelper.backup(getContext()); 
	}

	public static void setNotifyChanges(boolean b) {
		notifyChanges = b;
	}

	@Override
	public String getType(Uri uri) {
		UriTableMapping uriTableMapping = getUriTableMap(uri);
		if (uriTableMapping == null) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (uriContentTypeMatcher.match(uri)) {
		case CONTENT:
			return uriTableMapping.contentType;
		case CONTENT_ITEM:
			return uriTableMapping.contentItemType;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	private UriTableMapping getUriTableMap(Uri uri) {
		int match = uriTableMatcher.match(uri);
		UriTableMapping[] map = DB.UriTableConfig.map; //content://ch.amana.android.cputuner/TimeInStateIndex_DISTINCT
		if (match < 0 || match > map.length - 1) {
			ArrayIndexOutOfBoundsException e = new ArrayIndexOutOfBoundsException(match);
			Logger.e("No uri table machting: ", e);
			throw e;
		}
		return DB.UriTableConfig.map[match];
	}

	public static void deleteAllTables(Context ctx, boolean deleteAutoloadConfig) {
		ContentResolver resolver = ctx.getContentResolver();
		resolver.delete(DB.Trigger.CONTENT_URI, null, null);
		resolver.delete(DB.CpuProfile.CONTENT_URI, null, null);
		resolver.delete(DB.VirtualGovernor.CONTENT_URI, null, null);
		resolver.delete(DB.TimeInStateIndex.CONTENT_URI, null, null);
		resolver.delete(DB.TimeInStateValue.CONTENT_URI, null, null);
		if (deleteAutoloadConfig) {
			resolver.delete(DB.ConfigurationAutoload.CONTENT_URI, null, null);
		}
	}

	static {

		uriTableMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriContentTypeMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		for (int type = 0; type < DB.UriTableConfig.map.length; type++) {
			String contentItemName = DB.UriTableConfig.map[type].contentItemName;
			uriTableMatcher.addURI(DB.AUTHORITY, contentItemName, type);
			uriTableMatcher.addURI(DB.AUTHORITY, contentItemName + "/#", type);
			uriContentTypeMatcher.addURI(DB.AUTHORITY, contentItemName, CONTENT);
			uriContentTypeMatcher.addURI(DB.AUTHORITY, contentItemName + "/#", CONTENT_ITEM);
		}

	}

}
