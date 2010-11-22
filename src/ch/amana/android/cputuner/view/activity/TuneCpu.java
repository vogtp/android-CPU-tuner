package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.model.IProfileChangeCallback;
import ch.amana.android.cputuner.model.PowerProfiles;

public class TuneCpu extends Activity implements IProfileChangeCallback {

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
	private CpuModel cpuModel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cur_cpu_info);
		cpuHandler = CpuHandler.getInstance();

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

		cpuModel = cpuHandler.getCurrentCpuSettings();

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int val = availCpuFreqs[seekBar.getProgress()];
					if (val != cpuHandler.getMaxCpuFreq()) {
						if (cpuHandler.setMaxCpuFreq(val)) {
							Toast.makeText(TuneCpu.this, "Setting CPU max freq to " + val, Toast.LENGTH_LONG).show();
						}
						cpuModel.setMaxFreq(val);
						updateView();
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.e(Logger.TAG, "Cannot set max freq in gui", e);
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
							Toast.makeText(TuneCpu.this, "Setting CPU min freq to " + val, Toast.LENGTH_LONG).show();
						}
						cpuModel.setMinFreq(val);
						updateView();
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.e(Logger.TAG, "Cannot set min freq in gui", e);
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
						Toast.makeText(parent.getContext(), "Setting govenor to " + gov, Toast.LENGTH_LONG).show();
					}
				}
				cpuModel.setGov(gov);
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				updateView();
			}

		});

		spinnerSetGov.setEnabled(RootHandler.isRoot() && cpuHandler.hasGov());

	}

	@Override
	protected void onResume() {
		super.onResume();
		PowerProfiles.registerCallback(this);
		updateView();
	}

	@Override
	protected void onPause() {
		PowerProfiles.unregisterCallback(this);
		super.onPause();
	}

	private void updateView() {
		batteryLevelChanged();
		profileChanged();
		acPowerChanged();
		tvExplainGov.setText(GuiUtils.getExplainGovernor(this, cpuHandler.getCurCpuGov()));
		tvBatteryCurrent.setText(BatteryHandler.getBatteryCurrentNow() + " mA (avg: " + BatteryHandler.getBatteryCurrentAverage() + " mA)");
		if (!cpuHandler.getCurrentCpuSettings().equals(cpuModel)) {
			if (SettingsStorage.getInstance().isDisableDisplayIssues()) {
				if (tvMessage != null) {
					LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutMessage);
					ll.removeView(tvMessage);
					tvMessage = null;
				}
			} else {
				getMessageTextView().setText("Found some issues... Some features might not work. (Tap for more information.)");
			}
		}
	}

	private void setSeekbar(int val, int[] valList, SeekBar seekBar, TextView textView) {
		textView.setText(CpuModel.convertFreq2GHz(val));
		for (int i = 0; i < valList.length; i++) {
			if (val == valList[i]) {
				seekBar.setProgress(i);
			}
		}
	}

	@Override
	public void batteryLevelChanged() {
		tvBatteryLevel.setText(PowerProfiles.getBatteryLevel() + "%");
	}

	@Override
	public void profileChanged() {
		CharSequence profile = PowerProfiles.getCurrentProfileName();
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			tvCurrentProfile.setText(profile);
			tvCurrentTrigger.setText(PowerProfiles.getCurrentTriggerName());
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
	}

	@Override
	public void acPowerChanged() {
		tvAcPower.setText(PowerProfiles.getAcPower() ? "Yes" : "No");
	}

	private TextView getMessageTextView() {
		if (tvMessage == null) {
			tvMessage = new TextView(this);
			tvMessage.setTextColor(Color.RED);
			tvMessage.setTextSize(18);
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