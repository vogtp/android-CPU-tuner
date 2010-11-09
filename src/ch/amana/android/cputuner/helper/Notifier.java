package ch.amana.android.cputuner.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.view.activity.CpuTunerTabActivity;

public class Notifier {

	private static final int NOTIFICATION_PROFILE = 1;
	private final NotificationManager notificationManager;
	// private Notification notification;
	private final Context context;
	private String contentTitle;
	private PendingIntent contentIntent;

	private static int curLevel = 1;

	private static Notifier instance;

	public static void startStatusbarNotifications(Context ctx) {
		if (instance == null) {
			instance = new Notifier(ctx);
		}
		instance.notifyStatus(PowerProfiles.getCurrentProfile());
	}

	public Notifier(final Context ctx) {
		super();
		this.context = ctx;
		String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) ctx.getSystemService(ns);
	}

	public static void notify(Context context, String msg, int level) {
		Log.i(Logger.TAG, msg);
		if (level <= curLevel && SettingsStorage.getInstance().isToastNotifications()) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void notifyStatus(CharSequence profileName) {
		if (!PowerProfiles.NO_PROFILE.equals(profileName)) {
			contentTitle = context.getString(R.string.app_name);
			String contentText = contentTitle + " profile: " + profileName;
			Notification notification = new Notification(R.drawable.icon, contentText, System.currentTimeMillis());
			Intent notificationIntent = new Intent(context, CpuTunerTabActivity.class);
			contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			notificationManager.notify(NOTIFICATION_PROFILE, notification);

		}
	}

	public static void notifyProfile(CharSequence profileName) {
		Log.i(Logger.TAG, "Appling profile: " + profileName);
		if (instance != null) {
			instance.notifyStatus(profileName);
		}
	}

	public static void stopStatusbarNotifications() {
		try {
			instance.notificationManager.cancel(NOTIFICATION_PROFILE);
		} catch (Throwable e) {
		}
		instance = null;
	}

}
