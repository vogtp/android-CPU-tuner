package ch.amana.android.cputuner.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;

public class Notifier extends BroadcastReceiver {

	public static final String BROADCAST_TRIGGER_CHANGED = "ch.amana.android.cputuner.triggerChanged";
	public static final String BROADCAST_PROFILE_CHANGED = "ch.amana.android.cputuner.profileChanged";
	public static final String BROADCAST_DEVICESTATUS_CHANGED = "ch.amana.android.cputuner.deviceStatusChanged";

	private static final int NOTIFICATION_PROFILE = 1;
	private final NotificationManager notificationManager;
	private final Context context;
	private String contentTitle;
	private PendingIntent contentIntent;
	private CharSequence lastContentText;

	private static Notifier instance;
	private Notification notification;

	public static void startStatusbarNotifications(Context ctx) {
		if (instance == null) {
			instance = new Notifier(ctx);
		}
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_PROFILE_CHANGED));
		instance.notifyStatus(PowerProfiles.getInstance().getCurrentProfileName());
	}

	public Notifier(final Context ctx) {
		super();
		this.context = ctx.getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) ctx.getSystemService(ns);
	}

	private void notifyStatus(CharSequence contentText) {
		if (!PowerProfiles.UNKNOWN.equals(contentText)) {
			if (contentText == null || contentText.equals(lastContentText)) {
				return;
			}
			lastContentText = contentText;
			contentTitle = context.getString(R.string.app_name);
			Notification notification = getNotification(contentText);
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			try {
				notificationManager.notify(NOTIFICATION_PROFILE, notification);
			} catch (Exception e) {
				Logger.e("Cannot notify " + notification);
			}
		}
	}

	private Notification getNotification(CharSequence contentText) {
		boolean isDisplayNotification = SettingsStorage.getInstance().isStatusbarNotifications();
		if (isDisplayNotification || notification == null) {
			if (!isDisplayNotification) {
				contentText = "";
			}
			notification = new Notification(R.drawable.icon, contentText, System.currentTimeMillis());
			contentIntent = PendingIntent.getActivity(context, 0, CpuTunerViewpagerActivity.getStartIntent(context), 0);

			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		return notification;
	}

	public static void stopStatusbarNotifications(Context ctx) {
		try {
			ctx.unregisterReceiver(instance);
			instance.notificationManager.cancel(NOTIFICATION_PROFILE);
		} catch (Throwable e) {
		}
		instance = null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && BROADCAST_PROFILE_CHANGED.equals(intent.getAction())) {
			StringBuffer sb = new StringBuffer(25);
			sb.append(context.getString(R.string.labelCurrentProfile));
			sb.append(" ").append(PowerProfiles.getInstance().getCurrentProfileName());
			if (PowerProfiles.getInstance().isManualProfile()) {
				sb.append(" (").append(context.getString(R.string.msg_manual_profile)).append(")");
			}
			if (PulseHelper.getInstance(context).isPulsing()) {
				int res = PulseHelper.getInstance(context).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
				sb.append(" ").append(context.getString(res));
			}
			notifyStatus(sb.toString());
		}
	}

}
