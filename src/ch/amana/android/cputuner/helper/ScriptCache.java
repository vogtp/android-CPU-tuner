package ch.amana.android.cputuner.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Build;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;

public class ScriptCache {

	private static FileWriter writer = null;

	public static void removeScripts(Context ctx) {
		if (!RootHandler.execute("rm -rf " + getPath(ctx).getAbsolutePath() + "/*")) {
			RootHandler.execute("rm " + getPath(ctx).getAbsolutePath() + "/*");
		}
	}

	private static File getPath(Context ctx) {
		return ctx.getFilesDir();
	}

	protected static File getFile(Context ctx, long pid) {
		File file = new File(getPath(ctx), pid + ".sh");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			file.setExecutable(true);
		}else {
			RootHandler.execute("chmod +x "+file.getAbsolutePath());
		}
		return file;
	}

	public static boolean runScript(Context ctx, long pid) {
		return RootHandler.execute(getFile(ctx, pid).getAbsolutePath());
	}

	public static boolean hasScript(Context ctx, long pid) {
		return getFile(ctx, pid).exists();
	}

	public static void startRecording(Context ctx, long pid) {
		try {
			writer = new FileWriter(getFile(ctx, pid));
		} catch (IOException e) {
			Logger.e("Cannot open FileWriter to script cache for " + pid, e);
			writer = null;
		}
	}

	public static void endRecording() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
				// chmod 4700 *     
			} catch (IOException e) {
				Logger.w("Cannot flush and close script cache writer", e);
			}
		}
		writer = null;
	}

	public static boolean isRecoding() {
		return writer != null;
	}

	public static void recordLine(String cmd) {
		if (writer == null || cmd == null) {
			Logger.w("Writer should not be null when writing to script cache");
		}
		try {
			Logger.w("Adding line to script: " + cmd);
			writer.write(cmd);
			writer.write("\n");
		} catch (IOException e) {
			Logger.w("Cannot write to script cache writer: " + cmd, e);
		}
	}

}
