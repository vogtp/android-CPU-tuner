package ch.amana.android.cputuner.hw;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class RootHandler {

	private static final String NEW_LINE = "\n";

	private static boolean isRoot = false;

	private static boolean checkedSystemApp = false;
	private static boolean isSystemApp = false;

	public static boolean execute(String cmd) {
		Process p;
		boolean success = false;
		try {
			Log.v(Logger.TAG, "Running " + cmd);
			p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());

			if (!cmd.endsWith(NEW_LINE)) {
				cmd += NEW_LINE;
			}
			os.writeBytes(cmd);

			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();
				String msg = "Command >" + cmd.trim() + "< returned " + p.exitValue();
				writeLog(msg);
				Log.i(Logger.TAG, msg);
				copyStreamToLog("OUT", p.getInputStream());
				copyStreamToLog("ERR", p.getErrorStream());
				if (p.exitValue() != 255) {
					success = true;
				}
			} catch (InterruptedException e) {
				Log.e(Logger.TAG, "Interrupt while waiting from cmd " + cmd + " to finish", e);
			}
		} catch (FileNotFoundException e) {
			Log.e(Logger.TAG, "File not found in " + cmd);
		} catch (IOException e) {
			Log.e(Logger.TAG, "IO error from: " + cmd, e);
		}
		return success;
	}

	private static void copyStreamToLog(String preAmp, InputStream in) {
		if (in == null) {
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			line = reader.readLine();
			while (line != null && !line.trim().equals("")) {
				Log.v(Logger.TAG, preAmp + ": " + line);
				writeLog(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			Log.w(Logger.TAG, "Cannot read process stream", e);
		}
	}

	private static void writeLog(String line) throws IOException {
		if (logWriter != null) {
			logWriter.write(line);
			logWriter.write("\n");
		}
	}

	public static boolean isRoot() {
		if (!isRoot) {
			isRoot = execute("ls -ld /");
		}
		return isRoot;
	}

	public static boolean isSystemApp(Context ctx) {
		if (!checkedSystemApp) {
			String[] fileList = findAppPath(ctx, Environment.getRootDirectory());
			isSystemApp = fileList != null && fileList.length > 0;
			checkedSystemApp = true;
		}
		Log.i(Logger.TAG, "Is system app: " + isSystemApp);
		return isSystemApp;
	}

	public static String[] findAppPath(Context ctx, File root) {
		if (!root.isDirectory()) {
			return new String[] {};
		}
		File appsRoot = new File(root, "app");
		final String packageName = ctx.getPackageName();
		String[] fileList = appsRoot.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(packageName);
			}
		});
		return fileList;
	}

	static String readFile(String directory, String filename) {
		synchronized (filename) {
			String val = "";
			BufferedReader reader;
			try {
				Log.v(Logger.TAG, "Reading file >" + filename + "<");
				writeLog("Reading file >" + filename + "<");
				reader = new BufferedReader(new FileReader(directory + filename));
				String line = reader.readLine();
				while (line != null && !line.trim().equals("")) {
					Log.v(Logger.TAG, "Read line >" + line + "<");
					writeLog(line);
					val += line;
					line = reader.readLine();
				}
				reader.close();
			} catch (Throwable e) {
				Log.e(Logger.TAG, "Cannot open for reading " + filename, e);
			}
			if (val.trim().equals("")) {
				val = NOT_AVAILABLE;
			}
			return val;
		}
	}

	public static final String NOT_AVAILABLE = "not available";

	private static Writer logWriter;

	public static void setLogLocation(File file) {
		if (file == null) {
			logWriter = null;
			return;
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Log.w(Logger.TAG, "Cannot creat log file " + file.toString(), e);
				logWriter = null;
				return;
			}
		}
		if (file.isFile() && file.canWrite()) {
			Log.i(Logger.TAG, "Opening logfile " + file.getAbsolutePath());
			try {
				logWriter = new FileWriter(file);
			} catch (IOException e) {
				Log.w(Logger.TAG, "Cannot open logfile", e);
			}
		} else {
			logWriter = null;
		}
	}

	public static void clearLogLocation() {
		if (logWriter != null) {
			try {
				logWriter.flush();
				logWriter.close();
			} catch (IOException e) {
				Log.w(Logger.TAG, "Cannot close logfile", e);
			}
		}
		logWriter = null;
	}
}
