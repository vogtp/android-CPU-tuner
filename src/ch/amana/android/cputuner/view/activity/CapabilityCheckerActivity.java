package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

public class CapabilityCheckerActivity extends Activity {

	private static final String FILE_KERNEL_CPUFREQ_CONFIG = "kernel_cpufreq_config.txt";
	private static final String FILE_DEVICE_INFO = "device_info.txt";
	private static final String DIR_REPORT = "/report";
	private static final String FILE_GETPROP = "getProp.txt";
	public static final String EXTRA_RECHEK = "extra_recheck";
	private static final String FILE_CAPABILITIESCHECK = "capabilitiy_check.txt";
	private CapabilityChecker checker;
	private TextView tvSummary;
	private TableLayout tlCapabilities;
	private CheckBox cbAcknowledge;
	private TextView tvDeviceInfo;
	private Button buSendBugreport;
	private File path;
	private TextView tvMailMessage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		openLogFile(FILE_CAPABILITIESCHECK);
		checker = CapabilityChecker.getInstance(getIntent().getBooleanExtra(EXTRA_RECHEK, false));
		closeLogFile();
		setContentView(R.layout.capability_checker);

		tvSummary = (TextView) findViewById(R.id.tvSummary);
		tvMailMessage = (TextView) findViewById(R.id.tvMailMessage);
		tvDeviceInfo = (TextView) findViewById(R.id.tvDeviceInfo);
		tlCapabilities = (TableLayout) findViewById(R.id.tlCapabilities);
		cbAcknowledge = (CheckBox) findViewById(R.id.cbAcknowledge);
		buSendBugreport = (Button) findViewById(R.id.buSendBugreport);

		buSendBugreport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBugReport();
			}
		});

		cbAcknowledge.setChecked(SettingsStorage.getInstance().isDisableDisplayIssues());
		cbAcknowledge.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SettingsStorage.getInstance().setDisableDisplayIssues(isChecked);
			}
		});

		tvSummary.setText(checker.getSummary());
		if (checker.hasIssues()) {
			tvSummary.setTextColor(Color.RED);
		} else {
			tvSummary.setTextColor(Color.LTGRAY);
		}
		addTableRow("Governor", checker.isReadGovernor(), checker.isWriteGovernor());
		addTableRow("Min frequency", checker.isReadMinFreq(), checker.isWriteMinFreq());
		addTableRow("Max frequency", checker.isReadMaxFreq(), checker.isWriteMaxFreq());
		addTableRow("User frequency", checker.isReadUserCpuFreq(), checker.isWriteUserCpuFreq());
		// addTableRow("", checker.isRead, checker.isWrite);

		String mailMessage = "No issues have been found...\n" +
				"If you are pleased and want to give feedback:\n" +
				"Comments and good ratings on the market are highly appreciated...\n\n" +
				"Please do not write empty e-mails!";
		if (!RootHandler.isRoot()) {
			mailMessage = "Your device is not rooted!\n" +
					"Cpu tuner cannot do anything!\n\n" +
					"Please do not write e-mails unless you think there is something wrong with the check or the app.\n\n" +
					"Please do not write empty e-mails!";
		} else if (CapabilityChecker.getInstance().hasIssues()) {
			mailMessage = "You configuration has issues...\n" +
					"This is most likely due to the kernel/ROM." +
					"You have two possibilities:\n\n" +
					"1) Flash a new kernel\n\n" +
					"2) Flash a new ROM\n" +
					"You might want to have a look at http://wiki.cyanogenmod.com they do very nice ROMS\n\n" +
					"Please do not write e-mails unless you think there is something wrong with the check or the app.\n\n" +
					"Please do not write empty e-mails!";
		}

		tvMailMessage.setText(mailMessage);
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

	private void addTableRow(String title, boolean read, boolean write) {

		TableRow tr = new TableRow(this);
		tr.addView(getTextView(title));
		tr.addView(getTextView(read));
		tr.addView(getTextView(write));
		tlCapabilities.addView(tr);
	}

	private TextView getTextView(boolean b) {
		TextView tv = getTextView(b ? "Yes" : "No");
		if (!b) {
			tv.setTextColor(Color.RED);
		}
		return tv;
	}

	private TextView getTextView(String text) {
		TextView tv = new TextView(this);
		tv.setPadding(0, 0, 10, 0);
		tv.setText(text);
		return tv;
	}

	private void sendBugReport() {

		File path = new File(Environment.getExternalStorageDirectory(), getPackageName());
		File file = new File(path, "report.zip");

		Intent sendIntent = new Intent(Intent.ACTION_SEND);

		getDeviceInfo();
		getKernelInfo();

		DB.OpenHelper oh = new OpenHelper(this);
		DataXmlExporter dm = new DataXmlExporter(oh.getWritableDatabase(), path.getAbsolutePath() + DIR_REPORT);
		try {
			dm.export(DB.Trigger.TABLE_NAME);
			dm.export(DB.CpuProfile.TABLE_NAME);
		} catch (IOException e) {
			Logger.w("Error exporting DB", e);
		}

		sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "patrick.vogt.pv@gmail.com" });
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "cpu tuner report");
		StringBuilder body = new StringBuilder("Please add some additional information.\nEmpty e-mail will be ignored...\n\n");
		openLogFile(FILE_DEVICE_INFO);
		body.append("Device model: ").append(DeviceInformation.getDeviceModel()).append('\n');
		body.append("Manufacturer: ").append(DeviceInformation.getManufacturer()).append('\n');
		body.append("Mod version: ").append(DeviceInformation.getModVersion()).append('\n');
		body.append("Developer ID: ").append(DeviceInformation.getRomManagerDeveloperId()).append('\n');
		body.append("Device nickname: ").append(DeviceInformation.getDeviceNick()).append('\n');
		body.append('\n').append("------------------------------------------").append('\n');
		body.append("CPU governors: ").append(Arrays.toString(CpuHandler.getInstance().getAvailCpuGov())).append('\n');
		body.append("CPU frequencies: ").append(Arrays.toString(CpuHandler.getInstance().getAvailCpuFreq())).append('\n');
		closeLogFile();
		body.append('\n').append("------------------------------------------").append('\n');
		body.append(CapabilityChecker.getInstance().toString());

		sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			addFileToZip(zip, "output", FILE_DEVICE_INFO);
			addFileToZip(zip, "", FILE_CAPABILITIESCHECK);
			addFileToZip(zip, "", FILE_GETPROP);
			addFileToZip(zip, "", FILE_KERNEL_CPUFREQ_CONFIG);
			addFileToZip(zip, "DB", DB.Trigger.TABLE_NAME + ".xml");
			addFileToZip(zip, "DB", DB.CpuProfile.TABLE_NAME + ".xml");
			addDirectoryToZip(zip, "cpufreq", new File(CpuHandler.CPU_DIR), true);
			addDirectoryToZip(zip, "battery", new File(BatteryHandler.BATTERY_DIR), true);
			zip.flush();
			zip.close();

			Uri uri = Uri.fromFile(file);
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			sendIntent.setType("application/zip");
			startActivity(sendIntent);
		} catch (IOException e) {
			Logger.w("Error zipping attachments", e);
		}
	}

	private void addDirectoryToZip(ZipOutputStream zip, String prefix, File cpuFreqDir, boolean traverse) {
		File[] cpufreqFiles = cpuFreqDir.listFiles();
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
}
