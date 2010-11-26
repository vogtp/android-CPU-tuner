package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.OpenHelper;

public class CapabilityCheckerActivity extends Activity {

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		openLogFile(FILE_CAPABILITIESCHECK);
		checker = CapabilityChecker.getInstance(getIntent().getBooleanExtra(EXTRA_RECHEK, false));
		closeLogFile();
		setContentView(R.layout.capability_checker);

		tvSummary = (TextView) findViewById(R.id.tvSummary);
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
		// StringBuffer info = new StringBuffer("Device Information:\n");

		openLogFile(FILE_GETPROP);
		RootHandler.execute("getprop");
		closeLogFile();

		// info.append("Phone type: ").append(tm.getPhoneType()).append("\n");

		// info.append(": ").append().append("\n");

		// tvDeviceInfo.setText(info.toString());
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

		DB.OpenHelper oh = new OpenHelper(this);
		DataXmlExporter dm = new DataXmlExporter(oh.getWritableDatabase(), path.getAbsolutePath() + DIR_REPORT);
		try {
			dm.export(DB.Trigger.TABLE_NAME);
			dm.export(DB.CpuProfile.TABLE_NAME);
		} catch (IOException e) {
			Log.w(Logger.TAG, "Error exporting DB", e);
		}

		sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "patrick.vogt.pv@gmail.com" });
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "cpu tuner repport");
		StringBuilder body = new StringBuilder("Please add some additional information.\nEmpty e-mail will be ignored...\n\n");
		body.append(CapabilityChecker.getInstance().toString());

		sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());

		try {
			ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			addFileToZip(zip, FILE_CAPABILITIESCHECK);
			addFileToZip(zip, FILE_GETPROP);
			addFileToZip(zip, DB.Trigger.TABLE_NAME + ".xml");
			addFileToZip(zip, DB.CpuProfile.TABLE_NAME + ".xml");
			zip.flush();
			zip.close();

			Uri uri = Uri.fromFile(file);
			sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			sendIntent.setType("application/zip");
			startActivity(sendIntent);
		} catch (IOException e) {
			Log.w(Logger.TAG, "Error zipping attachments", e);
		}
	}

	private void addFileToZip(ZipOutputStream zip, String fileName) {
		try {
			zip.putNextEntry(new ZipEntry(fileName));
			File filePath = getFilePath(fileName);
			FileInputStream in = new FileInputStream(filePath);
			byte[] buf = new byte[1024];

			int len;
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			zip.closeEntry();
			in.close();
		} catch (IOException e) {
			Log.w(Logger.TAG, "Error exporting adding file to zip", e);
		}

	}
}
