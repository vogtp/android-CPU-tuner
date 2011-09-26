package ch.amana.android.cputuner.helper;

import java.io.File;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.almana.android.importexportdb.ExportDataTask;
import ch.almana.android.importexportdb.constants.JsonConstants;
import ch.almana.android.importexportdb.importer.DataJsonImporter;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class BackupRestoreHelper {

	public static final String DIRECTORY_CONFIGURATIONS = "configurations";

	public static void backup(BackupRestoreCallback cb, File storagePath) {
		if (!storagePath.isDirectory()) {
			storagePath.mkdir();
		}
		// ModelAccess.getInstace(cb.getContext()).applyDelayedTriggerUpdates();
		SQLiteDatabase db = new OpenHelper(cb.getContext()).getWritableDatabase();
		ExportDataTask exportDataTask = new ExportDataTask(cb, db, storagePath, ExportDataTask.ExportType.JSON);
		exportDataTask.execute(new String[] { DB.DATABASE_NAME });
	}

	public static File getStoragePath(Context ctx, String directory) {
		return new File(Environment.getExternalStorageDirectory(), ctx.getPackageName() + "/" + directory);
	}

	private static void restore(BackupRestoreCallback cb, DataJsonImporter dje, boolean inclAutoloadConfig) throws Exception {
		Context ctx = cb.getContext();
		CpuTunerProvider.deleteAllTables(ctx, inclAutoloadConfig);
		ContentResolver contentResolver = ctx.getContentResolver();
		try {
			synchronized (ModelAccess.virtgovCacheMutex) {
				synchronized (ModelAccess.profileCacheMutex) {
					synchronized (ModelAccess.triggerCacheMutex) {
						loadVirtualGovernors(contentResolver, dje);
						loadCpuProfiles(contentResolver, dje);
						loadTriggers(contentResolver, dje);
						if (inclAutoloadConfig) {
							loadAutoloadConfig(contentResolver, dje);
						}
					}
				}
			}
			cb.hasFinished(true);
			ModelAccess.getInstace(cb.getContext()).clearCache();
		} catch (JSONException e) {
			Logger.e("Cannot restore tables", e);
			cb.hasFinished(false);
			throw new Exception("Error restoring", e);
		}
	}

	private static void loadVirtualGovernors(ContentResolver contentResolver, DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.VirtualGovernor.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			VirtualGovernorModel vgm = new VirtualGovernorModel();
			vgm.readFromJson(new JSONBundle(table.getJSONObject(i)));
			contentResolver.insert(DB.VirtualGovernor.CONTENT_URI, vgm.getValues());
		}
	}

	private static void loadCpuProfiles(ContentResolver contentResolver, DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.CpuProfile.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			ProfileModel pm = new ProfileModel();
			pm.readFromJson(new JSONBundle(table.getJSONObject(i)));
			contentResolver.insert(DB.CpuProfile.CONTENT_URI, pm.getValues());
		}
	}

	private static void loadTriggers(ContentResolver contentResolver, DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.Trigger.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			TriggerModel tr = new TriggerModel();
			tr.readFromJson(new JSONBundle(table.getJSONObject(i)));
			contentResolver.insert(DB.Trigger.CONTENT_URI, tr.getValues());
		}
	}

	private static void loadAutoloadConfig(ContentResolver contentResolver, DataJsonImporter dje) throws JSONException {
		JSONArray table = dje.getTables(DB.ConfigurationAutoload.TABLE_NAME);
		for (int i = 0; i < table.length(); i++) {
			ConfigurationAutoloadModel cam = new ConfigurationAutoloadModel();
			cam.readFromJson(new JSONBundle(table.getJSONObject(i)));
			contentResolver.insert(DB.ConfigurationAutoload.CONTENT_URI, cam.getValues());
		}
	}

	public static void backupConfiguration(BackupRestoreCallback cb, String name) {
		if (name == null) {
			return;
		}
		File storagePath = new File(BackupRestoreHelper.getStoragePath(cb.getContext(), DIRECTORY_CONFIGURATIONS), name);
		if (!storagePath.isDirectory()) {
			storagePath.mkdirs();
		}
		backup(cb, storagePath);
	}

	public static void restoreConfiguration(BackupRestoreCallback cb, String name, boolean isUserConfig) throws Exception {
		if (name == null) {
			return;
		}
		if (isUserConfig) {
			File file = new File(BackupRestoreHelper.getStoragePath(cb.getContext(), DIRECTORY_CONFIGURATIONS), name);
			DataJsonImporter dje = new DataJsonImporter(DB.DATABASE_NAME, file);
			restore(cb, dje, !isUserConfig);
		} else {
			String fileName = DIRECTORY_CONFIGURATIONS + "/" + name + "/" + DB.DATABASE_NAME + JsonConstants.FILE_NAME;
			InputStream is = cb.getContext().getAssets().open(fileName);
			//			getconfig 
			DataJsonImporter dje = new DataJsonImporter(DB.DATABASE_NAME, is);
			restore(cb, dje, !isUserConfig);
			//			fix frequencies
			//			fix governors
		}
		PowerProfiles.getInstance().reapplyProfile(true);
	}

	public static void saveConfiguration(final Context ctx) {
		SettingsStorage settings = SettingsStorage.getInstance();
		if (settings.isSaveConfiguration()) {
			BackupRestoreCallback callback = new BackupRestoreCallback() {
				@Override
				public void hasFinished(boolean success) {
				}

				@Override
				public Context getContext() {
					return ctx;
				}
			};
			BackupRestoreHelper.backupConfiguration(callback, settings.getCurrentConfiguration());
		}
	}

}
