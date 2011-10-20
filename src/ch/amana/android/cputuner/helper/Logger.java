package ch.amana.android.cputuner.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import ch.amana.android.cputuner.R;

public class Logger {
	private static final String TAG = "CPUTuner";

	public final static boolean DEBUG = true;

	private static ArrayList<String> log;

	private static Date now;

	private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss");
	public static void addToLog(String msg) {
		int logSize = SettingsStorage.getInstance().getProfileSwitchLogSize();
		if (log == null) {
			log = new ArrayList<String>(logSize);
			now = new Date();
		}
		now.setTime(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		sb.append(logDateFormat.format(now)).append(": ").append(msg);
		log.add(0, sb.toString());
		if (log.size() > logSize) {
			log.remove(logSize);
		}
	}

	public static String getLog(Context context) {
		if (!SettingsStorage.getInstance().isEnableLogProfileSwitches()) {
			return context.getString(R.string.not_enabled);
		}
		StringBuilder sb = new StringBuilder();
		if (log != null) {
			for (Iterator<String> profileLogItr = log.iterator(); profileLogItr.hasNext();) {
				String log = profileLogItr.next();
				if (log != null) {
					sb.append(log).append("\n");
				}
			}
		}
		if (sb.length() < 2) {
			sb.append(context.getString(R.string.msg_no_profile_switch_log));
		}
		return sb.toString();
	}

	public static void e(String msg, Throwable t) {
		try {
			Log.e(TAG, msg, t);
		} catch (Throwable t1) {
		}
	}

	public static void w(String msg, Throwable t) {
		Log.w(TAG, msg);
	}

	public static void d(String msg, Throwable t) {
		Log.d(TAG, msg);
	}

	public static void v(String msg, Throwable t) {
		Log.v(TAG, msg);
	}

	public static void i(String msg, Throwable t) {
		Log.i(TAG, msg);
	}

	public static void e(String msg) {
		Log.e(TAG, msg);
	}

	public static void w(String msg) {
		Log.w(TAG, msg);
	}

	public static void d(String msg) {
		Log.d(TAG, msg);
	}

	public static void v(String msg) {
		Log.v(TAG, msg);
	}

	public static void i(String msg) {
		Log.i(TAG, msg);
	}

	public static void logStacktrace(String msg) {
		if (!Logger.DEBUG) {
			return;
		}
		logStacktrace(msg, new Exception());
	}

	public static void logStacktrace(String msg, Throwable e) {
		if (!Logger.DEBUG) {
			return;
		}
		Log.w(TAG, "Stacktrace logger: " + msg, e);
		try {
			Writer w = new FileWriter("/mnt/sdcard/cputuner.log", true);
			w.write("**************  Stacktrace ***********************\n");
			w.write((new Date()).toString());
			w.write("\n");
			w.write(msg);
			w.write("\n");
			e.printStackTrace(new PrintWriter(w));
			w.write("**************************************************\n");
			w.flush();
			w.close();
		} catch (IOException e1) {
			Logger.w("Cannot write delete log", e1);
		}
	}

}
