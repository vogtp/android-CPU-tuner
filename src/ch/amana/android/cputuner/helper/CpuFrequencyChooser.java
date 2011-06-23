package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.HardwareHandler;

public class CpuFrequencyChooser {

	public interface FrequencyChangeCallback {
		public Context getContext();

		public void setMaxCpuFreq(int val);

		public void setMinCpuFreq(int val);
	}

	private SeekBar sbCpuFreqMax;
	private Spinner spCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private Spinner spCpuFreqMin;
	private int[] availCpuFreqsMin;
	private int[] availCpuFreqsMax;
	private FrequencyChangeCallback callback;
	private CpuHandler cpuHandler;
	private int maxFreq;
	private int minFreq;

	private CpuFrequencyChooser() {
		super();
		cpuHandler = CpuHandler.getInstance();
		availCpuFreqsMax = cpuHandler.getAvailCpuFreq();
		availCpuFreqsMin = cpuHandler.getAvailCpuFreq(true);
		maxFreq = availCpuFreqsMax[availCpuFreqsMax.length - 1];
		minFreq = availCpuFreqsMin[0];
	}

	public CpuFrequencyChooser(FrequencyChangeCallback callback, SeekBar sbCpuFreqMin, Spinner spCpuFreqMin, SeekBar sbCpuFreqMax, Spinner spCpuFreqMax) {
		this();
		this.callback = callback;
		this.sbCpuFreqMin = sbCpuFreqMin;
		this.spCpuFreqMin = spCpuFreqMin;
		this.sbCpuFreqMax = sbCpuFreqMax;
		this.spCpuFreqMax = spCpuFreqMax;

		spCpuFreqMax.setAdapter(getCpufreqSpinnerAdapter(availCpuFreqsMax));
		spCpuFreqMax.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				try {
					int val = availCpuFreqsMax[position];
					fireMaxCpuFreqChanged(val);
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.e("Cannot set max freq in gui", e);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spCpuFreqMin.setAdapter(getCpufreqSpinnerAdapter(availCpuFreqsMin));
		spCpuFreqMin.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				try {
					int val = availCpuFreqsMin[position];
					fireMinCpuFreqChanged(val);
				} catch (ArrayIndexOutOfBoundsException e) {
					Logger.e("Cannot set min freq in gui", e);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		sbCpuFreqMax.setMax(availCpuFreqsMax.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int position = seekBar.getProgress();
				try {
					int val = availCpuFreqsMax[position];
					fireMaxCpuFreqChanged(val);
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
				int position = seekBar.getProgress();
				try {
					int val = availCpuFreqsMin[position];
					fireMinCpuFreqChanged(val);
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
	}

	private void fireMaxCpuFreqChanged(int val) {
		if (val >= minFreq && val != HardwareHandler.NO_VALUE_INT) {
			setMaxCpuFreq(val);
			callback.setMaxCpuFreq(val);
		} else {
			setMaxCpuFreq(minFreq);
		}
	}

	private void fireMinCpuFreqChanged(int val) {
		if (val <= maxFreq && val != HardwareHandler.NO_VALUE_INT) {
			setMinCpuFreq(val);
			callback.setMinCpuFreq(val);
		} else {
			setMinCpuFreq(maxFreq);
		}
	}

	// private SpinnerAdapter getCpufreqSpinnerAdapter(int[] freqs) {
	// ArrayAdapter<Integer> cpuFreqAdapter = new ArrayAdapter<Integer>(this,
	// android.R.layout.simple_spinner_item, android.R.id.text1);
	// cpuFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	// for (int i = freqs.length - 1; i > -1; i--) {
	// cpuFreqAdapter.add(freqs[i]);
	// }
	// return cpuFreqAdapter;
	// }

	private SpinnerAdapter getCpufreqSpinnerAdapter(int[] freqs) {
		ArrayAdapter<Integer> cpuFreqAdapter = new ArrayAdapter<Integer>(callback.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
		cpuFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0; i < freqs.length; i++) {
			cpuFreqAdapter.add(freqs[i]);
		}
		return cpuFreqAdapter;
	}

	private void setSeekbar(int val, int[] valList, SeekBar seekBar) {
		for (int i = 0; i < valList.length; i++) {
			if (val == valList[i]) {
				seekBar.setProgress(i);
			}
		}
	}

	private void setSpinner(int freq, Spinner spinner) {
		SpinnerAdapter adapter = spinner.getAdapter();
		for (int i = 0; i < adapter.getCount(); i++) {
			if (freq == (Integer) adapter.getItem(i)) {
				spinner.setSelection(i);
			}
		}

	}

	public void setMaxCpuFreq(int freq) {
		if (freq >= minFreq && freq != HardwareHandler.NO_VALUE_INT) {
			maxFreq = freq;
			setSeekbar(freq, availCpuFreqsMax, sbCpuFreqMax);
			setSpinner(freq, spCpuFreqMax);
		}
	}

	public void setMinCpuFreq(int freq) {
		if (freq <= maxFreq && freq != HardwareHandler.NO_VALUE_INT) {
			minFreq = freq;
			setSeekbar(freq, availCpuFreqsMin, sbCpuFreqMin);
			setSpinner(freq, spCpuFreqMin);
		}
	}
}
