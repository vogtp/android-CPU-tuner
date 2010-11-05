package ch.amana.android.cputuner.hw;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class RootHandler {

	private static final String NEW_LINE = "\n";

	private static boolean isRoot = false;

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
		} catch (IOException e) {
			Log.e(Logger.TAG, "Error while waiting from cmd " + cmd + " to finish", e);
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
}
