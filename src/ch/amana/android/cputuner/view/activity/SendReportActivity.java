package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import ch.almana.android.backupDb2Xml.DataXmlExporter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CapabilityChecker;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.DeviceInformation;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class SendReportActivity extends Activity {
	private static final String FILE_KERNEL_CPUFREQ_CONFIG = "kernel_cpufreq_config.txt";
	private static final String FILE_DEVICE_INFO = "device_info.txt";
	static final String DIR_REPORT = "/report";
	private static final String FILE_GETPROP = "getProp.txt";
	private File path;
	private EditText etSubject;
	private EditText etMailBody;
	private Button buSendMail;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_report);

		etSubject = (EditText) findViewById(R.id.etSubject);
		etMailBody = (EditText) findViewById(R.id.etMailBody);
		buSendMail = (Button) findViewById(R.id.buSendMail);
		buSendMail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBugReport();
			}
		});
	}

	private void sendBugReport() {

		String mailSubject = etSubject.getText().toString();
		String mailBody = etMailBody.getText().toString();

		if (TextUtils.isEmpty(mailSubject) || TextUtils.isEmpty(mailBody)) {
			Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setTitle("E-mail report");
			alertBuilder.setMessage("Please enter a subject and some text describing your problem!");
			alertBuilder.setCancelable(false);
			alertBuilder.setPositiveButton(android.R.string.yes, null);
			alertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SendReportActivity.this.finish();

				}
			});
			AlertDialog alert = alertBuilder.create();
			alert.show();
			return;
		}

		File path = new File(Environment.getExternalStorageDirectory(), getPackageName());
		File file = new File(path, "report.zip");

		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		getDeviceInfo();
		getKernelInfo();

		sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "cputuner-help@lists.sourceforge.net" });
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "cpu tuner report: " + mailSubject);
		StringBuilder body = new StringBuilder();
		body.append(mailBody).append("\n\n");
		body.append('\n').append("------------------------------------------").append('\n');
		openLogFile(FILE_DEVICE_INFO);
		body.append("Android release: ").append(DeviceInformation.getAndroidRelease()).append('\n');
		body.append("Device model: ").append(DeviceInformation.getDeviceModel()).append('\n');
		body.append("Manufacturer: ").append(DeviceInformation.getManufacturer()).append('\n');
		body.append("Mod version: ").append(DeviceInformation.getModVersion()).append('\n');
		body.append("Developer ID: ").append(DeviceInformation.getRomManagerDeveloperId()).append('\n');
		body.append("Device nickname: ").append(DeviceInformation.getDeviceNick()).append('\n');
		body.append('\n').append("------------------------------------------").append('\n');
		body.append("CPU tuner version: ").append(getString(R.string.version)).append('\n');
		body.append("Language: ").append(Locale.getDefault().getLanguage()).append('\n');
		body.append("Userlevel: ").append(SettingsStorage.getInstance().getUserLevel()).append('\n');
		body.append("Beta mode: ").append(SettingsStorage.getInstance().isEnableBeta()).append('\n');
		body.append("Installed as system app: ").append(RootHandler.isSystemApp(this)).append('\n');
		body.append('\n').append("------------------------------------------").append('\n');
		CpuHandler cpuHandler = CpuHandler.getInstance();
		body.append("CPU governors: ").append(Arrays.toString(cpuHandler.getAvailCpuGov())).append('\n');
		body.append("CPU frequencies: ").append(Arrays.toString(cpuHandler.getAvailCpuFreq(true)));
		if (!cpuHandler.hasAvailCpuFreq()) {
			body.append(" (no available frequencies)");
		}
		body.append('\n');
		body.append("Min scaling frequency: ").append(cpuHandler.getMinCpuFreq()).append('\n');
		body.append("Max scaling frequency: ").append(cpuHandler.getMaxCpuFreq()).append('\n');
		body.append("Current governor: ").append(cpuHandler.getCurCpuGov()).append('\n');
		body.append("Current frequency: ").append(cpuHandler.getCurCpuFreq()).append('\n');
		body.append("Current power usage: ").append(BatteryHandler.getBatteryCurrentNow()).append('\n');
		body.append("Average power usage: ").append(BatteryHandler.getBatteryCurrentAverage()).append('\n');
		closeLogFile();
		body.append('\n').append("------------------------------------------").append('\n');
		body.append(CapabilityChecker.getInstance(this).toString());

		try {
			DB.OpenHelper oh = new OpenHelper(this);
			DataXmlExporter dm = new DataXmlExporter(oh.getWritableDatabase(), path.getAbsolutePath() + DIR_REPORT);
			try {
				dm.export(DB.DATABASE_NAME);
			} catch (IOException e) {
				Logger.w("Error exporting DB", e);
			}
		} catch (Throwable e) {
			Logger.e("Could not export DB", e);
			body.append("Could not export DB: ").append(e.getMessage()).append("\n");
		}

		sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			addFileToZip(zip, "output", FILE_DEVICE_INFO);
			addFileToZip(zip, "", CapabilityCheckerActivity.FILE_CAPABILITIESCHECK);
			addFileToZip(zip, "", FILE_GETPROP);
			addFileToZip(zip, "", FILE_KERNEL_CPUFREQ_CONFIG);
			addFileToZip(zip, "DB", DB.DATABASE_NAME + ".xml");
			addDirectoryToZip(zip, "cpufreq", new File(CpuHandler.CPU_DIR), true);
			addDirectoryToZip(zip, "battery", new File(BatteryHandler.BATTERY_DIR), true);
			zip.flush();
			zip.close();

			Uri uri = Uri.fromFile(file);
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			sendIntent.setType("application/zip");
			startActivity(sendIntent);
			finish();
		} catch (IOException e) {
			Logger.w("Error zipping attachments", e);
		}
	}

	private void addDirectoryToZip(ZipOutputStream zip, String prefix, File cpuFreqDir, boolean traverse) {
		File[] cpufreqFiles = cpuFreqDir.listFiles();
		if (cpufreqFiles == null) {
			return;
		}
		for (int i = 0; i < cpufreqFiles.length; i++) {
			if (traverse && cpufreqFiles[i].isDirectory()) {
				addDirectoryToZip(zip, prefix + "/" + cpufreqFiles[i].getName(), cpufreqFiles[i], false);
			} else {
				addFileToZip(zip, prefix, cpufreqFiles[i]);
			}
		}
	}

	private void getKernelInfo() {
		openLogFile(FILE_KERNEL_CPUFREQ_CONFIG);
		RootHandler.execute("gunzip< /proc/config.gz | grep CONFIG_CPU_FREQ");
		closeLogFile();
	}

	private void addFileToZip(ZipOutputStream zip, String zipDir, String fileName) {
		addFileToZip(zip, zipDir, getFilePath(fileName));
	}

	private void addFileToZip(ZipOutputStream zip, String zipDir, File file) {
		try {
			zip.putNextEntry(new ZipEntry(zipDir + "/" + file.getName()));
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[1024];

			int len;
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			zip.closeEntry();
			in.close();
		} catch (IOException e) {
			Logger.w("Error exporting adding file to zip", e);
		}

	}

	private void closeLogFile() {
		RootHandler.clearLogLocation();
	}

	private void openLogFile(String fileName) {

		RootHandler.setLogLocation(getFilePath(fileName));
	}

	private File getFilePath(String fileName) {
		if (path == null) {
			path = new File(Environment.getExternalStorageDirectory(), getPackageName() + DIR_REPORT);
			if (!path.exists()) {
				path.mkdirs();
			}
		}
		return new File(path, fileName);
	}

	private void getDeviceInfo() {
		openLogFile(FILE_GETPROP);
		RootHandler.execute("getprop");
		closeLogFile();
	}

}
