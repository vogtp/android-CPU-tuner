package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
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
		tvAcPower = (TextView) findViewById(R.id.tvAcPower);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				int val = availCpuFreqs[seekBar.getProgress()];
				if (val != cpuHandler.getMaxCpuFreq()) {
					if (cpuHandler.setMaxCpuFreq(val)) {
						Toast.makeText(TuneCpu.this, "Setting CPU max freq to " + val, Toast.LENGTH_LONG).show();
					}
					updateView();
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

				int val = availCpuFreqs[seekBar.getProgress()];
				if (val != cpuHandler.getMinCpuFreq()) {
					if (cpuHandler.setMinCpuFreq(val)) {
						Toast.makeText(TuneCpu.this, "Setting CPU min freq to " + val, Toast.LENGTH_LONG).show();
					}
					updateView();
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
						Toast.makeText(parent.getContext(), "Setting govenor to " + gov, Toast.LENGTH_LONG).show();
					}
				}
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
	}

	@Override
	public void acPowerChanged() {
		tvAcPower.setText(PowerProfiles.getAcPower() ? "Yes" : "No");
	}
}