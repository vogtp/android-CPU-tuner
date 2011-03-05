package ch.amana.android.cputuner.helper;


import android.content.Context;
import android.os.Handler;
import ch.amana.android.cputuner.hw.ServicesHandler;

public class PulseHelper {

	private static final long MIN_TO_MILLIES = 1000 * 60;

	private static Handler handler = new Handler();
	private static boolean pulsing = false;
	private boolean isOn = false;

	private Context ctx;

	private boolean pulseBackgroundSyncState = false;

	private boolean pulseBluetoothState = false;

	private boolean pulseGpsState = false;

	private boolean pulseWifiState = false;

	private boolean pulseMobiledataConnectionState = false;

	private static PulseHelper instance;

	private final Runnable runner = new Runnable() {

		@Override
		public void run() {
			if (pulsing) {
				isOn = !isOn;
				doit(isOn);
				long delay = isOn ? SettingsStorage.getInstance().getPulseDelayOn() : SettingsStorage.getInstance().getPulseDelayOff();
				long delayInMillies = delay * MIN_TO_MILLIES;
				Logger.i("Pluse is " + isOn + " next switch in " + delay + " minutes (" + delayInMillies + " millies)");
				handler.postDelayed(this, delayInMillies);
			}
		}

	};



	public static PulseHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new PulseHelper(ctx.getApplicationContext());
		}
		return instance;
	}

	static int p = 0;

	private void doit(boolean isOn) {
		if (pulsing) {
			if ( pulseBackgroundSyncState ) {
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
			if (pulseMobiledataConnectionState) {
				ServicesHandler.enableMobileData(ctx, isOn);
			}
		}
	}

	public void doPulsing(boolean b) {
		if (pulsing == b) {
			return;
		}
		pulsing = b;
		if (pulsing) {
			runner.run();
		} else {
			handler.removeCallbacks(runner);
		}
	}

	protected PulseHelper(Context context) {
		super();
		this.ctx = context;
	}

	public boolean isPulsing() {
		return pulsing;
	}

	public void pulseBackgroundSyncState(boolean b) {
		pulseBackgroundSyncState = b;
		doPulsing(b);
	}

	public void pulseBluetoothState(boolean b) {
		pulseBluetoothState = b;
		doPulsing(b);
	}

	public void pulseGpsState(boolean b) {
		pulseGpsState = b;
		doPulsing(b);
	}

	public void pulseWifiState(boolean b) {
		pulseWifiState = b;
		doPulsing(b);
	}

	public void pulseMobiledataConnectionState(boolean b) {
		pulseMobiledataConnectionState = b;
		doPulsing(b);
	}

}
