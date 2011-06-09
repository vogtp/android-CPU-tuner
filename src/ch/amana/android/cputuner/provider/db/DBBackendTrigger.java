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
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;
import ch.amana.android.cputuner.provider.db.DB.Trigger;

public class DBBackendTrigger {

	private static HashMap<String, String> sTriggerProjectionMap;

	private static final int TRIGGER = 1;
	private static final int TRIGGER_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static int delete(OpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			count = db.delete(DB.Trigger.TABLE_NAME, selection, selectionArgs);
			break;

		case TRIGGER_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.Trigger.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			return DB.Trigger.CONTENT_TYPE;

		case TRIGGER_ID:
			return DB.Trigger.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(OpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.Trigger.TABLE_NAME);
		qb.setProjectionMap(sTriggerProjectionMap);
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			break;

		case TRIGGER_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.Trigger.SORTORDER_DEFAULT;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		// SQLiteDatabase db = openHelper.getReadableDatabase();
		SQLiteDatabase db = openHelper.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		return c;
	}

	public static int update(OpenHelper openHelper, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TRIGGER:
			count = db.update(DB.Trigger.TABLE_NAME, values, selection, selectionArgs);
			break;

		case TRIGGER_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(DB.Trigger.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(OpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TRIGGER) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.Trigger.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(Trigger.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, Trigger.CONTENT_ITEM_NAME, TRIGGER);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, Trigger.CONTENT_ITEM_NAME + "/#", TRIGGER_ID);

		sTriggerProjectionMap = new HashMap<String, String>();
		for (String col : Trigger.colNames) {
			sTriggerProjectionMap.put(col, col);
		}
	}
}
