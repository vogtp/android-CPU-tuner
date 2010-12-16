package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
	private EditText etName;
	private EditText etGovTreshUp;
	private EditText etGovTreshDown;
	private TextView labelGovThreshUp;
	private TextView labelGovThreshDown;
	private Spinner spMobileData3G;
	private Spinner spSync;
	private boolean hasDeviceStatesBeta;

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
		} else if (Intent.ACTION_EDIT.equals(action)) {
			cpu = CpuHandler.getInstance().getCurrentCpuSettings();
			origCpu = CpuHandler.getInstance().getCurrentCpuSettings();
		}

		if (cpu == null) {
			cpu = new CpuModel();
			origCpu = new CpuModel();
		}

		setTitle("Profile Editor: " + cpu.getProfileName());

		cpuHandler = CpuHandler.getInstance();
		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		if (cpu.getMinFreq() < cpuHandler.getMinimumSensibleFrequency()
				&& !SettingsStorage.getInstance().isPowerUser()) {
			if (availCpuFreqs != null && availCpuFreqs.length > 0) {
				cpu.setMinFreq(availCpuFreqs[0]);
			}
		}

		if (CpuModel.NO_VALUE_INT == cpu.getMinFreq() && availCpuFreqs.length > 0) {
			cpu.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (CpuModel.NO_VALUE_INT == cpu.getMaxFreq() && availCpuFreqs.length > 0) {
			cpu.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		hasDeviceStatesBeta = 3 == Math.max(cpu.getWifiState(),
				Math.max(cpu.getGpsState(),
						Math.max(cpu.getMobiledataState(),
								Math.max(cpu.getBluetoothState(),
										Math.max(cpu.getBackgroundSyncState(),
												cpu.getWifiState())))));

		etName = (EditText) findViewById(R.id.etName);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		tvExplainGov = (TextView) findViewById(R.id.tvExplainGov);
		labelCpuFreqMin = (TextView) findViewById(R.id.labelCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		labelGovThreshUp = (TextView) findViewById(R.id.labelGovThreshUp);
		labelGovThreshDown = (TextView) findViewById(R.id.labelGovThreshDown);
		etGovTreshUp = (EditText) findViewById(R.id.etGovTreshUp);
		etGovTreshDown = (EditText) findViewById(R.id.etGovTreshDown);
		spinnerSetGov = (Spinner) findViewById(R.id.SpinnerCpuGov);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);
		spWifi = (Spinner) findViewById(R.id.spWifi);
		spGps = (Spinner) findViewById(R.id.spGps);
		spBluetooth = (Spinner) findViewById(R.id.spBluetooth);
		spMobileData3G = (Spinner) findViewById(R.id.spMobileData3G);
		spSync = (Spinner) findViewById(R.id.spSync);

		sbCpuFreqMax.requestFocus();

		TableLayout tlServices = (TableLayout) findViewById(R.id.TableLayoutServices);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					updateModel();
					int val = availCpuFreqs[seekBar.getProgress()];
					cpu.setMaxFreq(val);
					updateView();

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

		sbCpuFreqMin.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					updateModel();
					if (!CpuHandler.GOV_USERSPACE.equals(cpu.getGov())) {
						int val = availCpuFreqs[seekBar.getProgress()];
						cpu.setMinFreq(val);
					}
					updateView();
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

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availCpuGovs);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateModel();
				String gov = parent.getItemAtPosition(pos).toString();
				cpu.setGov(gov);
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				updateView();
			}

		});

		if (SettingsStorage.getInstance().isEnableSwitchWifi()) {
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
		} else {
			tlServices.removeView(findViewById(R.id.TableRowWifi));
		}

		if (SettingsStorage.getInstance().isEnableSwitchGps()) {
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

		if (SettingsStorage.getInstance().isEnableSwitchBluetooth()) {
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

		if (SettingsStorage.getInstance().isEnableSwitchMobiledata()) {
			int mobiledatastates = R.array.mobiledataStates;
			if (SettingsStorage.getInstance().isEnableBeta()) {
				mobiledatastates = R.array.mobiledataStatesBeta;
			}
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, mobiledatastates, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spMobileData3G.setAdapter(adapter);
			spMobileData3G.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					cpu.setMobiledataState(pos);

				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowMobileData3G));
		}

		if (SettingsStorage.getInstance().isEnableSwitchBackgroundSync()) {
			spSync.setAdapter(getSystemsAdapter());
			spSync.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					cpu.setBackgroundSyncState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowSync));
		}

		// hide keyboard
		etName.setInputType(InputType.TYPE_NULL);
		etName.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				etName.setInputType(InputType.TYPE_CLASS_TEXT);
				return false;
			}
		});

		updateView();
	}

	private ArrayAdapter<CharSequence> getSystemsAdapter() {
		int devicestates = R.array.deviceStates;
		if (SettingsStorage.getInstance().isEnableBeta() || hasDeviceStatesBeta) {
			devicestates = R.array.deviceStatesBeta;
		}
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, devicestates, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		updateModel();
		cpu.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	private void updateModel() {
		cpu.setProfileName(etName.getText().toString());
		cpu.setGovernorThresholdUp(etGovTreshUp.getText().toString());
		cpu.setGovernorThresholdDown(etGovTreshDown.getText().toString());
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
		super.onPause();
		updateModel();
		try {
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				Uri uri = getContentResolver().insert(DB.CpuProfile.CONTENT_URI, cpu.getValues());
				long id = ContentUris.parseId(uri);
				if (id > 0) {
					cpu.setDbId(id);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (origCpu.equals(cpu)) {
					return;
				}
				if (!cpu.equals(origCpu)) {
					getContentResolver().update(DB.CpuProfile.CONTENT_URI, cpu.getValues(), DB.NAME_ID + "=?", new String[] { cpu.getDbId() + "" });
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

		}
	}

	private void updateView() {
		etName.setText(cpu.getProfileName());
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
		spMobileData3G.setSelection(cpu.getMobiledataState());
		spSync.setSelection(cpu.getBackgroundSyncState());
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
		updateGovernorFeatures();
	}

	private void updateGovernorFeatures() {
		String gov = cpu.getGov();

		boolean hasThreshholdUpFeature = true;
		boolean hasThreshholdDownFeature = true;

		if (CpuHandler.GOV_POWERSAVE.equals(gov)
				|| CpuHandler.GOV_PERFORMANCE.equals(gov)
				|| CpuHandler.GOV_USERSPACE.equals(gov)
				|| CpuHandler.GOV_INTERACTIVE.equals(gov)) {
			hasThreshholdUpFeature = false;
			hasThreshholdDownFeature = false;
		} else if (CpuHandler.GOV_ONDEMAND.equals(gov)) {
			hasThreshholdDownFeature = false;
		}

		if (hasThreshholdUpFeature) {
			labelGovThreshUp.setVisibility(View.VISIBLE);
			etGovTreshUp.setVisibility(View.VISIBLE);
			etGovTreshUp.setText(cpu.getGovernorThresholdUp() + "");
		} else {
			labelGovThreshUp.setVisibility(View.INVISIBLE);
			etGovTreshUp.setVisibility(View.INVISIBLE);
		}

		if (hasThreshholdDownFeature) {
			labelGovThreshDown.setVisibility(View.VISIBLE);
			etGovTreshDown.setVisibility(View.VISIBLE);
			etGovTreshDown.setText(cpu.getGovernorThresholdDown() + "");
		} else {
			labelGovThreshDown.setVisibility(View.INVISIBLE);
			etGovTreshDown.setVisibility(View.INVISIBLE);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.edit_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemCancel:
			Bundle bundle = new Bundle();
			origCpu.saveToBundle(bundle);
			cpu.readFromBundle(bundle);
			updateView();
			finish();
			break;
		case R.id.menuItemSave:
			finish();
			break;
		}
		return false;
	}
}