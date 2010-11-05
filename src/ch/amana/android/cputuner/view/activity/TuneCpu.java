package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.view.preference.CpuProfilePreferenceActivity;

public class TuneCpu extends Activity {

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cpuHandler = CpuHandler.getInstance();

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

		sbCpuFreqMax.setMax(cpuHandler.getAvailCpuFreq().length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				String val = cpuHandler.getAvailCpuFreq()[seekBar.getProgress()];
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

		sbCpuFreqMin.setMax(cpuHandler.getAvailCpuFreq().length - 1);
		sbCpuFreqMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				String val = cpuHandler.getAvailCpuFreq()[seekBar.getProgress()];
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

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cpuHandler.getAvailCpuGov());
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

		CheckBox cbApplyOnBoot = (CheckBox) findViewById(R.id.cbApplyOnBoot);
		cbApplyOnBoot.setChecked(SettingsStorage.getInstance().isApplyOnBoot());
		cbApplyOnBoot.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SettingsStorage.getInstance().setApplyOnBoot(isChecked);
			}
		});
		spinnerSetGov.setEnabled(RootHandler.isRoot() && cpuHandler.hasGov());

		((Button) findViewById(R.id.ButtonProfiles)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(TuneCpu.this, CpuProfilePreferenceActivity.class));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	private void updateView() {
		tvCurrentProfile.setText(PowerProfiles.getCurrentProfile());
		tvCpuFreq.setText(cpuHandler.getCurCpuFreq());
		StringBuilder sb = new StringBuilder(10);
		sb.append(PowerProfiles.getBatteryLow() ? "Low" : "OK");
		sb.append(" (").append(PowerProfiles.getBatteryLevel()).append("%)");
		tvBatteryLevel.setText(sb.toString());
		tvAcPower.setText(PowerProfiles.getAcPower() ? "Yes" : "No");
		String[] availCpuFreq = cpuHandler.getAvailCpuFreq();
		setSeekbar(cpuHandler.getMaxCpuFreq(), availCpuFreq, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(cpuHandler.getMinCpuFreq(), availCpuFreq, sbCpuFreqMin, tvCpuFreqMin);
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