package ch.amana.android.cputuner.helper;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;
import ch.amana.android.cputuner.hw.RootHandler;

public class SystemAppHelper implements OnClickListener {

	private boolean moved = false;
	private final boolean toSystem;
	private final Context ctx;

	public SystemAppHelper(Context ctx, boolean toSystem) {
		super();
		this.ctx = ctx;
		this.toSystem = toSystem;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		File systemDir = Environment.getRootDirectory();
		File dataDir = Environment.getDataDirectory();
		if (toSystem) {
			moved = moveApp(dataDir, systemDir);
		} else {
			moved = moveApp(systemDir, dataDir);
		}
	}

	private boolean checkInstallLocation(File loc) {
		return true;
		// does not work for Environment.getDataDirectory()
		// String[] files = RootHandler.findAppPath(ctx, loc);
		// return files != null || files.length > 0;
	}

	private boolean moveApp(File from, File to) {
		if (checkInstallLocation(from)) {
			RootHandler.execute("mount -o remount,rw " + Environment.getRootDirectory());
			RootHandler.execute("mv " + from + "/app/" + ctx.getPackageName() + "* " + to + "/app");

			if (checkInstallLocation(to)) {
				Logger.w("Successfully moved package from " + from + " to " + to + " rebooting now");
				reboot();
				return true;
			} else {
				Logger.w("Could not move package from " + from + " to "
						+ to);
			}
		} else {
			Logger.w("Did not find cputuner apk in " + from +
					" to moving to " + to);
		}
		Toast.makeText(ctx, "Could not install apk", Toast.LENGTH_LONG).show();
		return false;
	}

	private void reboot() {
		try {
			PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			pm.reboot(null);
		} catch (Throwable e) {
			Logger.w("Cannot do a PowerManager reboot.", e);
			rebootHard();
		}
	}

	private void rebootHard() {
		Builder alertBuilder = new AlertDialog.Builder(ctx);
		alertBuilder.setTitle("Reboot");
		alertBuilder
				.setMessage("The automatic (save) reboot failed.  Please do a manualy reboot by long pressing the power button, otherwise cpu tuner will not work.");
		alertBuilder.setCancelable(false);
		// alertBuilder.setPositiveButton("Manual reboot", null);
		alertBuilder.setNegativeButton("System reboot (not recomentded)", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				RootHandler.execute("reboot");

			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	public boolean isMoved() {
		return moved;
	}

	public static boolean install(Context ctx, boolean toSystem) {
		Builder alertBuilder = new AlertDialog.Builder(ctx);
		String title = "Unnstall cpu tuner as system app";
		String yesButton = "Unnstall and reboot";
		String message = "Requires a reboot, by pressing 'Unnstall and reboot' the device will reboot.";
		if (toSystem) {
			title = "Install cpu tuner as system app";
			yesButton = "Install and reboot";
			message = "Requires a reboot, by pressing 'install and reboot' the device will reboot.";
		}
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(message);
		alertBuilder.setCancelable(false);
		SystemAppHelper listener = new SystemAppHelper(ctx, toSystem);
		alertBuilder.setPositiveButton(yesButton, listener);
		alertBuilder.setNegativeButton("Cancel", null);
		AlertDialog alert = alertBuilder.create();
		alert.show();
		return listener.isMoved();
	}

}
