package ch.amana.android.cputuner.view.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.PopupChooserActivity;
import ch.amana.android.cputuner.view.widget.ServiceSwitcher;

public class ProfileAppwidgetProvider extends AppWidgetProvider {

	private static int intentId = 100;
	private static float textSize;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		updateView(context);
	}

	//FIXME do we need both?
	//		@Override
	//		public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	//			updateView(context);
	//		}

	public static void updateView(Context ctx) {
		if (Logger.DEBUG) {
			Logger.v("ProfileAppWidget update");
		}
		ComponentName compName2x2 = new ComponentName(ctx, ProfileAppwidgetProvider.class);
		ComponentName compName2x1 = new ComponentName(ctx, ProfileAppwidgetProvider2x1.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		int[] appWidgetIds2x2 = manager.getAppWidgetIds(compName2x2);
		int[] appWidgetIds2x1 = manager.getAppWidgetIds(compName2x1);
		boolean has2x2 = appWidgetIds2x2 != null && appWidgetIds2x2.length > 0;
		boolean has2x1 = appWidgetIds2x1 != null && appWidgetIds2x1.length > 0;
		if (has2x2 || has2x1) {
			RemoteViews rViews;
			if (SettingsStorage.getInstance(ctx).hasWidget()) {
				rViews = createAppWidgetView(ctx);
			} else {
				rViews = createNotEnabledView(ctx);
			}
			if (has2x2) {
				manager.updateAppWidget(compName2x2, rViews);
			}
			if (has2x1) {
				manager.updateAppWidget(compName2x1, rViews);
			}
		}
	}

	static RemoteViews createNotEnabledView(Context ctx) {
		RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.appwidget_base);
		PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, BillingProductListActiviy.getExtentionsIntent(ctx), 0);
		views.setOnClickPendingIntent(R.id.topAppWiget, pendingIntent);
		views.setTextViewText(R.id.tvWidgetMsg, ctx.getString(R.string.msg_widgets_not_installed));
		return views;
	}

	static RemoteViews createAppWidgetView(Context context) {
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
		}else {
			views.setViewVisibility(R.id.ivCpuTunerIcon, View.GONE);
		}
		if (settings.isShowWidgetTrigger()) {
			views.setViewVisibility(R.id.labelTrigger, labelVisibility);
			views.setViewVisibility(R.id.tvTrigger, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.tvTrigger, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.labelTrigger, chooseProfilePendingIntent);
			setTextSize(views, R.id.tvTrigger);
			if (settings.isEnableProfiles()) {
				views.setTextViewText(R.id.tvTrigger, powerProfiles.getCurrentTriggerName());
				views.setTextColor(R.id.tvTrigger, Color.WHITE);
			} else {
				views.setTextViewText(R.id.tvTrigger, context.getString(R.string.notEnabled));
				views.setTextColor(R.id.tvTrigger, Color.RED);
			}
		}else {
			views.setViewVisibility(R.id.tvTrigger, View.GONE);
			views.setViewVisibility(R.id.labelTrigger, View.GONE);
		}
		if (settings.isShowWidgetProfile()) {
			views.setViewVisibility(R.id.labelProfile, labelVisibility);
			views.setViewVisibility(R.id.tvProfile, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.labelProfile, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.tvProfile, chooseProfilePendingIntent);
			setTextSize(views, R.id.tvProfile);
		}else {
			views.setViewVisibility(R.id.labelProfile, View.GONE);
			views.setViewVisibility(R.id.tvProfile, View.GONE);
		}
		if (settings.isShowWidgetGovernor()) {
			views.setViewVisibility(R.id.labelGov, labelVisibility);
			views.setViewVisibility(R.id.tvGov, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.labelGov, chooseProfilePendingIntent);
			views.setOnClickPendingIntent(R.id.tvGov, chooseProfilePendingIntent);
			if (settings.isUseVirtualGovernors()) {
				setTextSize(views, R.id.tvGov);
				views.setViewVisibility(R.id.tvGov, View.VISIBLE);
				views.setTextViewText(R.id.tvGov, powerProfiles.getCurrentVirtGovName());
			} else {
				views.setViewVisibility(R.id.labelGov, View.GONE);
				views.setViewVisibility(R.id.tvGov, View.GONE);
			}
			
		}else {
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
			
		}else {
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

		if (Logger.DEBUG) {
			views.setViewVisibility(R.id.tvUpdate, View.VISIBLE);
			views.setTextViewText(R.id.tvUpdate, SettingsStorage.dateTimeFormat.format(System.currentTimeMillis()));
		}

		return views;
	}

	private static void setTextSize(RemoteViews views, int id) {
		views.setFloat(id, "setTextSize", textSize);
	}

	private static void setServiceStateIcon(Context context, RemoteViews views, int id, ServiceType serviceType) {
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

	private static void setImageResource(RemoteViews views, int id, int iconId) {
		views.setInt(id, "setImageResource", iconId);
	}

	private static void setAlpha(RemoteViews views, int id, int alpha) {
		views.setInt(id, "setAlpha", alpha);
	}

	private static void setAnimation(RemoteViews views, int id, int anim) {
		views.setInt(id, "setAnimation", anim);
	}

}
