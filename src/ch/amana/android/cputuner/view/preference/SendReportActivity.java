package ch.amana.android.cputuner.view.preference;

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
import ch.almana.android.importexportdb.exporter.DataExporter;
import ch.almana.android.importexportdb.exporter.DataJsonExporter;
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

	public static final String EXTRAS_SEND_DIRECTLY = "sendDirectly";

	private static final String FILE_KERNEL_CPUFREQ_CONFIG = "kernel_cpufreq_config.txt";
	private static final String FILE_DEVICE_INFO = "device_info.txt";
	static final String DIR_REPORT = "/report";
	private static final String FILE_GETPROP = "getProp.txt";
	private File path;
	private EditText etSubject;
	private EditText etMailBody;
	private Button buSendMail;
	private boolean sendDirectly;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_report);

		Bundle extras = getIntent().getExtras();
		if (extras.containsKey(EXTRAS_SEND_DIRECTLY)) {
			sendDirectly = extras.getBoolean(EXTRAS_SEND_DIRECTLY);
		} else {
			sendDirectly = false;
		}

		etSubject = (EditText) findViewById(R.id.etSubject);
		etMailBody = (EditText) findViewById(R.id.etMailBody);
		buSendMail = (Button) findViewById(R.id.buSendMail);
		buSendMail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBugReport();
			}
		});

		if (sendDirectly) {
			sendBugReport();
		}
	}

	private void sendBugReport() {
		Logger.v("Send report: START");
		String mailSubject = etSubject.getText().toString();
		String mailBody = etMailBody.getText().toString();

		if (!sendDirectly && (TextUtils.isEmpty(mailSubject) || TextUtils.isEmpty(mailBody))) {
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

		Logger.v("Send report: getting paths");
		File path = new File(Environment.getExternalStorageDirectory(), getPackageName());
		File file = new File(path, "report.zip");

		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		Logger.v("Send report: getDeviceInfo");
		getDeviceInfo();
		Logger.v("Send report: getKernelInfo");
		getKernelInfo();
		Logger.v("Send report: CpuHandler.getInstance");
		CpuHandler cpuHandler = CpuHandler.getInstance();

		// sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]
		// { "cputuner-help@lists.sourceforge.net" });
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "cpu tuner report: " + mailSubject);
		Logger.v("Send report: appending custom text");
		StringBuilder body = new StringBuilder();
		body.append(mailBody).append("\n\n");
		body.append('\n').append("------------------------------------------").append('\n');
		Logger.v("Send report: getting LogFile");
		openLogFile(FILE_DEVICE_INFO);
		Logger.v("Send report: getting Android release");
		body.append("Android release: ").append(DeviceInformation.getAndroidRelease()).append('\n');
		Logger.v("Send report: getting Device model");
		body.append("Device model: ").append(DeviceInformation.getDeviceModel()).append('\n');
		Logger.v("Send report: getting Manufacturer");
		body.append("Manufacturer: ").append(DeviceInformation.getManufacturer()).append('\n');
		Logger.v("Send report: getting Mod version");
		body.append("Mod version: Send report").append(DeviceInformation.getModVersion()).append('\n');
		Logger.v("Send report: getting Developer ID");
		body.append("Developer ID: Send report").append(DeviceInformation.getRomManagerDeveloperId()).append('\n');
		Logger.v("Send report: getting Device nickname");
		body.append("Device nickname: Send report").append(DeviceInformation.getDeviceNick()).append('\n');
		Logger.v("Send report: getting PU tuner version");
		body.append('\n').append("------------------------------------------").append('\n');
		body.append("CPU tuner version: ").append(getString(R.string.version)).append('\n');
		Logger.v("Send report: getting Language");
		body.append("Language: ").append(Locale.getDefault().getLanguage()).append('\n');
		Logger.v("Send report: getting Userlevel");
		body.append("Userlevel: Send report").append(SettingsStorage.getInstance().getUserLevel()).append('\n');
		Logger.v("Send report: getting Beta mode");
		body.append("Beta mode: ").append(SettingsStorage.getInstance().isEnableBeta()).append('\n');
		Logger.v("Send report: getting Multicore");
		body.append("Multicore: ").append(cpuHandler.getClass().getName()).append('\n');
		Logger.v("Send report: getting system app");
		body.append("Installed as system app: ").append(RootHandler.isSystemApp(this)).append('\n');
		body.append('\n').append("------------------------------------------").append('\n');
		Logger.v("Send report: getting CPU governors");
		body.append("CPU governors: ").append(Arrays.toString(cpuHandler.getAvailCpuGov())).append('\n');
		Logger.v("Send report: getting CPU frequencies");
		body.append("CPU frequencies: ").append(Arrays.toString(cpuHandler.getAvailCpuFreq(true)));
		if (!cpuHandler.hasAvailCpuFreq()) {
			body.append(" (no available frequencies)");
		}
		body.append('\n');
		Logger.v("Send report: getting Min scaling frequency");
		body.append("Min scaling frequency: ").append(cpuHandler.getMinCpuFreq()).append('\n');
		Logger.v("Send report: getting Max scaling frequency");
		body.append("Max scaling frequency: ").append(cpuHandler.getMaxCpuFreq()).append('\n');
		Logger.v("Send report: getting Current governor");
		body.append("Current governor: ").append(cpuHandler.getCurCpuGov()).append('\n');
		Logger.v("Send report: getting Current frequency");
		body.append("Current frequency: ").append(cpuHandler.getCurCpuFreq()).append('\n');
		Logger.v("Send report: getting BatteryHandler.getInstance()");
		BatteryHandler batteryHandler = BatteryHandler.getInstance();
		Logger.v("Send report: getting Current power usage");
		body.append("Current power usage: ").append(batteryHandler.getBatteryCurrentNow()).append('\n');
		Logger.v("Send report: getting Average power usage");
		body.append("Average power usage: ").append(batteryHandler.getBatteryCurrentAverage()).append('\n');
		closeLogFile();
		body.append('\n').append("------------------------------------------").append('\n');
		body.append(CapabilityChecker.getInstance(this).toString());

		try {
			DB.OpenHelper oh = new OpenHelper(this);
			DataExporter dm = new DataJsonExporter(oh.getWritableDatabase(), new File(path, DIR_REPORT));
			Logger.v("Send report: exporting DB");
			try {
				dm.export(DB.DATABASE_NAME);
			} catch (Exception e) {
				Logger.w("Error exporting DB", e);
			}
		} catch (Throwable e) {
			Logger.e("Could not export DB", e);
			body.append("Could not export DB: ").append(e.getMessage()).append("\n");
		}

		sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());

		try {
			Logger.v("Send report: appending file FILE_DEVICE_INFO");
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			addFileToZip(zip, "output", FILE_DEVICE_INFO);
			Logger.v("Send report: appending file FILE_CAPABILITIESCHECK");
			addFileToZip(zip, "", CapabilityCheckerActivity.FILE_CAPABILITIESCHECK);
			Logger.v("Send report: appending file FILE_GETPROP");
			addFileToZip(zip, "", FILE_GETPROP);
			Logger.v("Send report: appending file FILE_KERNEL_CPUFREQ_CONFIG");
			addFileToZip(zip, "", FILE_KERNEL_CPUFREQ_CONFIG);
			Logger.v("Send report: appending file DB json");
			addFileToZip(zip, "DB", DB.DATABASE_NAME + ".json");
			Logger.v("Send report: appending file cpufreq");
			addDirectoryToZip(zip, "cpufreq", new File(CpuHandler.CPU_BASE_DIR), 5);
			Logger.v("Send report: appending file battery");
			addDirectoryToZip(zip, "battery", new File(BatteryHandler.BATTERY_DIR), 1);
			zip.flush();
			zip.close();

			Uri uri = Uri.fromFile(file);
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			sendIntent.setType("application/zip");
			Logger.v("Send report: sending");
			startActivity(sendIntent);
			finish();
			Logger.v("Send report: FINISHED");
		} catch (IOException e) {
			Logger.w("Error zipping attachments", e);
		}
	}

	private void addDirectoryToZip(ZipOutputStream zip, String prefix, File cpuFreqDir, int depth) {
		if (depth < 0) {
			return;
		}
		File[] cpufreqFiles = cpuFreqDir.listFiles();
		if (cpufreqFiles == null) {
			return;
		}
		for (int i = 0; i < cpufreqFiles.length; i++) {
			if (cpufreqFiles[i].isDirectory()) {
				addDirectoryToZip(zip, prefix + "/" + cpufreqFiles[i].getName(), cpufreqFiles[i], depth - 1);
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
		if (file == null || !file.exists()) {
			return;
		}
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
			Logger.w("Error exporting adding file to zip " + file.getAbsolutePath(), e);
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
