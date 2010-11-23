package ch.amana.android.cputuner.view.activity;

import java.io.File;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CapabilityChecker;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.RootHandler;

public class CapabilityCheckerActivity extends Activity {

	private static final String FILE_GETPROP = "getProp.txt";
	public static final String EXTRA_RECHEK = "extra_recheck";
	private static final String FILE_CAPABILITIESCHECK = "capabilitiy_check.txt";
	private CapabilityChecker checker;
	private TextView tvSummary;
	private TableLayout tlCapabilities;
	private CheckBox cbAcknowledge;
	private TextView tvDeviceInfo;

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

		getDevieInfo();
	}

	private void closeLogFile() {
		RootHandler.clearLogLocation();
	}

	private void openLogFile(String fileName) {

		RootHandler.setLogLocation(new File(getCacheDir(), fileName));
	}

	private void getDevieInfo() {
		StringBuffer info = new StringBuffer("Device Information:\n");

		openLogFile(FILE_GETPROP);
		RootHandler.execute("getprop");
		closeLogFile();

		// info.append("Phone type: ").append(tm.getPhoneType()).append("\n");

		// info.append(": ").append().append("\n");

		tvDeviceInfo.setText(info.toString());
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
}
