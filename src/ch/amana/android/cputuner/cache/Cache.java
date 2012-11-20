package ch.amana.android.cputuner.cache;

import android.content.Context;
import ch.amana.android.cputuner.helper.SettingsStorage;

public abstract class Cache {

	private static Cache instance;

	public static Cache getInstance() {
		if (instance == null) {
			// no way to access script cache in this case
			instance = new CommandCache();
		}
		return instance;
	}

	public static Cache getInstance(Context ctx) {
		if (instance == null) {
			if (SettingsStorage.getInstance(ctx).isUseScriptcache()) {
				instance = new ScriptCache(ctx);
			}
			instance = new CommandCache();
		}
		return instance;
	}

	public static void reset(Context ctx) {
		instance = null;
		getInstance(ctx);
	}

	public abstract void clear();

	public abstract boolean execute(long pid);

	public abstract boolean exists(long pid);

	public abstract void startRecording(long pid);

	public abstract void endRecording();

	public abstract boolean isRecoding();

	public abstract void recordLine(String cmd);


}