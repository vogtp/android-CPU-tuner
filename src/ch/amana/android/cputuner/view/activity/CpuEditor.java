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
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.model.PowerProfiles;

public class CpuEditor extends Activity {

	private CpuModel cpu;
	private CpuHandler cpuHandler;
	private Spinner spinnerSetGov;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;
	private int profile;
	private String[] availCpuGovs;
	private String[] availCpuFreqs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cpu_editor);

		profile = getIntent().getIntExtra(CpuModel.INTENT_EXTRA, -1);

		cpuHandler = CpuHandler.getInstance();
		cpu = PowerProfiles.getCpuModelForProfile(profile);
		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		if (SettingsStorage.NO_VALUE.equals(cpu.getMinFreq()) && availCpuFreqs.length > 0) {
			cpu.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (SettingsStorage.NO_VALUE.equals(cpu.getMaxFreq()) && availCpuFreqs.length > 0) {
			cpu.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		((TextView) findViewById(R.id.tvPowerProfile)).setText(cpu.getProfileName());
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
				cpu.setMaxFreq(val);
				updateView();

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
				cpu.setMinFreq(val);
				updateView();
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
				cpu.setGov(gov);
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				updateView();
			}

		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cpu.save();
		PowerProfiles.reapplyCurProfile(cpu.getProfileName());
	}

	private void updateView() {
		setSeekbar(cpu.getMaxFreq(), availCpuFreqs, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(cpu.getMinFreq(), availCpuFreqs, sbCpuFreqMin, tvCpuFreqMin);
		String curGov = cpu.getGov();
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}

	}

	private void setSeekbar(String val, String[] valList, SeekBar seekBar, TextView textView) {
		textView.setText(val);
		for (int i = 0; i < valList.length; i++) {
			if (val.equals(valList[i])) {
				seekBar.setProgress(i);
			}
		}
	}
}