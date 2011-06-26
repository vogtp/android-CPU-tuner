package ch.amana.android.cputuner.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;
import ch.amana.android.cputuner.R;

public class Logger {
	private static final String TAG = "CPUTuner";

	public final static boolean DEBUG = false;

	public static final boolean FAKE_MULTICORE = false;

	private static ArrayList<String> log;

	private static Date now;


	public static void addToLog(String msg) {
		int logSize = SettingsStorage.getInstance().getProfileSwitchLogSize();
		if (log == null) {
			log = new ArrayList<String>(logSize);
			now = new Date();
		}
		now.setTime(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		sb.append(SettingsStorage.getInstance().getSimpledateformat().format(now)).append(": ").append(msg);
		log.add(0, sb.toString());
		if (log.size() > logSize) {
			log.remove(logSize);
		}
	}

	public static String getLog(Context context) {
		if (log == null || SettingsStorage.getInstance().getProfileSwitchLogSize() < 1) {
			return context.getString(R.string.not_enabled);
		}
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> profileLogItr = log.iterator(); profileLogItr.hasNext();) {
			String log = profileLogItr.next();
			if (log != null) {
				sb.append(log).append("\n");
			}
		}
		return sb.toString();
	}

	public static void e(String msg, Throwable t) {
		try {
			Log.e(TAG, msg, t);
		}catch(Throwable t1) {
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
}
