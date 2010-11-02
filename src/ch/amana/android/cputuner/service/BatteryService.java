package ch.amana.android.cputuner.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.receiver.BatteryReceiver;

public class BatteryService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(Logger.TAG, "Starting BatteryService");
		// BatteryReceiver.unregisterBatteryReceiver(this);
		BatteryReceiver.registerBatteryReceiver(this);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
