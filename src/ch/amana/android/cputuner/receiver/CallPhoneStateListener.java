package ch.amana.android.cputuner.receiver;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.service.TunerService;

public class CallPhoneStateListener extends PhoneStateListener {

	private static Object lock = new Object();
	private static PhoneStateListener phoneStateListener = null;
	private final Context context;

	public CallPhoneStateListener(Context ctx) {
		super();
		this.context = ctx.getApplicationContext();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		if (SettingsStorage.FIXED_PREF_RUN_PROFILECHANGE_IN_MAINTHREAD) {
			TunerService.handlePhoneState(context, state);
		} else {
			Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_PHONESTATE);
			i.putExtra(TunerService.EXTRA_PHONE_STATE, state);
			context.startService(i);
		}
	}

	public static void register(Context context) {
		synchronized (lock) {
			if (phoneStateListener == null) {
				if (SettingsStorage.getInstance().isEnableCallInProgressProfile()) {
					TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					phoneStateListener = new CallPhoneStateListener(context);
					tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
				}
			}
		}
	}

	public static void unregister(Context context) {
		synchronized (lock) {
			Logger.w("Request to unegistered CallPhoneStateListener");
			if (phoneStateListener != null) {
				try {
					TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
					phoneStateListener = null;
					Logger.w("Unegistered BatteryReceiver");
				} catch (Throwable e) {
					Logger.w("Could not unregister CallPhoneStateListener", e);
				}
			}
		}
	}
}
