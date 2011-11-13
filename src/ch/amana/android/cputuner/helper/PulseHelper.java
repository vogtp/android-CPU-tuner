package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.content.Intent;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.hw.ServicesHandler;
import ch.amana.android.cputuner.service.TunerService;

public class PulseHelper {

	private boolean pulsing = false;

	private boolean pulseOn = false;

	private final Context ctx;

	private boolean pulseBackgroundSyncState = false;

	private boolean pulseBluetoothState = false;

	private boolean pulseGpsState = false;

	private boolean pulseWifiState = false;

	private boolean pulseMobiledataConnectionState = false;

	private boolean pulseAirplanemodeState;

	private static PulseHelper instance;


	public static PulseHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new PulseHelper(ctx.getApplicationContext());
		}
		return instance;
	}

	public void doPulse(boolean isOn) {
		SettingsStorage settings = SettingsStorage.getInstance();
		//		if (!settings.isEnableProfiles()) {
		//			Logger.i("Not pulsing since profiles are not eabled.");
		//			return;
		//		}
		if (pulsing) {
			this.pulseOn = isOn;
			if (settings.isLogPulse()) {
				Logger.addToSwitchLog(ctx.getString(isOn ? R.string.msg_pulse_log_on : R.string.msg_pulse_log_off));
			}
			if (pulseBackgroundSyncState) {
				ServicesHandler.enableBackgroundSync(ctx, isOn);
			}
			if (pulseBluetoothState) {
				ServicesHandler.enableBluetooth(isOn);
			}
			if (pulseGpsState) {
				ServicesHandler.enableGps(ctx, isOn);
			}
			if (pulseWifiState) {
				ServicesHandler.enableWifi(ctx, isOn);
			}
			if (pulseAirplanemodeState) {
				ServicesHandler.enableAirplaneMode(ctx, isOn);
			}
			if (pulseMobiledataConnectionState) {
				if (settings.isPulseMobiledataOnWifi()) {
					ServicesHandler.enableMobileData(ctx, isOn);
				} else {
					if (isOn && ServicesHandler.isWifiConnected(ctx)) {
						Logger.i("Not pulsing mobiledata since wifi is connected");
					} else {
						ServicesHandler.enableMobileData(ctx, isOn);
					}
				}
			}
			ctx.sendBroadcast(new Intent(Notifier.BROADCAST_PROFILE_CHANGED));
		}
	}

	private void doPulsing(boolean b) {
		if (pulsing == b) {
			return;
		}
		if (b && !pulsing) {
			pulsing = true;
			PulseHelper.startPulseService(ctx);
		}
	}

	public void stopPulseIfNeeded() {
		if (pulsing) {
			boolean someService = pulseBackgroundSyncState || pulseBluetoothState || pulseGpsState || pulseWifiState || pulseMobiledataConnectionState;
			if (!someService) {
				PulseHelper.stopPulseService(ctx);
			}
		}
	}

	protected PulseHelper(Context context) {
		super();
		this.ctx = context;
	}

	public boolean isPulsing() {
		return pulsing;
	}

	public boolean isPulsing(ServiceType type) {
		if (!pulsing) {
			return false;
		}
		switch (type) {
		case wifi:
			return pulseWifiState;
		case bluetooth:
			return pulseBluetoothState;
		case mobiledataConnection:
			return pulseMobiledataConnectionState;
		case backgroundsync:
			return pulseBackgroundSyncState;
		case airplainMode:
			return pulseAirplanemodeState;
		case gps:
			return pulseGpsState;
		case mobiledata3g:
			return false;
		default:
			Logger.e("Did not find service type " + type.toString() + " for pulsing.");
		}
		return false;
	}

	public void pulse(ServiceType type, boolean b) {
		switch (type) {
		case wifi:
			pulseWifiState(b);
			break;
		case bluetooth:
			pulseBluetoothState(b);
			break;
		case mobiledataConnection:
			pulseMobiledataConnectionState(b);
			break;
		case backgroundsync:
			pulseBackgroundSyncState(b);
			break;
		case airplainMode:
			pulseAirplanemodeState(b);
			break;
		case gps:
			pulseGpsState(b);
			break;
		case mobiledata3g:
			Logger.w("Cannot pulse mobiledata 3G");
			break;
		default:
			Logger.e("Did not find service type " + type.toString() + " for pulsing.");
		}
	}

	public void pulseBackgroundSyncState(boolean b) {
		if (!b && pulseBackgroundSyncState && pulseOn) {
			ServicesHandler.enableBackgroundSync(ctx, b);
		}
		pulseBackgroundSyncState = b;
		doPulsing(b);
	}

	public void pulseBluetoothState(boolean b) {
		if (!b && pulseBluetoothState && pulseOn) {
			ServicesHandler.enableBluetooth(b);
		}
		pulseBluetoothState = b;
		doPulsing(b);
	}

	public void pulseGpsState(boolean b) {
		if (!b && pulseGpsState && pulseOn) {
			ServicesHandler.enableGps(ctx, b);
		}
		pulseGpsState = b;
		doPulsing(b);
	}

	public void pulseWifiState(boolean b) {
		if (!b && pulseWifiState && pulseOn) {
			ServicesHandler.enableWifi(ctx, b);
		}
		pulseWifiState = b;
		doPulsing(b);
	}

	public void pulseMobiledataConnectionState(boolean b) {
		if (!b && pulseMobiledataConnectionState && pulseOn) {
			ServicesHandler.enableMobileData(ctx, b);
		}
		pulseMobiledataConnectionState = b;
		doPulsing(b);
	}

	public void pulseAirplanemodeState(boolean b) {
		if (!b && pulseAirplanemodeState && pulseOn) {
			ServicesHandler.enableAirplaneMode(ctx, false);
		}
		pulseAirplanemodeState = b;
		doPulsing(b);
	}

	public boolean isOn() {
		return pulseOn;
	}

	public static void startPulseService(final Context ctx) {
		Logger.w("Start pulse service now");
		Intent i = new Intent(TunerService.ACTION_PULSE);
		i.putExtra(TunerService.EXTRA_PULSE_START, true);
		ctx.startService(i);
	}

	public static void stopPulseService(Context ctx) {
		PulseHelper pulseHelper = PulseHelper.getInstance(ctx);
		if (!pulseHelper.pulsing) {
			return;
		}
		Logger.w("Stop pulse service");
		if (SettingsStorage.getInstance().isLogPulse()) {
			Logger.addToSwitchLog(ctx.getString(R.string.msg_pulse_log_end));
		}
		pulseHelper.pulsing = false;
		ctx.sendBroadcast(new Intent(Notifier.BROADCAST_PROFILE_CHANGED));

		Intent intent = new Intent(TunerService.ACTION_PULSE);
		intent.putExtra(TunerService.EXTRA_PULSE_STOP, true);
		ctx.startService(intent);
	}

}
