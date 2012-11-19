package ch.amana.android.cputuner.cache;

import android.content.Context;

public abstract class Cache {

	private static Cache instance;

	public static Cache getInstance() {
		if (instance == null) {
			if (true) {
				instance = new CommandCache();
			}
			instance = new ScriptCache();
		}
		return instance;
	}

	public abstract void clear(Context ctx);

	public abstract boolean execute(Context ctx, long pid);

	public abstract boolean exists(Context ctx, long pid);

	public abstract void startRecording(Context ctx, long pid);

	public abstract void endRecording();

	public abstract boolean isRecoding();

	public abstract void recordLine(String cmd);

}