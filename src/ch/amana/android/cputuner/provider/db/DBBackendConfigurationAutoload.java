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
import ch.amana.android.cputuner.provider.db.DB.ConfigurationAutoload;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class DBBackendConfigurationAutoload {

	private static HashMap<String, String> sConfigurationAutoloadProjectionMap;

	private static final int CONFIGURATION_AUTOLOAD = 1;
	private static final int CONFIGURATION_AUTOLOAD_ID = 2;

	private static final UriMatcher sUriMatcher;

	public static int delete(OpenHelper openHelper, Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case CONFIGURATION_AUTOLOAD:
			count = db.delete(DB.ConfigurationAutoload.TABLE_NAME, selection, selectionArgs);
			break;

		case CONFIGURATION_AUTOLOAD_ID:
			String id = uri.getPathSegments().get(1);
			count = db.delete(DB.ConfigurationAutoload.TABLE_NAME, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case CONFIGURATION_AUTOLOAD:
			return DB.ConfigurationAutoload.CONTENT_TYPE;

		case CONFIGURATION_AUTOLOAD_ID:
			return DB.ConfigurationAutoload.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	public static Cursor query(OpenHelper openHelper, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		qb.setTables(DB.ConfigurationAutoload.TABLE_NAME);
		qb.setProjectionMap(sConfigurationAutoloadProjectionMap);
		switch (sUriMatcher.match(uri)) {
		case CONFIGURATION_AUTOLOAD:
			break;

		case CONFIGURATION_AUTOLOAD_ID:
			qb.appendWhere(DB.NAME_ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = DB.ConfigurationAutoload.SORTORDER_DEFAULT;
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
		case CONFIGURATION_AUTOLOAD:
			count = db.update(DB.ConfigurationAutoload.TABLE_NAME, values, selection, selectionArgs);
			break;

		case CONFIGURATION_AUTOLOAD_ID:
			String id = uri.getPathSegments().get(1);
			count = db
					.update(DB.ConfigurationAutoload.TABLE_NAME, values, DB.NAME_ID + "=" + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return count;
	}

	public static Uri insert(OpenHelper openHelper, Uri uri, ContentValues initialValues) {
		// Validate the requested uri
		if (sUriMatcher.match(uri) != CONFIGURATION_AUTOLOAD) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = openHelper.getWritableDatabase();
		long rowId = db.insert(DB.ConfigurationAutoload.TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri retUri = ContentUris.withAppendedId(ConfigurationAutoload.CONTENT_URI, rowId);
			return retUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, ConfigurationAutoload.CONTENT_ITEM_NAME, CONFIGURATION_AUTOLOAD);
		sUriMatcher.addURI(CpuTunerProvider.AUTHORITY, ConfigurationAutoload.CONTENT_ITEM_NAME + "/#", CONFIGURATION_AUTOLOAD_ID);

		sConfigurationAutoloadProjectionMap = new HashMap<String, String>();
		for (String col : ConfigurationAutoload.colNames) {
			sConfigurationAutoloadProjectionMap.put(col, col);
		}
	}
}
