package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.CpuModel;
import ch.amana.android.cputuner.provider.db.DB;

public class CpuEditor extends Activity {

	private CpuModel cpu;
	private CpuHandler cpuHandler;
	private Spinner spinnerSetGov;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;
	private String[] availCpuGovs;
	private int[] availCpuFreqs;
	private CpuModel origCpu;
	private TextView tvExplainGov;
	private Spinner spWifi;
	private Spinner spGps;
	private Spinner spBluetooth;
	private TextView labelCpuFreqMin;
	private TextView labelCpuFreqMax;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cpu_editor);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.CpuProfile.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				cpu = new CpuModel(c);
				origCpu = new CpuModel(c);
			}
			c.close();
		}

		if (cpu == null) {
			cpu = new CpuModel();
			origCpu = new CpuModel();
		}

		cpuHandler = CpuHandler.getInstance();
		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		if (CpuModel.NO_VALUE_INT == cpu.getMinFreq() && availCpuFreqs.length > 0) {
			cpu.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (CpuModel.NO_VALUE_INT == cpu.getMaxFreq() && availCpuFreqs.length > 0) {
			cpu.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		((TextView) findViewById(R.id.tvPowerProfile)).setText(cpu.getProfileName());
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		tvExplainGov = (TextView) findViewById(R.id.tvExplainGov);
		labelCpuFreqMin = (TextView) findViewById(R.id.labelCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);
		spWifi = (Spinner) findViewById(R.id.spWifi);
		spGps = (Spinner) findViewById(R.id.spGps);
		spBluetooth = (Spinner) findViewById(R.id.spBluetooth);

		TableLayout tlServices = (TableLayout) findViewById(R.id.TableLayoutServices);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				int val = availCpuFreqs[seekBar.getProgress()];
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
				if (!CpuHandler.GOV_USERSPACE.equals(cpu.getGov())) {
					int val = availCpuFreqs[seekBar.getProgress()];
					cpu.setMinFreq(val);
				}
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
				updateView();
			}

		});

		spWifi.setAdapter(getSystemsAdapter());
		spWifi.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				cpu.setWifiState(pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		if (SettingsStorage.getInstance().isEnableBeta() || RootHandler.isSystemApp(this)) {
			spGps.setAdapter(getSystemsAdapter());
			spGps.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					cpu.setGpsState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowGps));
		}

		if (BluetoothAdapter.getDefaultAdapter() != null) {
			spBluetooth.setAdapter(getSystemsAdapter());
			spBluetooth.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					cpu.setBluetoothState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowBluetooth));
		}

		updateView();
	}

	private ArrayAdapter<CharSequence> getSystemsAdapter() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.deviceStates, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		cpu.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (cpu == null) {
			cpu = new CpuModel(savedInstanceState);
		} else {
			cpu.readFromBundle(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	@Override
	protected void onPause() {
		try {
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				// not yet implemented
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (origCpu.equals(cpu)) {
					return;
				}
				getContentResolver().update(DB.CpuProfile.CONTENT_URI, cpu.getValues(), DB.NAME_ID + "=?", new String[] { cpu.getDbId() + "" });
			}
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot insert or update", e);

		}
		super.onPause();
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
		tvExplainGov.setText(GuiUtils.getExplainGovernor(this, curGov));
		spWifi.setSelection(cpu.getWifiState());
		spGps.setSelection(cpu.getGpsState());
		spBluetooth.setSelection(cpu.getBluetoothState());
		if (CpuHandler.GOV_USERSPACE.equals(curGov)) {
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

	private void setSeekbar(int val, int[] valList, SeekBar seekBar, TextView textView) {
		textView.setText(CpuModel.convertFreq2GHz(val));
		for (int i = 0; i < valList.length; i++) {
			if (val == valList[i]) {
				seekBar.setProgress(i);
			}
		}
	}
}