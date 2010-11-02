package ch.amana.android.cputuner.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.RootHandler;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.Cpu;

public class TuneCpu extends Activity {

	private Cpu cpu;
	private Spinner spinnerSetGov;
	private TextView tvCpuFreq;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cpu = new Cpu();

		tvCpuFreq = (TextView) findViewById(R.id.tvCpuFreq);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);

		sbCpuFreqMax.setMax(cpu.getAvailCpuFreq().length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				String val = cpu.getAvailCpuFreq()[seekBar.getProgress()];
				if (!val.equals(cpu.getMaxCpuFreq())) {
					if (cpu.setMaxCpuFreq(val)) {
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

		sbCpuFreqMin.setMax(cpu.getAvailCpuFreq().length - 1);
		sbCpuFreqMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				String val = cpu.getAvailCpuFreq()[seekBar.getProgress()];
				if (!val.equals(cpu.getMinCpuFreq())) {
					if (cpu.setMinCpuFreq(val)) {
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

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cpu.getAvailCpuGov());
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String gov = parent.getItemAtPosition(pos).toString();
				if (gov != cpu.getCurCpuGov()) {
					boolean ret = cpu.setCurGov(gov);
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
		spinnerSetGov.setEnabled(RootHandler.isRoot() && cpu.hasGov());
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	private void updateView() {
		tvCpuFreq.setText(cpu.getCurCpuFreq());
		String[] availCpuFreq = cpu.getAvailCpuFreq();
		setSeekbar(cpu.getMaxCpuFreq(), availCpuFreq, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(cpu.getMinCpuFreq(), availCpuFreq, sbCpuFreqMin, tvCpuFreqMin);
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