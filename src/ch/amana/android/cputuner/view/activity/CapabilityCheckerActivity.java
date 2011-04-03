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
	//
	// private static final String NOT_WORKING = "Not working";
	// private static final String WORKING = "Working";
	public static final String EXTRA_RECHEK = "extra_recheck";
	public static final String FILE_CAPABILITIESCHECK = "capabilitiy_check.txt";
	private CapabilityChecker checker;
	private TextView tvSummary;
	private TableLayout tlCapabilities;
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

		openLogFile(FILE_CAPABILITIESCHECK);
		checker = CapabilityChecker.getInstance(this, getIntent().getBooleanExtra(EXTRA_RECHEK, false));

		closeLogFile();
		setContentView(R.layout.capability_checker);

		tvSummary = (TextView) findViewById(R.id.tvSummary);
		tvMailMessage = (TextView) findViewById(R.id.tvMailMessage);
		tvDeviceInfo = (TextView) findViewById(R.id.tvDeviceInfo);
		tlCapabilities = (TableLayout) findViewById(R.id.tlCapabilities);
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
				CapabilityCheckerActivity.this.finish();
			}
		});

	}

	public void dispalyChecks() {
		tvSummary.setText(checker.getSummary(this));
		switch (checker.hasIssues()) {
		case SUCCESS:
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
		} else if (CapabilityChecker.getInstance(this).hasIssues() == CheckResult.FAILURE) {
			mailMessage = getString(R.string.msg_premail_issues);
			if (!DeviceInformation.getRomManagerDeveloperId().toLowerCase().contains("cyanogenmod")) {
				mailMessage += getString(R.string.msg_premail_issues_cm);
			}
			mailMessage += "\n";
		} else if (CapabilityChecker.getInstance(this).hasIssues() == CheckResult.WORKING) {
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
			path = new File(Environment.getExternalStorageDirectory(), getPackageName() + SendReportActivity.DIR_REPORT);
			if (!path.exists()) {
				path.mkdirs();
			}
		}
		return new File(path, fileName);
	}

}
