package ch.amana.android.cputuner.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public interface IAccess {

	public abstract int delete(Uri uri, String selection, String[] selectionArgs);

	public abstract Uri insert(Uri uri, ContentValues initialValues);

	public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

	public abstract Cursor query(String string);

}