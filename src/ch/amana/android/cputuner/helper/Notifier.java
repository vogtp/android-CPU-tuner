package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.widget.Toast;

public class Notifier {

	private static int curLevel = 1;

	public static void notify(Context context, String msg, int level) {
		if (level <= curLevel) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}

}
