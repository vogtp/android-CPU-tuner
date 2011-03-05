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
import ch.amana.android.cputuner.R;
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
		alertBuilder.setTitle(R.string.title_reboot);
		alertBuilder.setMessage(R.string.msg_reboot);
		alertBuilder.setCancelable(false);
		// alertBuilder.setPositiveButton("Manual reboot", null);
		alertBuilder.setNegativeButton(R.string.bu_system_reboot, new OnClickListener() {

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
		int title = R.string.title_uninstall_sys;
		int yesButton = R.string.bu_yes_uninstall_sys;
		int message = R.string.msg_uninstall_sys;
		if (toSystem) {
			title = R.string.title_install_sys;
			yesButton = R.string.bu_yes_title_install_sys;
			message = R.string.msg_install_sys;
		}
		alertBuilder.setTitle(title);
		alertBuilder.setMessage(message);
		alertBuilder.setCancelable(false);
		SystemAppHelper listener = new SystemAppHelper(ctx, toSystem);
		alertBuilder.setPositiveButton(yesButton, listener);
		alertBuilder.setNegativeButton(R.string.cancel, null);
		AlertDialog alert = alertBuilder.create();
		alert.show();
		return listener.isMoved();
	}

}
