package ch.amana.android.cputuner.view.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;

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

	static void updateView(Context ctx) {
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
		Intent intent = CpuTunerViewpagerActivity.getStartIntent(context);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.topAppWiget, pendingIntent);
		PowerProfiles powerProfiles = PowerProfiles.getInstance(context);

		views.setTextViewText(R.id.tvTrigger, powerProfiles.getCurrentTriggerName());
		views.setTextViewText(R.id.tvProfile, powerProfiles.getCurrentProfileName());
		if (SettingsStorage.getInstance(context).isUseVirtualGovernors()) {
			views.setViewVisibility(R.id.tvGov, View.VISIBLE);
			views.setTextViewText(R.id.tvGov, powerProfiles.getCurrentVirtGovName());
		} else {
			views.setViewVisibility(R.id.tvGov, View.GONE);
		}
		views.setTextViewText(R.id.tvBattery, powerProfiles.getBatteryInfo());

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
