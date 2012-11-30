package ch.amana.android.cputuner.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import ch.almana.android.db.backend.DBProviderBase;
import ch.almana.android.db.backend.UriTableMapping;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;


public class DBProvider extends DBProviderBase {

	@Override
	protected void notifyChange(Uri uri) {
		super.notifyChange(uri);
		if (isNotifyChanges() && SettingsStorage.getInstance().isEnableCpuTuner() && getUriTableMap(uri).notifyOnChange) {
			PowerProfiles.getInstance(getContext()).reapplyProfile(true);
		}
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

	@Override
	protected SQLiteOpenHelper getOpenHelper() {
		return new DB.OpenHelper(getContext());
	}

	@Override
	protected UriTableMapping[] getUriTableMapping() {
		return DB.UriTableConfig.getUriTableMapping();
	}

	@Override
	protected String getAuthority() {
		return DB.AUTHORITY;
	}
}
