package ch.amana.android.cputuner.log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class SwitchLog extends BroadcastReceiver {
	private static SwitchLog instance;

	public static final String ACTION_ADD_TO_LOG = "ch.amana.android.cputuner.addToSwitchLog";
	public static final String EXTRA_LOG_ENTRY = "EXTRA_LOG_ENTRY";

	private ArrayList<String> log;

	private Date now;

	private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss");


	public static void start(Context ctx) {
		if (instance == null) {
			instance = new SwitchLog(ctx);
		}
		ctx.registerReceiver(instance, new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(ACTION_ADD_TO_LOG));
	}

	public static void stop(Context ctx) {
		try {
			ctx.unregisterReceiver(instance);
		} catch (Throwable e) {
		}
		instance = null;
	}

	public SwitchLog(Context ctx) {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!SettingsStorage.getInstance().isEnableLogProfileSwitches()) {
			return;
		}
		if (intent == null) {
			return;
		}
		String logEntry = intent.getStringExtra(EXTRA_LOG_ENTRY);
		if (logEntry != null) {
			addToSwitchLog(logEntry);
		}
	}

	private void addToSwitchLog(String msg) {
		int logSize = SettingsStorage.getInstance().getProfileSwitchLogSize();
		if (log == null) {
			log = new ArrayList<String>(logSize);
			now = new Date();
		}
		now.setTime(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		sb.append(logDateFormat.format(now)).append(": ").append(msg);
		log.add(0, sb.toString());
		for (int i = logSize - 1; i < log.size(); i++) {
			log.remove(i);
		}
	}

	public static void clearSwitchLog() {
		if (instance != null) {
			instance.log.clear();
		}
	}

	public static String getLog(Context context) {
		if (instance != null) {
			return instance.getLogInternal(context);
		}
		return context.getString(R.string.msg_no_profile_switch_log);
	}

	private String getLogInternal(Context context) {
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

}
