package ch.amana.android.cputuner.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.Cpu;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			handleBootCompleted();
			BatteryReceiver.registerBatteryReceiver(context);
		}
	}

	private void handleBootCompleted() {
		SettingsStorage storage = SettingsStorage.getInstance();
		if (storage.isApplyOnBoot()) {
			Log.w(Logger.TAG, "Appling CPU settings on boot");
			Cpu cpu = new Cpu();
			cpu.applyFromStorage();
		}
	}

}
