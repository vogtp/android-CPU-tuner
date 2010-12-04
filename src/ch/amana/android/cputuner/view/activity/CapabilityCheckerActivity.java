package ch.amana.android.cputuner.view.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CapabilityChecker;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.DeviceInformation;
import ch.amana.android.cputuner.hw.RootHandler;

public class CapabilityCheckerActivity extends Activity {

	public static final String EXTRA_RECHEK = "extra_recheck";
	public static final String FILE_CAPABILITIESCHECK = "capabilitiy_check.txt";
	private CapabilityChecker checker;
	private TextView tvSummary;
	private TableLayout tlCapabilities;
	private CheckBox cbAcknowledge;
	private TextView tvDeviceInfo;
	private Button buSendBugreport;
	private TextView tvMailMessage;
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
		tvMailMessage = (TextView) findViewById(R.id.tvMailMessage);
		tvDeviceInfo = (TextView) findViewById(R.id.tvDeviceInfo);
		tlCapabilities = (TableLayout) findViewById(R.id.tlCapabilities);
		cbAcknowledge = (CheckBox) findViewById(R.id.cbAcknowledge);
		buSendBugreport = (Button) findViewById(R.id.buSendBugreport);

		buSendBugreport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(CapabilityCheckerActivity.this, SendReportActivity.class));
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
					"2) Flash a new ROM\n";
			if (!DeviceInformation.getRomManagerDeveloperId().toLowerCase().contains("cyanogenmod")) {
				mailMessage += "You might want to have a look at http://wiki.cyanogenmod.com they do very nice ROMS\n\n" +
						"Please do not write e-mails unless you think there is something wrong with the check or the app.\n";
			}
			mailMessage += "\n";
		}

		tvMailMessage.setText(mailMessage);
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

	private void closeLogFile() {
		RootHandler.clearLogLocation();
	}

	private void openLogFile(String fileName) {

		RootHandler.setLogLocation(getFilePath(fileName));
	}

	private File getFilePath(String fileName) {
		if (path == null) {
			path = new File(Environment.getExternalStorageDirectory(), getPackageName() + SendReportActivity.DIR_REPORT);
			if (!path.exists()) {
				path.mkdirs();
			}
		}
		return new File(path, fileName);
	}

}
