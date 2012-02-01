package ch.amana.android.cputuner.view.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.log.Notifier;
import ch.amana.android.cputuner.service.TunerService;
import ch.amana.android.cputuner.view.widget.ServiceSwitcher;

public class ProfileAppwidgetUpdateReceiver extends BroadcastReceiver {
	private static final String ACTION_CHOOSE = "ch.amana.android.cputuner.ACTION_CHOOSE";
	private static Object lock = new Object();
	private static ProfileAppwidgetUpdateReceiver receiver = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		ProfileAppwidgetProvider.updateView(context);
	}

	public static void register(Context context) {
		// FIXME handle more than one widget
		synchronized (lock) {
			if (receiver == null) {
				receiver = new ProfileAppwidgetUpdateReceiver();
				context.registerReceiver(receiver, new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(Notifier.BROADCAST_DEVICESTATUS_CHANGED));
				context.registerReceiver(receiver, new IntentFilter(ACTION_CHOOSE));
				context.registerReceiver(receiver, new IntentFilter(TunerService.ACTION_PULSE));
				ServiceSwitcher.registerReceiver(context, receiver);
				Logger.w("Registered ProfileAppWidgetUpdateReceiver");
			} else {
				if (Logger.DEBUG) {
					Logger.i("ProfileAppWidgetUpdateReceiver allready registered, not registering again");
				}
			}
		}
	}

	public static void unregister(Context context) {
		// FIXME handle more than one widget
		synchronized (lock) {
			Logger.w("Request to unegistered ProfileAppWidgetUpdateReceiver");
			if (receiver != null) {
				try {
					Logger.logStacktrace("Unregister widget");
					context.unregisterReceiver(receiver);
					receiver = null;
					Logger.w("Unegistered ProfileAppWidgetUpdateReceiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister ProfileAppWidgetUpdateReceiver", e);
				}
			}
		}
	}
}
