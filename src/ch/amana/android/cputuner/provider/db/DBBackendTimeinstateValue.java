package ch.amana.android.cputuner.provider.db;

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
import ch.amana.android.cputuner.provider.db.DB.TimeInStateIndex;
import ch.amana.android.cputuner.provider.db.DB.TimeInStateValue;

public class DBBackendTimeinstateValue {

	private static final int TIS = 1;
	private static final int TIS_ID = 2;
	private static final int TIS_G = 3;
	private static final int TIS_ID_G = 4;

	private static final UriMatcher sUriMatcher;

	public static int delete(CpuTunerOpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TIS:
			count = db.delete(DB.TimeInStateValue.TABLE_NAME, selection, selectionArgs);
			break;

		case TIS_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.TimeInStateValue.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case TIS:
			return DB.TimeInStateValue.CONTENT_TYPE;
		case TIS_ID:
			return DB.TimeInStateValue.CONTENT_ITEM_TYPE;
		case TIS_G:
			return DB.TimeInStateValue.CONTENT_TYPE_GROUPED;
		case TIS_ID_G:
			return DB.TimeInStateValue.CONTENT_ITEM_TYPE_GROUPED;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(CpuTunerOpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.TimeInStateValue.TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case TIS:
			break;

		case TIS_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.TimeInStateValue.SORTORDER_DEFAULT;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		return c;
	}

	public static Cursor queryGrouped(CpuTunerOpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// select tisIndex, state, total(TimeInStateValue.time) as time from TimeInStateValue, TimeInStateIndex where (TimeInStateValue.tisIndex=TimeInStateIndex._id) and (TimeInStateIndex.trigger = 'All')  group by state;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TimeInStateValue.TABLE_NAME + ", " + TimeInStateIndex.TABLE_NAME);
		qb.appendWhere("TimeInStateValue.tisIndex=TimeInStateIndex._id");

		switch (sUriMatcher.match(uri)) {
		case TIS_G:
			break;

		case TIS_ID_G:
			qb.appendWhere(DB.TimeInStateIndex.TABLE_NAME + "." + DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.TimeInStateValue.SORTORDER_DEFAULT;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		return qb.query(db, TimeInStateValue.PROJECTION_TIME_SUM, selection, selectionArgs, TimeInStateValue.NAME_STATE, null, orderBy);
	}

	public static int update(CpuTunerOpenHelper openHelper, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case TIS:
			count = db.update(DB.TimeInStateValue.TABLE_NAME, values, selection, selectionArgs);
			break;

		case TIS_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(DB.TimeInStateValue.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(CpuTunerOpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != TIS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.TimeInStateValue.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(TimeInStateValue.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, TimeInStateValue.CONTENT_ITEM_NAME, TIS);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, TimeInStateValue.CONTENT_ITEM_NAME + "/#", TIS_ID);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, TimeInStateValue.CONTENT_ITEM_NAME_GROUPED, TIS_G);
	}
}
