package ch.amana.android.cputuner.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Logger {
	private static final String TAG = "CPUTuner";
	private static final String STACKTRACE_TAG = "CPUTunerStracktraceLog";

	public final static boolean DEBUG = false;

	public static void inApp(Context ctx, String msg) {
		if (DEBUG) {
			SwitchLog.addToLog(ctx, msg);
		}
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
			logToFile(msg, new Exception());
			;
		}
	}

	public static void logStacktrace(String msg, Throwable e) {
		if (!Logger.DEBUG) {
			logToFile(msg, new Exception());
		}
	}

	public static void logToFile(String msg, Throwable e) {
		if (Logger.DEBUG) {
			if (e != null) {
				Log.d(STACKTRACE_TAG, msg, e);
			} else {
				Log.d(TAG, msg, e);
			}
			try {
				Writer w = new FileWriter("/mnt/sdcard/cputuner.log", true);
				w.write("**************  Stacktrace ***********************\n");
				w.write((new Date()).toString());
				w.write("\n");
				w.write(msg);
				if (e != null) {
					w.write("\n");
					e.printStackTrace(new PrintWriter(w));
				}
				w.write("**************************************************\n");
				w.flush();
				w.close();
			} catch (IOException e1) {
				Logger.w("Cannot write stacktrage log", e1);
			}
		}
	}

	public static void logIntentExtras(Intent intent) {
		if (DEBUG) {
			try {
				if (intent == null || intent.getExtras() == null) {
					return;
				}
				Bundle extras = intent.getExtras();
				String action = intent.getAction();
				StringBuilder sb = new StringBuilder();
				sb.append("action: ").append(action);
				for (String key : extras.keySet()) {
					sb.append(" extra: ").append( key).append( " -> ");
					sb.append(extras.get(key)).append("\n");
				}
				logToFile(sb.toString(),null);

			} catch (Exception e) {
			}
		}
	}
}
