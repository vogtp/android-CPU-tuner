package ch.amana.android.cputuner.helper;

import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

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
				if (p.exitValue() != 255) {
					success = true;
				}
			} catch (InterruptedException e) {
				Log.e(Logger.TAG, "Interrupt while waiting from cmd " + cmd + " to finish", e);
			}
		} catch (IOException e) {
			Log.e(Logger.TAG, "Error while waiting from cmd " + cmd + " to finish", e);
		}
		return success;
	}

	public static boolean isRoot() {
		if (!isRoot) {
			isRoot = execute("ls /");
		}
		return isRoot;
	}
}
