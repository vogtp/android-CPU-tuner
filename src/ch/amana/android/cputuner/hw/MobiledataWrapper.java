package ch.amana.android.cputuner.hw;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import ch.amana.android.cputuner.helper.Logger;

public class MobiledataWrapper {

	private static MobiledataWrapper instance;

	private Method setMobileDataEnabled;

	private Method getMobileDataEnabled;

	private ConnectivityManager cm;

	public static MobiledataWrapper getInstance(Context ctx) {
		if (instance == null) {
			instance = new MobiledataWrapper(ctx.getApplicationContext());
		}
		return instance;
	}

	private MobiledataWrapper(Context ctx) {
		super();
		cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class<?> c = Class.forName(cm.getClass().getName());
			setMobileDataEnabled = c.getMethod("setMobileDataEnabled", boolean.class);
			getMobileDataEnabled = c.getMethod("getMobileDataEnabled");
		} catch (Exception e) {
			Logger.e("Cannot access mobiledata controll!", e);
		}
	}

	public boolean canUse() {
		return setMobileDataEnabled != null && getMobileDataEnabled != null;
	}

	public void setMobileDataEnabled(boolean b) {
		if (setMobileDataEnabled == null) {
			return;
		}
		try {
			setMobileDataEnabled.invoke(cm, b);
		} catch (Exception e) {
			Logger.w("Cannot call setMobileDataEnabled", e);
		}
	}

	/**
	 * Get the active mobiledata state by refection
	 * 
	 * @return state of mobiledata and true in case of errors
	 */
	public boolean getMobileDataEnabled() {
		if (getMobileDataEnabled == null) {
			return true;
		}
		try {
			Boolean b = (Boolean) getMobileDataEnabled.invoke(cm);
			return b.booleanValue();
		} catch (Exception e) {
			Logger.w("Cannot call getMobileDataEnabled", e);
			return true;
		}
	}

}
