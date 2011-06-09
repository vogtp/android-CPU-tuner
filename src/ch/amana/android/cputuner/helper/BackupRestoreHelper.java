package ch.amana.android.cputuner.helper;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import ch.almana.android.importexportdb.ExportDataTask;
import ch.almana.android.importexportdb.importer.DataJsonImporter;
import ch.almana.android.importexportdb.importer.JSONBundle;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class BackupRestoreHelper {

	public static final String DIRECTORY_CONFIGURATIONS = "configurations";

	public static void backup(Context ctx, File storagePath) {
		if (!storagePath.isDirectory()) {
			storagePath.mkdir();
		}
		SQLiteDatabase db = new OpenHelper(ctx).getWritableDatabase();
		ExportDataTask exportDataTask = new ExportDataTask(ctx, db, storagePath, ExportDataTask.ExportType.JSON);
		exportDataTask.execute(new String[] { DB.DATABASE_NAME });
		db.close();
	}

	public static File getStoragePath(Context ctx, String directory) {
		return new File(Environment.getExternalStorageDirectory(), ctx.getPackageName() + "/" + directory);
	}

	public static void restore(Context ctx, File storagePath, boolean inclAutoloadConfig) throws Exception {
		CpuTunerProvider.deleteAllTables(ctx, inclAutoloadConfig);
		ContentResolver contentResolver = ctx.getContentResolver();
		DataJsonImporter dje = new DataJsonImporter(DB.DATABASE_NAME, storagePath);
		try {
			loadVirtualGovernors(contentResolver, dje);
			loadCpuProfiles(contentResolver, dje);
			loadTriggers(contentResolver, dje);
			if (inclAutoloadConfig) {
				loadAutoloadConfig(contentResolver, dje);
			}
		} catch (JSONException e) {
			Logger.e("Cannot restore tables", e);
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

	public static void backupConfiguration(Context ctx, String name) {
		backup(ctx, new File(BackupRestoreHelper.getStoragePath(ctx, DIRECTORY_CONFIGURATIONS), name));
	}

	public static void restoreConfiguration(Context ctx, String name, boolean inclAutoloadConfig) throws Exception {
		restore(ctx, new File(BackupRestoreHelper.getStoragePath(ctx, DIRECTORY_CONFIGURATIONS), name), inclAutoloadConfig);
	}

}
