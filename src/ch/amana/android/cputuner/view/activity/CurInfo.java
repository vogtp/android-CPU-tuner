package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
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
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.provider.db.DB;

public class CurInfo extends Activity {

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
	private TextView tvCurrentTrigger;
	private int[] availCpuFreqsMin;
	private int[] availCpuFreqsMax;
	private String[] availCpuGovs;
	private TextView tvExplainGov;
	private TextView labelCpuFreqMin;
	private TextView labelCpuFreqMax;
	private TextView tvBatteryCurrent;
	private TextView tvGovTreshholds;
	private PowerProfiles powerProfiles;
	private Spinner spProfiles;
	private TextView labelBatteryCurrent;

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
		availCpuFreqsMax = cpuHandler.getAvailCpuFreq();
		availCpuFreqsMin = cpuHandler.getAvailCpuFreq(true);

		tvCurrentTrigger = (TextView) findViewById(R.id.tvCurrentTrigger);
		spProfiles = (Spinner) findViewById(R.id.spProfiles);
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
		labelBatteryCurrent = (TextView) findViewById(R.id.labelBatteryCurrent);

		Cursor cursor = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, null, null, DB.CpuProfile.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, new String[] { DB.CpuProfile.NAME_PROFILE_NAME },
				new int[] { android.R.id.text1 });
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spProfiles.setAdapter(adapter);

		spProfiles.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String profile = parent.getItemAtPosition(pos).toString();
				ProfileModel currentProfile = powerProfiles.getCurrentProfile();
				if (profile != null && !profile.equals(currentProfile)) {
					powerProfiles.applyProfile(id);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		sbCpuFreqMax.setMax(availCpuFreqsMax.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int val = availCpuFreqsMax[seekBar.getProgress()];
					if (val != cpuHandler.getMaxCpuFreq()) {
						if (cpuHandler.setMaxCpuFreq(val)) {
							Toast.makeText(CurInfo.this, getString(R.string.msg_setting_cpu_max_freq, val), Toast.LENGTH_LONG).show();
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

		sbCpuFreqMin.setMax(availCpuFreqsMin.length - 1);
		sbCpuFreqMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int val = availCpuFreqsMin[seekBar.getProgress()];
					if (val != cpuHandler.getMinCpuFreq()) {
						if (cpuHandler.setMinCpuFreq(val)) {
							Toast.makeText(CurInfo.this, getString(R.string.setting_cpu_min_freq, val), Toast.LENGTH_LONG).show();
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
							setSeekbar(cpuHandler.getCurCpuFreq(), availCpuFreqsMax, sbCpuFreqMax, tvCpuFreqMax);
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
		BatteryHandler batteryHandler = BatteryHandler.getInstance();
		int currentNow = batteryHandler.getBatteryCurrentNow();
		if (currentNow > 0) {
			currentText.append(batteryHandler.getBatteryCurrentNow()).append(" mA/h");
		}
		int currentAvg = batteryHandler.getBatteryCurrentAverage();
		if (currentAvg != BatteryHandler.NO_VALUE_INT && currentAvg != currentNow) {
			currentText.append(" (").append(getString(R.string.label_avgerage)).append(" ").append(batteryHandler.getBatteryCurrentAverage()).append(" mA/h)");
		}
		if (currentText.length() > 0) {
			labelBatteryCurrent.setVisibility(View.VISIBLE);
			tvBatteryCurrent.setVisibility(View.VISIBLE);
			tvBatteryCurrent.setText(currentText.toString());
		} else {
			labelBatteryCurrent.setVisibility(View.INVISIBLE);
			tvBatteryCurrent.setVisibility(View.INVISIBLE);
		}
	}

	private void profileChanged() {
		tvExplainGov.setText(GuiUtils.getExplainGovernor(this, cpuHandler.getCurCpuGov()));
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			CharSequence profile = powerProfiles.getCurrentProfileName();
			if (PulseHelper.getInstance(this).isPulsing()) {
				// FIXME show pulsing
				int res = PulseHelper.getInstance(this).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
				profile = profile + " " + getString(res);
			}
			ProfileModel currentProfile = powerProfiles.getCurrentProfile();
			if (currentProfile != null) {
				GuiUtils.setSpinner(spProfiles, currentProfile.getDbId());
				spProfiles.setEnabled(true);
			} else {
				spProfiles.setEnabled(false);
			}
			tvCurrentTrigger.setText(powerProfiles.getCurrentTriggerName());
		} else {
			spProfiles.setEnabled(false);
			tvCurrentTrigger.setText(R.string.notEnabled);
		}

		setSeekbar(cpuHandler.getMaxCpuFreq(), availCpuFreqsMax, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(cpuHandler.getMinCpuFreq(), availCpuFreqsMin, sbCpuFreqMin, tvCpuFreqMin);
		String curGov = cpuHandler.getCurCpuGov();
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}

		if (CpuHandler.GOV_USERSPACE.equals(curGov)) {
			setSeekbar(cpuHandler.getCurCpuFreq(), availCpuFreqsMax, sbCpuFreqMax, tvCpuFreqMax);
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
			tvGovTreshholds.setText(sb.toString());
		} else {
			tvGovTreshholds.setText("");
		}
	}

	private void acPowerChanged() {
		tvAcPower.setText(getText(powerProfiles.isAcPower() ? R.string.yes : R.string.no));
	}

}