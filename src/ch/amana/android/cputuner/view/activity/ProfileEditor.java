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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfileEditor extends Activity {

	private ProfileModel profile;
	private CpuHandler cpuHandler;
	private Spinner spinnerSetGov;
	private SeekBar sbCpuFreqMax;
	private TextView tvCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private TextView tvCpuFreqMin;
	private String[] availCpuGovs;
	private int[] availCpuFreqs;
	private ProfileModel origProfile;
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
	private Spinner spMobileDataConnection;
	private EditText etScript;
	private LinearLayout llTop;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_editor);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.CpuProfile.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				profile = new ProfileModel(c);
				origProfile = new ProfileModel(c);
			}
			c.close();
		} else if (Intent.ACTION_EDIT.equals(action)) {
			profile = CpuHandler.getInstance().getCurrentCpuSettings();
			origProfile = CpuHandler.getInstance().getCurrentCpuSettings();
		}

		if (profile == null) {
			profile = new ProfileModel();
			origProfile = new ProfileModel();
		}

		setTitle(getString(R.string.title_profile_editor) + " " + profile.getProfileName());

		cpuHandler = CpuHandler.getInstance();
		availCpuGovs = cpuHandler.getAvailCpuGov();
		availCpuFreqs = cpuHandler.getAvailCpuFreq();

		SettingsStorage settings = SettingsStorage.getInstance();
		if (profile.getMinFreq() < cpuHandler.getMinimumSensibleFrequency() && settings.isBeginnerUser()) {
			if (availCpuFreqs != null && availCpuFreqs.length > 0) {
				profile.setMinFreq(availCpuFreqs[0]);
			}
		}

		if (ProfileModel.NO_VALUE_INT == profile.getMinFreq() && availCpuFreqs.length > 0) {
			profile.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (ProfileModel.NO_VALUE_INT == profile.getMaxFreq() && availCpuFreqs.length > 0) {
			profile.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		// FIXME remove?
		hasDeviceStatesBeta = 3 == Math.max(profile.getWifiState(),
				Math.max(profile.getGpsState(),
						Math.max(profile.getMobiledata3GState(),
								Math.max(profile.getBluetoothState(),
										Math.max(profile.getBackgroundSyncState(),
												profile.getWifiState())))));

		llTop = (LinearLayout) findViewById(R.id.llTop);
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
		spMobileDataConnection = (Spinner) findViewById(R.id.spMobileDataConnection);
		spSync = (Spinner) findViewById(R.id.spSync);
		etScript = (EditText) findViewById(R.id.etScript);

		if (!settings.isEnableScriptOnProfileChange()) {
			llTop.removeView(findViewById(R.id.llScript));
		}

		sbCpuFreqMax.requestFocus();

		TableLayout tlServices = (TableLayout) findViewById(R.id.TableLayoutServices);

		sbCpuFreqMax.setMax(availCpuFreqs.length - 1);
		sbCpuFreqMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int max = availCpuFreqs[sbCpuFreqMax.getProgress()];
					int min = availCpuFreqs[sbCpuFreqMin.getProgress()];
					if (max >= min) {
						updateModel();
						profile.setMaxFreq(max);
						updateView();
					} else {
						Toast.makeText(ProfileEditor.this, R.string.msg_minimal_frequency_bigger_than_the_maximal, Toast.LENGTH_LONG).show();
						updateView();
					}

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
					int max = availCpuFreqs[sbCpuFreqMax.getProgress()];
					int min = availCpuFreqs[sbCpuFreqMin.getProgress()];
					if (max >= min) {
						updateModel();
						profile.setMinFreq(min);
						updateView();
					} else {
						Toast.makeText(ProfileEditor.this, R.string.msg_minimal_frequency_bigger_than_the_maximal, Toast.LENGTH_LONG).show();
						updateView();
					}
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
				profile.setGov(gov);
				updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				updateView();
			}

		});

		if (settings.isEnableSwitchWifi()) {
			spWifi.setAdapter(getSystemsAdapter());
			spWifi.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setWifiState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowWifi));
		}

		if (settings.isEnableSwitchGps()) {
			spGps.setAdapter(getSystemsAdapter());
			spGps.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setGpsState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowGps));
		}

		if (settings.isEnableSwitchBluetooth()) {
			spBluetooth.setAdapter(getSystemsAdapter());
			spBluetooth.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setBluetoothState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowBluetooth));
		}

		if (settings.isEnableSwitchMobiledata3G()) {
			int mobiledatastates = R.array.mobiledataStates;
			if (settings.isEnableBeta()) {
				mobiledatastates = R.array.mobiledataStatesBeta;
			}
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, mobiledatastates, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spMobileData3G.setAdapter(adapter);
			spMobileData3G.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setMobiledata3GState(pos);

				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowMobileData3G));
		}

		if (settings.isEnableSwitchMobiledataConnection()) {
			spMobileDataConnection.setAdapter(getSystemsAdapter());
			spMobileDataConnection.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setMobiledataConnectionState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowMobiledataConnection));
		}

		if (settings.isEnableSwitchBackgroundSync()) {
			spSync.setAdapter(getSystemsAdapter());
			spSync.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setBackgroundSyncState(pos);
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

		OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && etGovTreshUp.getVisibility() == View.VISIBLE) {
					String upthresh = etGovTreshUp.getText().toString();
					String downthresh = etGovTreshDown.getText().toString();
					try {
						int up = Integer.parseInt(upthresh);
						int down = 0;
						if (etGovTreshDown.getVisibility() == View.VISIBLE) {
							down = Integer.parseInt(downthresh);
						}
						if (up > 100 || up < 0) {
							Toast.makeText(ProfileEditor.this, R.string.msg_up_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshUp.setText(origProfile.getGovernorThresholdUp() + "");
						}
						if (down > 100 || down < 0) {
							Toast.makeText(ProfileEditor.this, R.string.msg_down_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshDown.setText(origProfile.getGovernorThresholdDown() + "");
						}
						if (up > down) {
							// all OK
							return;
						}
						Toast.makeText(ProfileEditor.this, R.string.msg_up_threshhold_smaler_than_the_down_threshold, Toast.LENGTH_LONG).show();
						down = up - 10;
						etGovTreshDown.setText(down + "");
					} catch (Exception e) {
						Toast.makeText(ProfileEditor.this, R.string.msg_threshhold_NaN, Toast.LENGTH_LONG).show();
					}
				}

			}
		};
		etGovTreshUp.setOnFocusChangeListener(onFocusChangeListener);
		etGovTreshDown.setOnFocusChangeListener(onFocusChangeListener);

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
		profile.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	private void updateModel() {
		profile.setProfileName(etName.getText().toString());
		profile.setGovernorThresholdUp(etGovTreshUp.getText().toString());
		profile.setGovernorThresholdDown(etGovTreshDown.getText().toString());
		if (SettingsStorage.getInstance().isEnableScriptOnProfileChange()) {
			profile.setScript(etScript.getText().toString());
		} else {
			profile.setScript("");
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (profile == null) {
			profile = new ProfileModel(savedInstanceState);
		} else {
			profile.readFromBundle(savedInstanceState);
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
				Uri uri = getContentResolver().insert(DB.CpuProfile.CONTENT_URI, profile.getValues());
				long id = ContentUris.parseId(uri);
				if (id > 0) {
					profile.setDbId(id);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (origProfile.equals(profile)) {
					return;
				}
				if (!profile.equals(origProfile)) {
					getContentResolver().update(DB.CpuProfile.CONTENT_URI, profile.getValues(), DB.NAME_ID + "=?", new String[] { profile.getDbId() + "" });
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

		}
	}

	private void updateView() {
		etName.setText(profile.getProfileName());
		setSeekbar(profile.getMaxFreq(), availCpuFreqs, sbCpuFreqMax, tvCpuFreqMax);
		setSeekbar(profile.getMinFreq(), availCpuFreqs, sbCpuFreqMin, tvCpuFreqMin);
		String curGov = profile.getGov();
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}
		tvExplainGov.setText(GuiUtils.getExplainGovernor(this, curGov));
		spWifi.setSelection(profile.getWifiState());
		spGps.setSelection(profile.getGpsState());
		spBluetooth.setSelection(profile.getBluetoothState());
		spMobileData3G.setSelection(profile.getMobiledata3GState());
		spMobileDataConnection.setSelection(profile.getMobiledataConnectionState());
		spSync.setSelection(profile.getBackgroundSyncState());
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
		if (SettingsStorage.getInstance().isPowerUser()) {
			etScript.setText(profile.getScript());
		}
		updateGovernorFeatures();
	}

	private void updateGovernorFeatures() {
		String gov = profile.getGov();

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

		int up = profile.getGovernorThresholdUp();
		int down = profile.getGovernorThresholdDown();
		if (hasThreshholdUpFeature) {
			labelGovThreshUp.setVisibility(View.VISIBLE);
			etGovTreshUp.setVisibility(View.VISIBLE);
			if (up < 2) {
				up = 90;
			}
			etGovTreshUp.setText(up + "");
		} else {
			profile.setGovernorThresholdUp(0);
			etGovTreshUp.setText("");
			labelGovThreshUp.setVisibility(View.INVISIBLE);
			etGovTreshUp.setVisibility(View.INVISIBLE);
		}

		if (hasThreshholdDownFeature) {
			labelGovThreshDown.setVisibility(View.VISIBLE);
			etGovTreshDown.setVisibility(View.VISIBLE);
			if (down >= up || down < 1) {
				if (up > 30) {
					down = up - 10;
				} else {
					down = up - 1;
				}
			}
			etGovTreshDown.setText(down + "");
		} else {
			profile.setGovernorThresholdDown(0);
			etGovTreshDown.setText("");
			labelGovThreshDown.setVisibility(View.INVISIBLE);
			etGovTreshDown.setVisibility(View.INVISIBLE);
		}

	}

	private void setSeekbar(int val, int[] valList, SeekBar seekBar, TextView textView) {
		if (val == HardwareHandler.NO_VALUE_INT) {
			textView.setText(R.string.notAvailable);
		} else {
			textView.setText(ProfileModel.convertFreq2GHz(val));
		}
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
			origProfile.saveToBundle(bundle);
			profile.readFromBundle(bundle);
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