package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
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
	private final int[] availCpuFreqsMin;
	private final int[] availCpuFreqsMax;
	private FrequencyChangeCallback callback;
	private final CpuHandler cpuHandler;
	private int maxFreq;
	private int minFreq;

	private CpuFrequencyChooser() {
		super();
		cpuHandler = CpuHandler.getInstance();
		availCpuFreqsMax = cpuHandler.getAvailCpuFreq(false);
		availCpuFreqsMin = cpuHandler.getAvailCpuFreq(true);
		if (availCpuFreqsMax.length > 1) {
			maxFreq = availCpuFreqsMax[availCpuFreqsMax.length - 1];
			minFreq = availCpuFreqsMin[0];
		} else {
			minFreq = availCpuFreqsMin[0];
			maxFreq = minFreq;
		}
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
					int val = availCpuFreqsMax[availCpuFreqsMax.length - position - 1];
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
					int val = availCpuFreqsMin[availCpuFreqsMin.length - position - 1];
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

	private SpinnerAdapter getCpufreqSpinnerAdapter(int[] freqs) {
		if (freqs.length > 1) {
			return buildCpufreqAdapter(freqs);
		}
		if (freqs.length == 1 && freqs[0] != HardwareHandler.NO_VALUE_INT) {
			return buildCpufreqAdapter(freqs);
		}
		ArrayAdapter<String> cpuFreqAdapter = new ArrayAdapter<String>(callback.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
		cpuFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cpuFreqAdapter.add(callback.getContext().getString(R.string.notAvailable));
		return cpuFreqAdapter;

	}

	class FrequencyAdaper extends ArrayAdapter<Integer> {

		public FrequencyAdaper(Context context, int resource, int textViewResourceId) {
			super(context, resource, textViewResourceId);
		}

		private View addMHz(int position, View v) {
			TextView tv = (TextView) v.findViewById(android.R.id.text1);
			String s = getItem(position) / 1000 + " MHz";
			tv.setText(s);
			return v;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return addMHz(position, super.getView(position, convertView, parent));
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return addMHz(position, super.getDropDownView(position, convertView, parent));
		}

	}

	private SpinnerAdapter buildCpufreqAdapter(int[] freqs) {
		FrequencyAdaper cpuFreqAdapter = new FrequencyAdaper(callback.getContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
		cpuFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = freqs.length - 1; i > -1; i--) {
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
		try {
			SpinnerAdapter adapter = spinner.getAdapter();
			for (int i = 0; i < adapter.getCount(); i++) {
				if (freq == (Integer) adapter.getItem(i)) {
					spinner.setSelection(i);
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot set current item of spinne", e);
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
