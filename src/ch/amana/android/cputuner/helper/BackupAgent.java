package ch.amana.android.cputuner.helper;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupAgent extends BackupAgentHelper {

	static final String PREFS_BACKUP_KEY = "cpuTunerPrefs";

	// Allocate a helper and add it to the backup agent
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, getDefaultPrefsName());
		addHelper(PREFS_BACKUP_KEY, helper);

	}

	private String getDefaultPrefsName() {
		return this.getPackageName() + "_preferences";
	}

}
