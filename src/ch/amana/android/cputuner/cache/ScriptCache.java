package ch.amana.android.cputuner.cache;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.Build;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;

public class ScriptCache extends Cache {

	private FileWriter writer = null;
	private boolean clearCache = true;
	private final Context ctx;

	public ScriptCache(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public void clear() {
		if (!RootHandler.execute("rm -rf " + getPath(ctx).getAbsolutePath() + "/*")) {
			RootHandler.execute("rm " + getPath(ctx).getAbsolutePath() + "/*");
		}
	}

	private File getPath(Context ctx) {
		return ctx.getFilesDir();
	}

	protected File getFile(Context ctx, long pid) {
		File file = new File(getPath(ctx), pid + ".sh");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			file.setExecutable(true);
		}else {
			RootHandler.execute("chmod +x "+file.getAbsolutePath());
		}
		return file;
	}

	@Override
	public boolean execute(long pid) {
		return RootHandler.execute(getFile(ctx, pid).getAbsolutePath());
	}

	@Override
	public boolean exists(long pid) {
		return getFile(ctx, pid).exists();
	}

	@Override
	public void startRecording(long pid) {
		if (clearCache) {
			clear();
			clearCache = false;
		}
		try {
			writer = new FileWriter(getFile(ctx, pid));
		} catch (IOException e) {
			Logger.e("Cannot open FileWriter to script cache for " + pid, e);
			writer = null;
		}
	}

	@Override
	public void endRecording() {
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

	@Override
	public boolean isRecoding() {
		return writer != null;
	}

	@Override
	public void recordLine(String cmd) {
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
