package ch.amana.android.cputuner.background;

import android.os.Handler;
import android.os.Looper;
import ch.amana.android.cputuner.helper.Logger;

public class BackgroundThread extends Thread {

	private static final int[] lock = new int[1];

	private final Handler handler = new Handler();

	private static BackgroundThread instance;

	public static BackgroundThread getInstance() {
		synchronized (lock) {
			if (instance == null || instance.handler == null) {
				instance = new BackgroundThread();
				instance.start();
			}
		}
		return instance;
	}

	public BackgroundThread() {
		super("cpu tuner background");
	}

	@Override
	public void run() {
		try {
			Logger.i("Starting background thread");
			Looper.prepare();
			Looper.loop();
			Logger.i("Stopping background thread");
		} catch (Throwable e) {
			//			handler = null;
			Logger.e("Background thread crashed with execption", e);
			Logger.logStacktrace("Background thread died", e);
		}
	}

	public void queue(Runnable run) {
		handler.post(run);
	}

	public void requestStop() {
		if (instance.isAlive()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Logger.i("Requesting stop of background thread");
					Looper.myLooper().quit();
				}
			});
		}
		instance = null;
	}

}
