package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import ch.almana.android.importexportdb.ExportConfig;
import ch.almana.android.importexportdb.ExportConfig.ExportType;
import ch.almana.android.importexportdb.exporter.DataExporter;
import ch.almana.android.importexportdb.exporter.DataJsonExporter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CapabilityChecker;
import ch.amana.android.cputuner.helper.CapabilityChecker.CheckResult;
import ch.amana.android.cputuner.helper.CapabilityChecker.GovernorResult;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.DeviceInformation;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuTunerOpenHelper;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class CapabilityCheckerActivity extends Activity {

	private static final String FILE_KERNEL_CPUFREQ_CONFIG = "kernel_cpufreq_config.txt";
	private static final String FILE_DEVICE_INFO = "device_info.txt";
	private static final String DIR_REPORT = "/report";
	private static final String FILE_GETPROP = "getProp.txt";

	public static final String FILE_CAPABILITIESCHECK = "capabilitiy_check.txt";
	private CapabilityChecker checker;
	private TextView tvSummary;
	private TableLayout tlCapabilities;
	private TextView tvDeviceInfo;
	private TextView tvMailMessage;
	private File path;

	private class GovernorResultRow extends TableRow {

		private final Context ctx;
		private final GovernorResult res;

		public GovernorResultRow(Context ctx, GovernorResult res) {
			super(ctx);
			this.ctx = ctx;
			this.res = res;
			addView(getTextView(res.governor + ": "));
			addView(getTextView(res));
		}

		private TextView getTextView(GovernorResult res) {
			CheckResult cr = res.getOverallIssue();
			TextView tv;
			switch (cr) {
			case SUCCESS:
				tv = getTextView(R.string.msg_fully_working);
				tv.setTextColor(Color.GREEN);
				return tv;
			case WORKING:
				return getTextView(R.string.msg_working);
			case FAILURE:
				tv = getTextView(R.string.msg_not_working);
				tv.setTextColor(Color.RED);
				return tv;
			default:
				tv = getTextView(R.string.msg_has_issues);
				tv.setTextColor(Color.YELLOW);
				return tv;
			}
		}

		private TextView getTextView(int resId) {
			return getTextView(getString(resId));
		}

		private TextView getTextView(String text) {
			TextView tv = new TextView(ctx);
			tv.setPadding(0, 0, 20, 0);
			tv.setText(text);
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Toast.makeText(ctx, res.toString(), Toast.LENGTH_LONG).show();
				}
			});
			return tv;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.capability_checker);
		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.prefCapabilities);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
			cputunerActionBar.setHomeAction(new ActionBar.Action() {
				@Override
				public void performAction(View view) {
					onBackPressed();
				}

				@Override
				public int getDrawable() {
					return R.drawable.cputuner_back;
				}
			});
			cputunerActionBar.setTitle(getString(R.string.prefCapabilities));
			cputunerActionBar.addAction(new Action() {
				@Override
				public void performAction(View view) {
					sendMail();
				}

				@Override
				public int getDrawable() {
					return android.R.drawable.ic_dialog_email;
				}
			});
		}
		openLogFile(FILE_CAPABILITIESCHECK);
		checker = CapabilityChecker.getCapabilityChecker(this);

		closeLogFile();

		tvSummary = (TextView) findViewById(R.id.tvSummary);
		tvMailMessage = (TextView) findViewById(R.id.tvMailMessage);
		tvDeviceInfo = (TextView) findViewById(R.id.tvDeviceInfo);
		tlCapabilities = (TableLayout) findViewById(R.id.tlCapabilities);

		tvDeviceInfo.setText(R.string.msg_capcheck_result_more_info);

	}

	public void dispalyChecks() {
		tvSummary.setText(checker.getSummary(this));
		switch (checker.hasIssues()) {
		case SUCCESS:
		case NOT_CHECKED:
		case DOES_NOT_APPLY:
		case CANNOT_CHECK:
			tvSummary.setTextColor(Color.LTGRAY);
			break;
		case WORKING:
			tvSummary.setTextColor(Color.YELLOW);
			break;
		case FAILURE:
			tvSummary.setTextColor(Color.RED);
			break;
		}

		Collection<GovernorResult> governorsCheckResults = checker.getGovernorsCheckResults();

		addTableRow("Root access", checker.isRooted());
		for (GovernorResult res : governorsCheckResults) {
			tlCapabilities.addView(new GovernorResultRow(this, res));
		}

		String mailMessage = getString(R.string.msg_premail_no_issues);
		if (!RootHandler.isRoot()) {
			mailMessage = getString(R.string.msg_premail_no_root);
		} else if (checker.hasIssues() == CheckResult.FAILURE) {
			mailMessage = getString(R.string.msg_premail_issues);
			if (!DeviceInformation.getRomManagerDeveloperId().toLowerCase().contains("cyanogenmod")) {
				mailMessage += getString(R.string.msg_premail_issues_cm);
			}
			mailMessage += "\n";
		} else if (checker.hasIssues() == CheckResult.WORKING) {
			mailMessage = getString(R.string.msg_premail_working);
		}

		tvMailMessage.setText(mailMessage);
	}

	private void addTableRow(String check, boolean working) {
		TableRow tr = new TableRow(this);
		tr.addView(getTextView(check + ": "));
		tr.addView(getTextView(working));
		tlCapabilities.addView(tr);
	}

	private TextView getTextView(boolean b) {
		TextView tv = getTextView(getString(b ? R.string.msg_fully_working : R.string.msg_not_working));
		if (!b) {
			tv.setTextColor(Color.RED);
		}
		return tv;
	}

	private TextView getTextView(String text) {
		TextView tv = new TextView(this);
		tv.setPadding(0, 0, 20, 0);
		tv.setText(text);
		return tv;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.capabilitycheck_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menuItemSendMail:
			sendMail();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_CAPABILITY_CHECK)) {
				return true;
			}
		}
		return false;
	}

	private void sendMail() {
		Logger.v("Send report: START");
		//		String mailSubject = etSubject.getText().toString();
		//		String mailBody = etMailBody.getText().toString();

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
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "cpu tuner report: ");
		Logger.v("Send report: appending custom text");
		StringBuilder body = new StringBuilder("\n\n");
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
		body.append("Mod version: ").append(DeviceInformation.getModVersion()).append('\n');
		Logger.v("Send report: getting Developer ID");
		body.append("Developer ID: ").append(DeviceInformation.getRomManagerDeveloperId()).append('\n');
		Logger.v("Send report: getting Device nickname");
		body.append("Device nickname: ").append(DeviceInformation.getDeviceNick()).append('\n');
		Logger.v("Send report: getting PU tuner version");
		body.append('\n').append("------------------------------------------").append('\n');
		body.append("CPU tuner version: ").append(getString(R.string.version)).append('\n');
		Logger.v("Send report: getting Language");
		body.append("Language: ").append(Locale.getDefault().getLanguage()).append('\n');
		Logger.v("Send report: getting Userlevel");
		body.append("Userlevel: ").append(SettingsStorage.getInstance().getUserLevel()).append('\n');
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
		body.append(checker.toString());

		try {
			DB.CpuTunerOpenHelper oh = new CpuTunerOpenHelper(this);
			ExportConfig config = new ExportConfig(oh.getWritableDatabase(), DB.DATABASE_NAME, new File(path, DIR_REPORT), ExportType.JSON);
			config.setExcludeTable(DB.SwitchLogDB.TABLE_NAME);
			DataExporter dm = new DataJsonExporter(oh.getWritableDatabase(), new File(path, DIR_REPORT));
			Logger.v("Send report: exporting DB");
			try {
				dm.export(config);
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

	private void getDeviceInfo() {
		openLogFile(FILE_GETPROP);
		RootHandler.execute("getprop");
		closeLogFile();
	}

}
