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
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class DBBackendVirtualGovernor {

	private static HashMap<String, String> sVirtualGovernorProjectionMap;

	private static final int VIRTUAL_GOVERNOR = 1;
	private static final int VIRTUAL_GOVERNOR_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static int delete(OpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case VIRTUAL_GOVERNOR:
			count = db.delete(DB.VirtualGovernor.TABLE_NAME, selection, selectionArgs);
			break;

		case VIRTUAL_GOVERNOR_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.VirtualGovernor.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case VIRTUAL_GOVERNOR:
			return DB.VirtualGovernor.CONTENT_TYPE;

		case VIRTUAL_GOVERNOR_ID:
			return DB.VirtualGovernor.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(OpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.VirtualGovernor.TABLE_NAME);
		qb.setProjectionMap(sVirtualGovernorProjectionMap);
		switch (sUriMatcher.match(uri)) {
		case VIRTUAL_GOVERNOR:
			break;

		case VIRTUAL_GOVERNOR_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.VirtualGovernor.SORTORDER_DEFAULT;
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
		case VIRTUAL_GOVERNOR:
			count = db.update(DB.VirtualGovernor.TABLE_NAME, values, selection, selectionArgs);
			break;

		case VIRTUAL_GOVERNOR_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(DB.VirtualGovernor.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(OpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != VIRTUAL_GOVERNOR) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.VirtualGovernor.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(VirtualGovernor.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, VirtualGovernor.CONTENT_ITEM_NAME, VIRTUAL_GOVERNOR);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, VirtualGovernor.CONTENT_ITEM_NAME + "/#", VIRTUAL_GOVERNOR_ID);

		sVirtualGovernorProjectionMap = new HashMap<String, String>();
		for (String col : VirtualGovernor.colNames) {
			sVirtualGovernorProjectionMap.put(col, col);
		}
	}
}
