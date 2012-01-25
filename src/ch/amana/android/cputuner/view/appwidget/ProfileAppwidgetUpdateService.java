package ch.amana.android.cputuner.view.appwidget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import ch.amana.android.cputuner.log.Logger;

public class ProfileAppwidgetUpdateService extends Service {

	public static final String ACTION_START_PROFILEWIDGET_UPDATE = "ch.amana.android.cputuner.ACTION_START_PROFILEWIDGET_UPDATE";
	public static final String ACTION_STOP_PROFILEWIDGET_UPDATE = "ch.amana.android.cputuner.ACTION_STOP_PROFILEWIDGET_UPDATE";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (intent == null) {
			Logger.w("ProfileAppWidgetUpdateService got null intent");
			return START_NOT_STICKY;
		}
		String serviceAction = intent.getAction();
		Logger.i("ProfileAppWidgetUpdateService " + serviceAction);

		if (ACTION_START_PROFILEWIDGET_UPDATE.equals(serviceAction)) {
			ProfileAppwidgetUpdateReceiver.register(this);
		} else if (ACTION_STOP_PROFILEWIDGET_UPDATE.equals(serviceAction)) {
			ProfileAppwidgetUpdateReceiver.unregister(this);
			stopSelf();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		ProfileAppwidgetUpdateReceiver.unregister(this);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
