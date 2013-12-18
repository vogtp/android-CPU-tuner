package ch.amana.android.cputuner.log;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.PopupChooserActivity;
import ch.amana.android.cputuner.view.widget.ServiceSwitcher;

public class Notifier extends BroadcastReceiver {

	public static final String BROADCAST_TRIGGER_CHANGED = "ch.amana.android.cputuner.triggerChanged";
	public static final String BROADCAST_PROFILE_CHANGED = "ch.amana.android.cputuner.profileChanged";
	public static final String BROADCAST_DEVICESTATUS_CHANGED = "ch.amana.android.cputuner.deviceStatusChanged";

	public static final int NOTIFICATION_ID = 1;
	private final NotificationManager notificationManager;
	private final Context context;
	private String contentTitle;
	private PendingIntent contentIntent;
	private CharSequence lastContentText;

	private static Notifier instance;
	private static float textSize;
	private Notification notification;
	private int icon;
	private static int intentId;

	public static Notification startStatusbarNotifications(final Context ctx) {
		if (instance == null) {
			instance = new Notifier(ctx);
		}
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_TRIGGER_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_PROFILE_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_DEVICESTATUS_CHANGED));
		instance.notifyStatus();
		return instance.notification;
	}

	public static void stopStatusbarNotifications(final Context ctx) {
		try {
			ctx.unregisterReceiver(instance);
			instance.notificationManager.cancel(NOTIFICATION_ID);
		} catch (Throwable e) {
		}
		instance = null;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent == null) {
			return;
		}
		notifyStatus();
		//		if (SettingsStorage.getInstance(context).hasWidget()) {
		//			// the widget checks if it exists
		//			ProfileAppwidgetProvider.updateView(context);
		//		}
	}

	public Notifier(final Context ctx) {
		super();
		this.context = ctx.getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) ctx.getSystemService(ns);
	}

	private void notifyStatus() {
		PowerProfiles powerProfiles = PowerProfiles.getInstance(context);
		CharSequence profileName = powerProfiles.getCurrentProfileName();
		if (!PowerProfiles.UNKNOWN.equals(profileName)) {
			StringBuffer sb = new StringBuffer(25);
			String contentText = null;
			if (SettingsStorage.getInstance().isEnableCpuTuner()) {
				sb.append(context.getString(R.string.labelCurrentProfile));
				sb.append(" ").append(PowerProfiles.getInstance().getCurrentProfileName());
				if (PowerProfiles.getInstance().isManualProfile()) {
					sb.append(" (").append(context.getString(R.string.msg_manual_profile)).append(")");
				}
				sb.append(" - ").append(context.getString(R.string.labelCurrentBattery)).append(" ");
				sb.append(powerProfiles.getBatteryLevel()).append(context.getString(R.string.percent));
				if (PulseHelper.getInstance(context).isPulsing()) {
					int res = PulseHelper.getInstance(context).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
					sb.append(" - ").append(context.getString(res));
				}
				contentText = sb.toString();
			} else {
				contentText = context.getString(R.string.msg_cpu_tuner_not_running);
			}
			if (contentText == null || contentText.equals(lastContentText)) {
				return;
			}
			lastContentText = contentText;
			contentTitle = context.getString(R.string.app_name);
			Notification notification = getNotification(contentText);
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			try {
				notificationManager.notify(NOTIFICATION_ID, notification);
			} catch (Exception e) {
				Logger.e("Cannot notify " + notification);
			}
		}
	}

	private Notification getNotification(CharSequence contentText) {
		boolean isDisplayNotification = SettingsStorage.getInstance().isStatusbarNotifications();
		int iconNew = R.drawable.ic_menu_cputuner;
		boolean jellyBean = SettingsStorage.getInstance().isJellyBean();
		if (!SettingsStorage.getInstance().isEnableCpuTuner()) {
			iconNew = R.drawable.ic_menu_cputuner_red;
		} else if (PowerProfiles.getInstance().isManualProfile()) {
			iconNew = R.drawable.ic_menu_cputuner_yellow;
		}
		if (isDisplayNotification || (notification == null || jellyBean) || icon != iconNew) {
			if (!isDisplayNotification) {
				contentText = "";
			}
			icon = iconNew;
			if (jellyBean) {
				Builder builder = new Notification.Builder(context)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(contentText)
				//						.setWhen(System.currentTimeMillis())
				.setSmallIcon(icon)
				.setOngoing(true);

				intentId = 100;
				if (false) {
					setServiceIcon(builder, ServiceType.mobiledata3g);
					setServiceIcon(builder, ServiceType.mobiledataConnection);
					setServiceIcon(builder, ServiceType.wifi);
					setServiceIcon(builder, ServiceType.airplainMode);
					setServiceIcon(builder, ServiceType.backgroundsync);
					setServiceIcon(builder, ServiceType.bluetooth);
				}else {
					RemoteViews views = createAppWidgetView(context);

					builder.setContent(views);

				}

				notification = builder.getNotification();

			} else {
				notification = new Notification(icon, contentText, System.currentTimeMillis());
			}
			contentIntent = PendingIntent.getActivity(context, 0, CpuTunerViewpagerActivity.getStartIntent(context), 0);
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;

		}
		return notification;
	}

	private void setServiceIcon(final Builder builder, final ServiceType serviceType) {
		// TODO Auto-generated method stub
		int imgRes = 0;
		Intent intent = PopupChooserActivity.getStartIntent(context);
		intent.putExtra(PopupChooserActivity.EXTRA_CHOOSER_TYPE, PopupChooserActivity.CHOOSER_TYPE_SERVICE);
		intent.putExtra(PopupChooserActivity.EXTRA_SERVICE_TYPE, serviceType.toString());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, intentId++, intent, PendingIntent.FLAG_ONE_SHOT);
		int state = ServicesHandler.getServiceState(context, serviceType);
		switch (serviceType) {
		case mobiledata3g:
			if (state == PowerProfiles.SERVICE_STATE_2G) {
				imgRes = R.drawable.serviceicon_md_2g;
			} else if (state == PowerProfiles.SERVICE_STATE_2G_3G) {
				imgRes = R.drawable.serviceicon_md_2g3g;
			} else if (state == PowerProfiles.SERVICE_STATE_3G) {
				imgRes = R.drawable.serviceicon_md_3g;
			} else {
				imgRes = R.drawable.serviceicon_md_2g3g;
			}
			break;
		case airplainMode:
			imgRes = R.drawable.serviceicon_airplane;
			break;
		case backgroundsync:
			imgRes = R.drawable.serviceicon_sync;
			break;
		case bluetooth:
			imgRes = R.drawable.serviceicon_bluetooth;
			break;
		case mobiledataConnection:
			imgRes = R.drawable.serviceicon_mobiledata_con;
			break;
		case wifi:
			imgRes = R.drawable.serviceicon_wifi;
			break;
		}
		builder.setContentIntent(pendingIntent);
		// builder.addAction(imgRes, null, pendingIntent);
	}

	static RemoteViews createAppWidgetView(final Context context) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.profile_appwidget);
		PendingIntent mainPendingIntent;

		SettingsStorage settings = SettingsStorage.getInstance(context);
		textSize = settings.getWidgetTextSize();
		int labelVisibility = settings.showWidgetLabels() ? View.VISIBLE : View.GONE;
		PendingIntent startCpuTunerPendingIntent = PendingIntent.getActivity(context, 0, CpuTunerViewpagerActivity.getStartIntent(context), 0);
		Intent profileChooserIntent = PopupChooserActivity.getStartIntent(context);
		profileChooserIntent.putExtra(PopupChooserActivity.EXTRA_CHOOSER_TYPE, PopupChooserActivity.CHOOSER_TYPE_PROFILE);
		PendingIntent chooseProfilePendingIntent = PendingIntent.getActivity(context, 1, profileChooserIntent, 0);
		PendingIntent batteryPendingIntent = PendingIntent.getActivity(context, 2, new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), PendingIntent.FLAG_ONE_SHOT);
		switch (settings.getAppwdigetOpenAction()) {
		case SettingsStorage.APPWIDGET_OPENACTION_CHOOSEPROFILES:
			mainPendingIntent = chooseProfilePendingIntent;
			break;
		default:
			mainPendingIntent = startCpuTunerPendingIntent;
			break;
		}

		PowerProfiles powerProfiles = PowerProfiles.getInstance(context);
		views.setOnClickPendingIntent(R.id.topAppWiget, mainPendingIntent);
		if (settings.isShowWidgetIcon()) {
			views.setViewVisibility(R.id.ivCpuTunerIcon, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.ivCpuTunerIcon, startCpuTunerPendingIntent);
		} else {
			views.setViewVisibility(R.id.ivCpuTunerIcon, View.GONE);
		}
		if (settings.isShowWidgetTrigger()) {
			views.setViewVisibility(R.id.labelTrigger, labelVisibility);
			views.setViewVisibility(R.id.tvTrigger, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.tvTrigger, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.labelTrigger, chooseProfilePendingIntent);
			setTextSize(views, R.id.tvTrigger);
			if (settings.isEnableCpuTuner()) {
				views.setTextViewText(R.id.tvTrigger, powerProfiles.getCurrentTriggerName());
				views.setTextColor(R.id.tvTrigger, Color.WHITE);
			} else {
				views.setTextViewText(R.id.tvTrigger, context.getString(R.string.notEnabled));
				views.setTextColor(R.id.tvTrigger, Color.RED);
			}
		} else {
			views.setViewVisibility(R.id.tvTrigger, View.GONE);
			views.setViewVisibility(R.id.labelTrigger, View.GONE);
		}
		if (settings.isShowWidgetProfile()) {
			views.setViewVisibility(R.id.labelProfile, labelVisibility);
			views.setViewVisibility(R.id.tvProfile, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.labelProfile, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.tvProfile, chooseProfilePendingIntent);
			setTextSize(views, R.id.tvProfile);
		} else {
			views.setViewVisibility(R.id.labelProfile, View.GONE);
			views.setViewVisibility(R.id.tvProfile, View.GONE);
		}
		if (settings.isShowWidgetGovernor()) {
			views.setViewVisibility(R.id.labelGov, labelVisibility);
			views.setViewVisibility(R.id.tvGov, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.labelGov, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.tvGov, chooseProfilePendingIntent);
			setTextSize(views, R.id.tvGov);
			views.setViewVisibility(R.id.tvGov, View.VISIBLE);
			views.setTextViewText(R.id.tvGov, powerProfiles.getCurrentVirtGovName());

		} else {
			views.setViewVisibility(R.id.labelGov, View.GONE);
			views.setViewVisibility(R.id.tvGov, View.GONE);
		}
		if (settings.isShowWidgetBattery()) {
			views.setViewVisibility(R.id.tvBattery, View.VISIBLE);
			views.setViewVisibility(R.id.labelBattery, labelVisibility);
			views.setOnClickPendingIntent(R.id.tvBattery, batteryPendingIntent);
			views.setOnClickPendingIntent(R.id.labelBattery, batteryPendingIntent);
			setTextSize(views, R.id.tvBattery);
			views.setTextViewText(R.id.tvBattery, powerProfiles.getBatteryInfo());
			if (powerProfiles.hasManualServicesChanges()) {
				views.setViewVisibility(R.id.tvServiceMsg, View.VISIBLE);

			} else {
				views.setViewVisibility(R.id.tvServiceMsg, View.GONE);
			}

		} else {
			views.setViewVisibility(R.id.tvBattery, View.GONE);
			views.setViewVisibility(R.id.labelBattery, View.GONE);
		}
		if (PulseHelper.getInstance(context).isPulsing()) {
			views.setViewVisibility(R.id.tvPulse, View.VISIBLE);
			int res = PulseHelper.getInstance(context).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
			views.setTextViewText(R.id.tvPulse, context.getString(res));
		} else {
			views.setViewVisibility(R.id.tvPulse, View.GONE);
		}
		if (powerProfiles.isManualProfile()) {
			StringBuilder sb = new StringBuilder(powerProfiles.getCurrentProfileName());
			sb.append("\n(").append(context.getString(R.string.msg_manual_profile)).append(")");
			views.setTextColor(R.id.tvProfile, Color.YELLOW);
			views.setTextViewText(R.id.tvProfile, sb.toString());
		} else {
			views.setTextViewText(R.id.tvProfile, powerProfiles.getCurrentProfileName());
			views.setTextColor(R.id.tvProfile, Color.WHITE);
		}

		views.setViewVisibility(R.id.ivServiceGPS, View.GONE);

		if (settings.isShowWidgetServices()) {
			intentId = 100;
			views.setViewVisibility(R.id.llServiceIcons_ref, View.VISIBLE);
			setServiceStateIcon(context, views, R.id.ivServiceAirplane, ServiceType.airplainMode);
			setServiceStateIcon(context, views, R.id.ivServiceSync, ServiceType.backgroundsync);
			setServiceStateIcon(context, views, R.id.ivServiceBluetooth, ServiceType.bluetooth);
			setServiceStateIcon(context, views, R.id.ivServiceMD3g, ServiceType.mobiledata3g);
			setServiceStateIcon(context, views, R.id.ivServiceMDCon, ServiceType.mobiledataConnection);
			setServiceStateIcon(context, views, R.id.ivServiceWifi, ServiceType.wifi);

		} else {
			views.setViewVisibility(R.id.llServiceIcons_ref, View.GONE);
		}

		return views;
	}

	private static void setTextSize(final RemoteViews views, final int id) {
		views.setFloat(id, "setTextSize", textSize);
	}

	private static void setServiceStateIcon(final Context context, final RemoteViews views, final int id, final ServiceType serviceType) {
		Intent intent = PopupChooserActivity.getStartIntent(context);
		int size = context.getResources().getDimensionPixelSize(R.dimen.widget_iconsize_medium);
		views.setBoolean(id, "setAdjustViewBounds", true);
		views.setInt(id, "setMaxHeight", size);
		views.setInt(id, "setMaxWidth", size);
		intent.putExtra(PopupChooserActivity.EXTRA_CHOOSER_TYPE, PopupChooserActivity.CHOOSER_TYPE_SERVICE);
		intent.putExtra(PopupChooserActivity.EXTRA_SERVICE_TYPE, serviceType.toString());
		PendingIntent pendingIntent = PendingIntent.getActivity(context, intentId++, intent, PendingIntent.FLAG_ONE_SHOT);
		views.setOnClickPendingIntent(id, pendingIntent);
		int state = ServicesHandler.getServiceState(context, serviceType);
		if (serviceType == ServiceType.mobiledata3g) {
			if (state == PowerProfiles.SERVICE_STATE_2G) {
				setImageResource(views, id, R.drawable.serviceicon_md_2g);
			} else if (state == PowerProfiles.SERVICE_STATE_2G_3G) {
				setImageResource(views, id, R.drawable.serviceicon_md_2g3g);
			} else if (state == PowerProfiles.SERVICE_STATE_3G) {
				setImageResource(views, id, R.drawable.serviceicon_md_3g);
			} else {
				setImageResource(views, id, R.drawable.serviceicon_md_2g3g);
			}
		} else {
			if (state == PowerProfiles.SERVICE_STATE_LEAVE) {
				setAlpha(views, id, ServiceSwitcher.ALPHA_LEAVE);
			} else if (state == PowerProfiles.SERVICE_STATE_OFF) {
				setAlpha(views, id, ServiceSwitcher.ALPHA_OFF);
			} else if (state == PowerProfiles.SERVICE_STATE_PREV) {
				setAnimation(views, id, R.anim.back);
			} else if (state == PowerProfiles.SERVICE_STATE_PULSE) {
				setAnimation(views, id, R.anim.pluse);
			} else {
				setAlpha(views, id, ServiceSwitcher.ALPHA_ON);
			}
		}
	}

	private static void setImageResource(final RemoteViews views, final int id, final int iconId) {
		views.setInt(id, "setImageResource", iconId);
	}

	private static void setAlpha(final RemoteViews views, final int id, final int alpha) {
		views.setInt(id, "setAlpha", alpha);
	}

	private static void setAnimation(final RemoteViews views, final int id, final int anim) {
		views.setInt(id, "setAnimation", anim);
	}

}
