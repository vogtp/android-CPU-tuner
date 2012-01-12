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
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.provider.db.DB.CpuTunerOpenHelper;

public class DBBackendCpuProfile {

	private static final int CPU_PROFILE = 1;
	private static final int CPU_PROFILE_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static int delete(CpuTunerOpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case CPU_PROFILE:
			count = db.delete(DB.CpuProfile.TABLE_NAME, selection, selectionArgs);
			break;

		case CPU_PROFILE_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.CpuProfile.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case CPU_PROFILE:
			return DB.CpuProfile.CONTENT_TYPE;

		case CPU_PROFILE_ID:
			return DB.CpuProfile.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(CpuTunerOpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.CpuProfile.TABLE_NAME);
		switch (sUriMatcher.match(uri)) {
		case CPU_PROFILE:
			break;

		case CPU_PROFILE_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.CpuProfile.SORTORDER_DEFAULT;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		return c;
	}

	public static int update(CpuTunerOpenHelper openHelper, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case CPU_PROFILE:
			count = db.update(DB.CpuProfile.TABLE_NAME, values, selection, selectionArgs);
			break;

		case CPU_PROFILE_ID:
			String id = uri.getPathSegments().get(1);
			count = db.update(DB.CpuProfile.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(CpuTunerOpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != CPU_PROFILE) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.CpuProfile.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(CpuProfile.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, CpuProfile.CONTENT_ITEM_NAME, CPU_PROFILE);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, CpuProfile.CONTENT_ITEM_NAME + "/#", CPU_PROFILE_ID);
	}
}
