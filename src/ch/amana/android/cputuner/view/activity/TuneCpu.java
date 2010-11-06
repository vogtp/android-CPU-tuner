package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.model.IProfileChangeCallback;
import ch.amana.android.cputuner.model.PowerProfiles;

public class TuneCpu extends Activity implements IProfileChangeCallback {

	private CpuHandler cpuHandler;
	private Spinner spinnerSetGov;
	private TextView tvCpuFreq;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;
	private TextView tvBatteryLevel;
	private TextView tvAcPower;
	private TextView tvCurrentProfile;
	private Button buApplyCurProfile;
	private String[] availCpuFreqs;
	private String[] availCpuGovs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cur_cpu_info);
		cpuHandler = CpuHandler.getInstance();

		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		tvCurrentProfile = (TextView) findViewById(R.id.tvCurrentProfile);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvAcPower = (TextView) findViewById(R.id.tvAcPower);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvCpuFreq = (TextView) findViewById(R.id.tvCpuFreq);
		tvCpuFreq = (TextView) findViewById(R.id.tvCpuFreq);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				String val = availCpuFreqs[seekBar.getProgress()];
				if (!val.equals(cpuHandler.getMaxCpuFreq())) {
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

				String val = availCpuFreqs[seekBar.getProgress()];
				if (!val.equals(cpuHandler.getMinCpuFreq())) {
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
				// TODO Auto-generated method stub
				updateView();
			}

		});

		spinnerSetGov.setEnabled(RootHandler.isRoot() && cpuHandler.hasGov());

		buApplyCurProfile = ((Button) findViewById(R.id.ButtonApplyCurProfile));
		buApplyCurProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CpuModel cpu = cpuHandler.getCurrentCpuSettings();
				CharSequence currentProfile = PowerProfiles.getCurrentProfile();
				cpu.save(currentProfile);
				Notifier.notify(TuneCpu.this, currentProfile + " changed", 1);
			}
		});
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
		buApplyCurProfile.setVisibility(SettingsStorage.getInstance().isEnableProfiles() ? Button.VISIBLE : Button.INVISIBLE);
		batteryLevelChanged();
		profileChanged();
		acPowerChanged();
	}

	private void setSeekbar(String val, String[] valList, SeekBar seekBar, TextView textView) {
		textView.setText(CpuModel.convertFreq2GHz(val));
		for (int i = 0; i < valList.length; i++) {
			if (val.equals(valList[i])) {
				seekBar.setProgress(i);
			}
		}
	}

	@Override
	public void batteryLevelChanged() {
		StringBuilder sb = new StringBuilder(10);
		sb.append(PowerProfiles.getBatteryLow() ? "Low" : "OK");
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			sb.append(" (").append(PowerProfiles.getBatteryLevel()).append("%)");
		}
		tvBatteryLevel.setText(sb.toString());
	}

	@Override
	public void profileChanged() {
		CharSequence profile = PowerProfiles.getCurrentProfile();
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			tvCurrentProfile.setText(profile);
		} else {
			tvCurrentProfile.setText(R.string.profilesNotEnabled);
		}
		buApplyCurProfile.setEnabled(!PowerProfiles.NO_PROFILE.equals(profile));
		tvCpuFreq.setText(cpuHandler.getCurCpuFreq());
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