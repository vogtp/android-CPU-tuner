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
import ch.amana.android.cputuner.helper.Logger;

public class RootHandler {

	private static final String NEW_LINE = "\n";

	private static boolean isRoot = false;

	private static boolean checkedSystemApp = false;
	private static boolean isSystemApp = false;

	public static boolean execute(String cmd) {
		return execute(cmd, null);
	}

	public static boolean execute(String cmd, StringBuilder result) {
		Process p;
		boolean success = false;
		try {
			Logger.v("Running " + cmd);
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
				Logger.i(msg);
				copyStreamToLog("OUT", p.getInputStream(), result);
				copyStreamToLog("ERR", p.getErrorStream(), result);
				if (p.exitValue() != 255) {
					success = true;
				}
			} catch (InterruptedException e) {
				Logger.e("Interrupt while waiting from cmd " + cmd + " to finish", e);
			}
		} catch (FileNotFoundException e) {
			Logger.e("File not found in " + cmd);
		} catch (IOException e) {
			Logger.e("IO error from: " + cmd, e);
		}
		return success;
	}

	private static void copyStreamToLog(String preAmp, InputStream in, StringBuilder result) {
		if (in == null) {
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			line = reader.readLine();
			while (line != null && !line.trim().equals("")) {
				Logger.v(preAmp + ": " + line);
				writeLog(line);
				if (result != null) {
					result.append(line);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			Logger.w("Cannot read process stream", e);
		}
	}

	public static void writeLog(String line) {
		if (logWriter != null) {
			try {
				logWriter.write(line);
				logWriter.write("\n");
				logWriter.flush();
			} catch (IOException e) {
				Logger.w("Cannot write >" + line + "< to log file", e);
			}
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
			Logger.i("Is system app: " + isSystemApp);
		}
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
				String fqfn = directory + "/" + filename;
				Logger.v("Reading file >" + fqfn + "<");
				writeLog("Reading file >" + filename + "<");
				reader = new BufferedReader(new FileReader(fqfn));
				String line = reader.readLine();
				while (line != null && !line.trim().equals("")) {
					Logger.v("Read line >" + line + "<");
					writeLog(line);
					val += line;
					line = reader.readLine();
				}
				reader.close();
			} catch (Throwable e) {
				Logger.e("Cannot open for reading " + filename, e);
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
				Logger.w("Cannot creat log file " + file.toString(), e);
				logWriter = null;
				return;
			}
		}
		if (file.isFile() && file.canWrite()) {
			Logger.i("Opening logfile " + file.getAbsolutePath());
			try {
				logWriter = new FileWriter(file);
			} catch (IOException e) {
				Logger.w("Cannot open logfile", e);
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
				Logger.w("Cannot close logfile", e);
			}
		}
		logWriter = null;
	}
}
