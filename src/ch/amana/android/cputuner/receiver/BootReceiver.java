package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.service.BatteryService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			handleBootCompleted(context);
		}
	}

	private void handleBootCompleted(Context context) {
		SettingsStorage storage = SettingsStorage.getInstance();
		if (storage.isEnableProfiles()) {
			Logger.w("Starting CPU tuner on boot");
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				context.startService(new Intent(context, BatteryService.class));
			}
		}else {
			Logger.w("Starting CPU tuner on boot (no boot start)");
			context.stopService(new Intent(context, BatteryService.class));
			BatteryReceiver.unregisterBatteryReceiver(context);
		}
	}

}
