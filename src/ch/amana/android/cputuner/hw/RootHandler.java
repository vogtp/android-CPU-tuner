package ch.amana.android.cputuner.hw;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
			Log.d(Logger.TAG, "Running " + cmd + " as root");
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
				copyStreamToLog("OUT", p.getInputStream());
				copyStreamToLog("ERR", p.getErrorStream());
				if (p.exitValue() != 255) {
					success = true;
					Log.d(Logger.TAG, "Command " + cmd.trim() + " returned " + p.exitValue());
				} else {
					Log.w(Logger.TAG, "Command " + cmd.trim() + " returned " + p.exitValue());
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			line = reader.readLine();
			while (line != null && !line.trim().equals("")) {
				Log.v(Logger.TAG, preAmp + ": " + line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			Log.w(Logger.TAG, "Cannot read process stream", e);
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
		String[] fileList1 = appsRoot.list();
		String[] fileList = appsRoot.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(packageName);
			}
		});
		return fileList;
	}
}
