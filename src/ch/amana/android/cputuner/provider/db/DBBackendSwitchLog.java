package ch.amana.android.cputuner.provider.db;

import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB.CpuTunerOpenHelper;
import ch.amana.android.cputuner.provider.db.DB.SwitchLogDB;

public class DBBackendSwitchLog {

	private static HashMap<String, String> sSwitchLogProjectionMap;

	private static final int SWITCH_LOG = 1;
	private static final int SWITCH_LOG_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static int delete(CpuTunerOpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case SWITCH_LOG:
			count = db.delete(DB.SwitchLogDB.TABLE_NAME, selection, selectionArgs);
			break;

		case SWITCH_LOG_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.SwitchLogDB.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SWITCH_LOG:
			return DB.SwitchLogDB.CONTENT_TYPE;

		case SWITCH_LOG_ID:
			return DB.SwitchLogDB.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(CpuTunerOpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.SwitchLogDB.TABLE_NAME);
		qb.setProjectionMap(sSwitchLogProjectionMap);
		switch (sUriMatcher.match(uri)) {
		case SWITCH_LOG:
			break;

		case SWITCH_LOG_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.SwitchLogDB.SORTORDER_DEFAULT;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		// SQLiteDatabase db = openHelper.getReadableDatabase();
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		return c;
	}

	public static int update(CpuTunerOpenHelper openHelper, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case SWITCH_LOG:
			count = db.update(DB.SwitchLogDB.TABLE_NAME, values, selection, selectionArgs);
			break;

		case SWITCH_LOG_ID:
			String id = uri.getPathSegments().get(1);
			count = db
					.update(DB.SwitchLogDB.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(CpuTunerOpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != SWITCH_LOG) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.SwitchLogDB.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(SwitchLogDB.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, SwitchLogDB.CONTENT_ITEM_NAME, SWITCH_LOG);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, SwitchLogDB.CONTENT_ITEM_NAME + "/#", SWITCH_LOG_ID);

		sSwitchLogProjectionMap = new HashMap<String, String>();
		for (String col : SwitchLogDB.colNames) {
			sSwitchLogProjectionMap.put(col, col);
		}
	}
}
