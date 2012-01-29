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
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.ProfileChooserActivity;

public class ProfileAppwidgetProvider extends AppWidgetProvider {

	@Override
	public void onDisabled(Context context) {
		context.startService(new Intent(ProfileAppwidgetUpdateService.ACTION_STOP_PROFILEWIDGET_UPDATE));
		super.onDisabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		context.startService(new Intent(ProfileAppwidgetUpdateService.ACTION_START_PROFILEWIDGET_UPDATE));
		updateView(context);
	}

	public static void updateView(Context ctx) {
		if (Logger.DEBUG) {
			Logger.v("ProfileAppWidget update");
		}
		RemoteViews rViews;
		if (SettingsStorage.getInstance(ctx).hasWidget()) {
			rViews = createAppWidgetView(ctx);
		} else {
			rViews = createNotEnabledView(ctx);
		}
		ComponentName compName = new ComponentName(ctx, ProfileAppwidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(ctx);
		manager.updateAppWidget(compName, rViews);
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
		PendingIntent startCpuTunerPendingIntent = PendingIntent.getActivity(context, 0, CpuTunerViewpagerActivity.getStartIntent(context), 0);
		PendingIntent chooseProfilePendingIntent = PendingIntent.getActivity(context, 0, ProfileChooserActivity.getStartIntent(context), 0);
		PendingIntent batteryPendingIntent = PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 0);
		switch (settings.getAppwdigetOpenAction()) {
		case SettingsStorage.APPWIDGET_OPENACTION_CHOOSEPROFILES:
			mainPendingIntent = chooseProfilePendingIntent;
			break;
		default:
			mainPendingIntent = startCpuTunerPendingIntent;
			break;
		}
		

		views.setOnClickPendingIntent(R.id.topAppWiget, mainPendingIntent);
		views.setOnClickPendingIntent(R.id.ivCpuTunerIcon, startCpuTunerPendingIntent);
		views.setOnClickPendingIntent(R.id.tvTrigger, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.tvProfile, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.tvGov, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.labelTrigger, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.labelProfile, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.labelGov, chooseProfilePendingIntent);
		views.setOnClickPendingIntent(R.id.tvBattery, batteryPendingIntent);
		views.setOnClickPendingIntent(R.id.labelBattery, batteryPendingIntent);

		PowerProfiles powerProfiles = PowerProfiles.getInstance(context);
		if (settings.isEnableProfiles()) {
			views.setTextViewText(R.id.tvTrigger, powerProfiles.getCurrentTriggerName());
			views.setTextColor(R.id.tvTrigger, Color.WHITE);
		} else {
			views.setTextViewText(R.id.tvTrigger, context.getString(R.string.notEnabled));
			views.setTextColor(R.id.tvTrigger, Color.RED);
		}
		if (powerProfiles.isManualProfile()) {
			StringBuilder sb = new StringBuilder(powerProfiles.getCurrentProfileName());
			sb.append(" (").append(context.getString(R.string.msg_manual_profile)).append(")");
			views.setTextColor(R.id.tvProfile, Color.YELLOW);
			views.setTextViewText(R.id.tvProfile, sb.toString());
		} else {
			views.setTextViewText(R.id.tvProfile, powerProfiles.getCurrentProfileName());
			views.setTextColor(R.id.tvProfile, Color.WHITE);
		}
		if (settings.isUseVirtualGovernors()) {
			views.setViewVisibility(R.id.tvGov, View.VISIBLE);
			views.setTextViewText(R.id.tvGov, powerProfiles.getCurrentVirtGovName());
		} else {
			views.setViewVisibility(R.id.tvGov, View.GONE);
		}
		views.setTextViewText(R.id.tvBattery, powerProfiles.getBatteryInfo());
		if (powerProfiles.hasManualServicesChanges()) {
			views.setViewVisibility(R.id.tvServiceMsg, View.VISIBLE);
		} else {
			views.setViewVisibility(R.id.tvServiceMsg, View.GONE);
		}
		if (PulseHelper.getInstance(context).isPulsing()) {
			views.setViewVisibility(R.id.tvPulse, View.VISIBLE);
			int res = PulseHelper.getInstance(context).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
			views.setTextViewText(R.id.tvPulse, context.getString(res));
		} else {
			views.setViewVisibility(R.id.tvPulse, View.GONE);
		}
		return views;
	}

	public static void enableWidget(Context ctx, boolean b) {
		// This only works post 3.1 and pre 4.0
		//		PackageManager pm = ctx.getApplicationContext().getPackageManager();
		//		//		pm.setComponentEnabledSetting(
		//		//                new ComponentName("com.example.android.apis", ".appwidget.ExampleBroadcastReceiver"),
		//		//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		//		//                PackageManager.DONT_KILL_APP);
		//		ComponentName componentName = new ComponentName("ch.amana.android.cputuner", ".view.appwidget.ProfileAppwidgetProvider");
		//		ComponentName componentName2 = new ComponentName(ctx, ProfileAppwidgetProvider.class);
		//		int state = b ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		//		pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
	}

}
