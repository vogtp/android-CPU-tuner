package ch.amana.android.cputuner.helper;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class CpuTunerBackupAgent extends BackupAgentHelper {

	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, getDefaultPrefsName());
		addHelper(getDefaultPrefsName(), helper);
	}

	private String getDefaultPrefsName() {
		return this.getPackageName() + "_preferences";
	}

}
