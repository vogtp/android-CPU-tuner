package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CapabilityChecker;
import ch.amana.android.cputuner.helper.CapabilityChecker.CheckResult;
import ch.amana.android.cputuner.helper.CapabilityChecker.GovernorResult;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.DeviceInformation;
import ch.amana.android.cputuner.hw.RootHandler;

public class CapabilityCheckerActivity extends Activity {

	private static final String NOT_WORKING = "Not working";
	private static final String WORKING = "Working";
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
	private Button buFindFrequencies;

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
				return getTextView(WORKING);
			case FAILURE:
				tv = getTextView(NOT_WORKING);
				tv.setTextColor(Color.RED);
				return tv;
			default:
				tv = getTextView("Has issues");
				tv.setTextColor(Color.YELLOW);
				return tv;
			}
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

		openLogFile(FILE_CAPABILITIESCHECK);
		checker = CapabilityChecker.getInstance(this, getIntent().getBooleanExtra(EXTRA_RECHEK, false));

		closeLogFile();
		setContentView(R.layout.capability_checker);

		tvSummary = (TextView) findViewById(R.id.tvSummary);
		tvMailMessage = (TextView) findViewById(R.id.tvMailMessage);
		tvDeviceInfo = (TextView) findViewById(R.id.tvDeviceInfo);
		tlCapabilities = (TableLayout) findViewById(R.id.tlCapabilities);
		cbAcknowledge = (CheckBox) findViewById(R.id.cbAcknowledge);
		buSendBugreport = (Button) findViewById(R.id.buSendBugreport);
		buFindFrequencies = (Button) findViewById(R.id.buFindFrequencies);

		tvDeviceInfo.setText("(Tap result for more information.)");

		final SettingsStorage settings = SettingsStorage.getInstance();
		buFindFrequencies.setEnabled(settings.isEnableBeta());
		buFindFrequencies.setVisibility(View.INVISIBLE);
		buFindFrequencies.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(CapabilityCheckerActivity.this, FindFrequenciesActivity.class));
			}
		});

		buSendBugreport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(CapabilityCheckerActivity.this, SendReportActivity.class));
			}
		});

		cbAcknowledge.setChecked(settings.isDisableDisplayIssues());
		cbAcknowledge.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.setDisableDisplayIssues(isChecked);
			}
		});
	}

	public void dispalyChecks() {

		tvSummary.setText(checker.getSummary());
		if (checker.hasIssues()) {
			tvSummary.setTextColor(Color.RED);
		} else {
			tvSummary.setTextColor(Color.LTGRAY);
		}

		Collection<GovernorResult> governorsCheckResults = checker.getGovernorsCheckResults();

		addTableRow("Root access", checker.isRooted());
		for (GovernorResult res : governorsCheckResults) {
			tlCapabilities.addView(new GovernorResultRow(this, res));
		}

		String mailMessage = "No issues have been found...\n" +
				"If you are pleased and want to give feedback:\n" +
				"Comments and good ratings on the market are highly appreciated...\n\n" +
				"Please do not write empty e-mails!";
		if (!RootHandler.isRoot()) {
			mailMessage = "CPU tuner does not have root access!\n" +
					"Cpu tuner cannot do anything!\n\n" +
					"Please do not write e-mails unless you think there is something wrong with the check or the app.\n\n" +
					"Please do not write empty e-mails!";
		} else if (CapabilityChecker.getInstance(this).hasIssues()) {
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

	private void addTableRow(String check, boolean working) {
		TableRow tr = new TableRow(this);
		tr.addView(getTextView(check + ": "));
		tr.addView(getTextView(working));
		tlCapabilities.addView(tr);
	}

	private TextView getTextView(boolean b) {
		TextView tv = getTextView(b ? WORKING : NOT_WORKING);
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
			path = new File(Environment.getExternalStorageDirectory(), getPackageName() + SendReportActivity.DIR_REPORT);
			if (!path.exists()) {
				path.mkdirs();
			}
		}
		return new File(path, fileName);
	}

}
