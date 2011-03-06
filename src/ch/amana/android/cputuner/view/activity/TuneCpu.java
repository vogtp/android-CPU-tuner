package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.model.ProfileModel;

public class TuneCpu extends Activity {

	private static final int[] lock = new int[1];
	private CpuTunerReceiver receiver;

	private CpuHandler cpuHandler;
	private Spinner spinnerSetGov;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;
	private TextView tvBatteryLevel;
	private TextView tvAcPower;
	private TextView tvCurrentProfile;
	private TextView tvCurrentTrigger;
	private int[] availCpuFreqs;
	private String[] availCpuGovs;
	private TextView tvExplainGov;
	private TextView labelCpuFreqMin;
	private TextView labelCpuFreqMax;
	private TextView tvMessage;
	private TextView tvBatteryCurrent;
	private TextView tvGovTreshholds;
	private PowerProfiles powerProfiles;

	protected class CpuTunerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			acPowerChanged();
			batteryLevelChanged();
			if (Notifier.BROADCAST_TRIGGER_CHANGED.equals(action)
					|| Notifier.BROADCAST_PROFILE_CHANGED.equals(action)) {
				profileChanged();
			}

		}
	}

	public void registerReceiver() {
		synchronized (lock) {
			IntentFilter deviceStatusFilter = new IntentFilter(Notifier.BROADCAST_DEVICESTATUS_CHANGED);
			IntentFilter triggerFilter = new IntentFilter(Notifier.BROADCAST_TRIGGER_CHANGED);
			IntentFilter profileFilter = new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED);
			receiver = new CpuTunerReceiver();
			registerReceiver(receiver, deviceStatusFilter);
			registerReceiver(receiver, triggerFilter);
			registerReceiver(receiver, profileFilter);
			Logger.i("Registered CpuTunerReceiver");
		}
	}

	public void unregisterReceiver() {
		synchronized (lock) {
			if (receiver != null) {
				try {
					unregisterReceiver(receiver);
					receiver = null;
				} catch (Throwable e) {
					Logger.w("Could not unregister BatteryReceiver", e);
				}
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!SettingsStorage.getInstance().isUserLevelSet()) {
			UserExperianceLevelChooser uec = new UserExperianceLevelChooser(this);
			uec.show();
		}
		
		setContentView(R.layout.cur_info);
		cpuHandler = CpuHandler.getInstance();
		powerProfiles = PowerProfiles.getInstance();

		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		tvCurrentTrigger = (TextView) findViewById(R.id.tvCurrentTrigger);
		tvCurrentProfile = (TextView) findViewById(R.id.tvCurrentProfile);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvExplainGov = (TextView) findViewById(R.id.tvExplainGov);
		tvAcPower = (TextView) findViewById(R.id.tvAcPower);
		tvBatteryCurrent = (TextView) findViewById(R.id.tvBatteryCurrent);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		labelCpuFreqMin = (TextView) findViewById(R.id.labelCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);
		tvGovTreshholds = (TextView) findViewById(R.id.tvGovTreshholds);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int val = availCpuFreqs[seekBar.getProgress()];
					if (val != cpuHandler.getMaxCpuFreq()) {
						if (cpuHandler.setMaxCpuFreq(val)) {
							Toast.makeText(TuneCpu.this, getString(R.string.msg_setting_cpu_max_freq, val), Toast.LENGTH_LONG).show();
						}
						updateView();
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.e("Cannot set max freq in gui", e);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});

		sbCpuFreqMin.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int val = availCpuFreqs[seekBar.getProgress()];
					if (val != cpuHandler.getMinCpuFreq()) {
						if (cpuHandler.setMinCpuFreq(val)) {
							Toast.makeText(TuneCpu.this, getString(R.string.setting_cpu_min_freq, val), Toast.LENGTH_LONG).show();
						}
						updateView();
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.e("Cannot set min freq in gui", e);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availCpuGovs);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String gov = parent.getItemAtPosition(pos).toString();
				if (gov != cpuHandler.getCurCpuGov()) {
					boolean ret = cpuHandler.setCurGov(gov);
					if (ret) {
						if (CpuHandler.GOV_USERSPACE.equals(gov)) {
							setSeekbar(cpuHandler.getCurCpuFreq(), availCpuFreqs, sbCpuFreqMax, tvCpuFreqMax);
						}
						Toast.makeText(parent.getContext(), getString(R.string.msg_setting_govenor, gov), Toast.LENGTH_LONG).show();
					}
				}
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				updateView();
			}

		});

		spinnerSetGov.setEnabled(cpuHandler.hasGov());

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	private void updateView() {
		batteryLevelChanged();
		profileChanged();
		acPowerChanged();

		if (SettingsStorage.getInstance().isEnableBeta()) {
			if (RootHandler.NOT_AVAILABLE.equals(cpuHandler.getCurCpuGov())
					|| cpuHandler.getMaxCpuFreq() < 1 || cpuHandler.getMinCpuFreq() < 1) {
				if (SettingsStorage.getInstance().isDisableDisplayIssues()) {
					if (tvMessage != null) {
						LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutMessage);
						ll.removeView(tvMessage);
						tvMessage = null;
					}
				} else {
					getMessageTextView().setText(R.string.msg_found_some_issues);
				}
			}
		}
	}

	private void setSeekbar(int val, int[] valList, SeekBar seekBar, TextView textView) {
		if (val == HardwareHandler.NO_VALUE_INT) {
			textView.setText(R.string.notAvailable);
		} else {
			textView.setText(ProfileModel.convertFreq2GHz(val));
		}
		for (int i = 0; i < valList.length; i++) {
			if (val == valList[i]) {
				seekBar.setProgress(i);
			}
		}
	}

	private void batteryLevelChanged() {
		StringBuilder bat = new StringBuilder();
		bat.append(powerProfiles.getBatteryLevel()).append("%");
		bat.append(" (");
		if (powerProfiles.isBatteryHot()) {
			bat.append(R.string.label_hot).append(" ");
		}
		bat.append(powerProfiles.getBatteryTemperature()).append(" °C)");
		tvBatteryLevel.setText(bat.toString());
		StringBuilder currentText = new StringBuilder();
		int currentNow = BatteryHandler.getBatteryCurrentNow();
		if (currentNow != BatteryHandler.NO_VALUE_INT) {
			currentText.append(BatteryHandler.getBatteryCurrentNow()).append(" mA/h");
		}
		int currentAvg = BatteryHandler.getBatteryCurrentAverage();
		if (currentAvg != BatteryHandler.NO_VALUE_INT && currentAvg != currentNow) {
			currentText.append(" (").append(getString(R.string.label_avgerage)).append(" ").append(BatteryHandler.getBatteryCurrentAverage()).append(" mA/h)");
		}
		if (currentText.length() > 0) {
			tvBatteryCurrent.setText(currentText.toString());
		} else {
			tvBatteryCurrent.setText(R.string.notAvailable);
		}
	}

	private void profileChanged() {
		tvExplainGov.setText(GuiUtils.getExplainGovernor(this, cpuHandler.getCurCpuGov()));
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			CharSequence profile = powerProfiles.getCurrentProfileName();
			if (PulseHelper.getInstance(this).isPulsing()) {
				int res = PulseHelper.getInstance(this).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
				profile = profile + " " + getString(res);
			}
			tvCurrentProfile.setText(profile);
			tvCurrentTrigger.setText(powerProfiles.getCurrentTriggerName());
		} else {
			tvCurrentProfile.setText(R.string.notEnabled);
			tvCurrentTrigger.setText(R.string.notEnabled);
		}

		setSeekbar(cpuHandler.getMaxCpuFreq(), availCpuFreqs, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(cpuHandler.getMinCpuFreq(), availCpuFreqs, sbCpuFreqMin, tvCpuFreqMin);
		String curGov = cpuHandler.getCurCpuGov();
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}

		if (CpuHandler.GOV_USERSPACE.equals(curGov)) {
			setSeekbar(cpuHandler.getCurCpuFreq(), availCpuFreqs, sbCpuFreqMax, tvCpuFreqMax);
			labelCpuFreqMax.setText(R.string.labelCpuFreq);
			labelCpuFreqMin.setVisibility(View.INVISIBLE);
			tvCpuFreqMin.setVisibility(View.INVISIBLE);
			sbCpuFreqMin.setVisibility(View.INVISIBLE);
		} else {
			labelCpuFreqMax.setText(R.string.labelMax);
			labelCpuFreqMin.setVisibility(View.VISIBLE);
			tvCpuFreqMin.setVisibility(View.VISIBLE);
			sbCpuFreqMin.setVisibility(View.VISIBLE);
		}

		int govThresholdUp = cpuHandler.getGovThresholdUp();
		int govThresholdDown = cpuHandler.getGovThresholdDown();
		StringBuilder sb = new StringBuilder();
		if (govThresholdUp > 0) {
			sb.append(getString(R.string.label_tresh_up)).append(" ").append(govThresholdUp).append("% ");
		}
		if (govThresholdDown > 0) {
			sb.append(getString(R.string.label_tresh_down)).append(" ").append(govThresholdDown).append("%");
		}
		if (sb.length() > 0) {
			sb.insert(0, getString(R.string.label_governor_tresholds)).append(" ");
			tvGovTreshholds.setText(sb.toString());
		} else {
			tvGovTreshholds.setText("");
		}
	}

	private void acPowerChanged() {
		tvAcPower.setText(powerProfiles.isAcPower() ? "Yes" : "No");
	}

	private TextView getMessageTextView() {
		if (tvMessage == null) {
			tvMessage = new TextView(this);
			tvMessage.setTextColor(Color.RED);
			// tvMessage.setTextSize(18);
			tvMessage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(TuneCpu.this, CapabilityCheckerActivity.class));
				}
			});
			LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutMessage);
			ll.addView(tvMessage);
		}
		return tvMessage;
	}
}