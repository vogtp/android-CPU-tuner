package ch.amana.android.cputuner.view.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser;
import ch.amana.android.cputuner.helper.CpuFrequencyChooser.FrequencyChangeCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.EditorCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.ExitStatus;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper.GovernorConfig;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.ProfileModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.CpuProfile;
import ch.amana.android.cputuner.view.fragments.GovernorBaseFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragment;
import ch.amana.android.cputuner.view.fragments.GovernorFragmentCallback;
import ch.amana.android.cputuner.view.fragments.VirtualGovernorFragment;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class ProfileEditor extends FragmentActivity implements GovernorFragmentCallback, FrequencyChangeCallback, EditorCallback {

	public static final String ACTION_EDIT_SWITCHPROFILE = "ACTION_EDIT_SWITCHPROFILE";

	private ProfileModel profile;
	private CpuHandler cpuHandler;
	private SeekBar sbCpuFreqMax;
	private Spinner spCpuFreqMax;
	private SeekBar sbCpuFreqMin;
	private Spinner spCpuFreqMin;
	private int[] availCpuFreqsMax;
	private int[] availCpuFreqsMin;
	private Spinner spWifi;
	private Spinner spGps;
	private Spinner spBluetooth;
	private TextView labelCpuFreqMax;
	private EditText etName;
	private Spinner spMobileData3G;
	private Spinner spSync;
	private boolean hasDeviceStatesBeta;
	private Spinner spMobileDataConnection;
	//	private LinearLayout llTop;
	private GovernorBaseFragment governorFragment;
	private TableRow trMinFreq;
	private TableRow trMaxFreq;
	private Spinner spAirplaneMode;
	private CpuFrequencyChooser cpuFrequencyChooser;
	private ExitStatus exitStatus = ExitStatus.undefined;
	private ModelAccess modelAccess;
	private ProfileModel origProfile;
	private TextView tvWarningManualServiceChanges;
	private TextView tvWarningWifiConnected;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_editor);
		SettingsStorage settings = SettingsStorage.getInstance();

		modelAccess = ModelAccess.getInstace(this);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			profile = modelAccess.getProfile(getIntent().getData());
		} else if (CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
			profile = modelAccess.getProfile(getIntent().getData());
			profile.setProfileName(null);
			profile.setDbId(-1);
		} else if (ACTION_EDIT_SWITCHPROFILE.equals(action)) {
			findViewById(R.id.llServices).setVisibility(View.GONE);
			findViewById(R.id.llProfileName).setVisibility(View.GONE);
			profile = new ProfileModel(settings.getSwitchCpuSetting());
			governorFragment = new GovernorFragment(this, profile);
		}

		if (profile == null) {
			profile = new ProfileModel();
		}

		origProfile = new ProfileModel(profile);


		if (SettingsStorage.getInstance().isUseVirtualGovernors()) {
			governorFragment = new VirtualGovernorFragment(this, profile);
		} else {
			governorFragment = new GovernorFragment(this, profile);
		}
		if (ACTION_EDIT_SWITCHPROFILE.equals(action)) {
			governorFragment = new GovernorFragment(this, profile);
		}

		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.title_profile_editor);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
			cputunerActionBar.setHomeAction(new ActionBar.Action() {

				@Override
				public void performAction(View view) {
					onBackPressed();
				}

				@Override
				public int getDrawable() {
					return R.drawable.cputuner_back;
				}
			});
			cputunerActionBar.setTitle(getString(R.string.title_profile_editor) + ": " + profile.getProfileName());
			EditorActionbarHelper.addActions(this, cputunerActionBar);
		}
		cpuHandler = CpuHandler.getInstance();
		availCpuFreqsMax = cpuHandler.getAvailCpuFreq(false);
		availCpuFreqsMin = cpuHandler.getAvailCpuFreq(true);

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.llGovernorFragmentAncor, governorFragment);
		fragmentTransaction.commit();

		if (profile.getMinFreq() < cpuHandler.getMinimumSensibleFrequency() && settings.isBeginnerUser()) {
			if (availCpuFreqsMin != null && availCpuFreqsMin.length > 0) {
				profile.setMinFreq(availCpuFreqsMin[0]);
			}
		}

		if (ProfileModel.NO_VALUE_INT == profile.getMinFreq() && availCpuFreqsMin.length > 0) {
			profile.setMinFreq(cpuHandler.getMinCpuFreq());
		}
		if (ProfileModel.NO_VALUE_INT == profile.getMaxFreq() && availCpuFreqsMax.length > 0) {
			profile.setMaxFreq(cpuHandler.getMaxCpuFreq());
		}

		// TODO make generic
		hasDeviceStatesBeta = 3 == Math.max(profile.getWifiState(),
				Math.max(profile.getGpsState(),
						Math.max(profile.getMobiledata3GState(),
								Math.max(profile.getBluetoothState(),
										Math.max(profile.getBackgroundSyncState(),
												profile.getWifiState())))));

		//		llTop = (LinearLayout) findViewById(R.id.llTop);
		etName = (EditText) findViewById(R.id.etName);
		spCpuFreqMax = (Spinner) findViewById(R.id.spCpuFreqMax);
		spCpuFreqMin = (Spinner) findViewById(R.id.spCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		tvWarningManualServiceChanges = (TextView) findViewById(R.id.tvWarningManualServiceChanges);
		tvWarningWifiConnected = (TextView) findViewById(R.id.tvWarningWifiConnected);
		sbCpuFreqMax = (SeekBar) findViewById(R.id.SeekBarCpuFreqMax);
		sbCpuFreqMin = (SeekBar) findViewById(R.id.SeekBarCpuFreqMin);
		spWifi = (Spinner) findViewById(R.id.spWifi);
		spGps = (Spinner) findViewById(R.id.spGps);
		spBluetooth = (Spinner) findViewById(R.id.spBluetooth);
		spMobileData3G = (Spinner) findViewById(R.id.spMobileData3G);
		spMobileDataConnection = (Spinner) findViewById(R.id.spMobileDataConnection);
		spAirplaneMode = (Spinner) findViewById(R.id.spAirplaneMode);
		spSync = (Spinner) findViewById(R.id.spSync);
		trMaxFreq = (TableRow) findViewById(R.id.TableRowMaxFreq);
		trMinFreq = (TableRow) findViewById(R.id.TableRowMinFreq);

		cpuFrequencyChooser = new CpuFrequencyChooser(this, sbCpuFreqMin, spCpuFreqMin, sbCpuFreqMax, spCpuFreqMax);

		TableLayout tlServices = (TableLayout) findViewById(R.id.TableLayoutServices);
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

		if (settings.isEnableAirplaneMode()) {
			spAirplaneMode.setAdapter(getSystemsAdapter());
			spAirplaneMode.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					profile.setAirplainemodeState(pos);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} else {
			tlServices.removeView(findViewById(R.id.TableRowAirplaneMode));
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

		// updateView();
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
		if (exitStatus != ExitStatus.discard) {
			updateModel();
			profile.saveToBundle(outState);
		} else {
			origProfile.saveToBundle(outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void updateModel() {
		profile.setProfileName(etName.getText().toString().trim());
		governorFragment.updateModel();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (profile == null) {
			profile = new ProfileModel(savedInstanceState);
		} else {
			profile.readFromBundle(savedInstanceState);
		}
		if (SettingsStorage.getInstance().isUseVirtualGovernors()) {
			governorFragment = new VirtualGovernorFragment(this, profile);
		} else {
			governorFragment = new GovernorFragment(this, profile);
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
		if (hasChange() && hasName() && isNameUnique()) {
			try {
				String action = getIntent().getAction();
				if (exitStatus == ExitStatus.save) {
					if (Intent.ACTION_INSERT.equals(action) || CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
						modelAccess.insertProfile(profile);
					} else if (Intent.ACTION_EDIT.equals(action)) {
						modelAccess.updateProfile(profile);
					} else if (ACTION_EDIT_SWITCHPROFILE.equals(action)) {
						SettingsStorage.getInstance(this).setSwitchCpuSetting(profile);
					}
				}
			} catch (Exception e) {
				Logger.w("Cannot insert or update", e);

			}
		}
	}

	private boolean hasChange() {
		updateModel();
		return !origProfile.equals(profile);
	}

	@Override
	public void updateView() {
		String profileName = profile.getProfileName();
		if (!ProfileModel.NO_VALUE_STR.equals(profileName)) {
			etName.setText(profileName);
		}
		cpuFrequencyChooser.setMaxCpuFreq(profile.getMaxFreq());
		cpuFrequencyChooser.setMinCpuFreq(profile.getMinFreq());
		spWifi.setSelection(profile.getWifiState());
		spGps.setSelection(profile.getGpsState());
		spBluetooth.setSelection(profile.getBluetoothState());
		spMobileData3G.setSelection(profile.getMobiledata3GState());
		spMobileDataConnection.setSelection(profile.getMobiledataConnectionState());
		spSync.setSelection(profile.getBackgroundSyncState());
		spAirplaneMode.setSelection(profile.getAirplainemodeState());

		GovernorConfig governorConfig = GovernorConfigHelper.getGovernorConfig(profile.getGov());
		if (governorConfig.hasNewLabelCpuFreqMax()) {
			labelCpuFreqMax.setText(governorConfig.getNewLabelCpuFreqMax(this));
		} else {
			labelCpuFreqMax.setText(R.string.labelMax);
		}
		if (governorConfig.hasMinFrequency()) {
			trMinFreq.setVisibility(View.VISIBLE);
		} else {
			trMinFreq.setVisibility(View.GONE);
		}
		if (governorConfig.hasMaxFrequency()) {
			trMaxFreq.setVisibility(View.VISIBLE);
		} else {
			trMaxFreq.setVisibility(View.GONE);
		}

		if (SettingsStorage.getInstance().isAllowManualServiceChanges()) {
			tvWarningManualServiceChanges.setVisibility(View.VISIBLE);
			tvWarningManualServiceChanges.setText(R.string.msg_warning_manual_service_switches);
			tvWarningManualServiceChanges.setTextColor(Color.YELLOW);
		} else {
			tvWarningManualServiceChanges.setVisibility(View.GONE);
		}
		if (!SettingsStorage.getInstance().isSwitchWifiOnConnectedNetwork()) {
			tvWarningWifiConnected.setVisibility(View.VISIBLE);
			tvWarningWifiConnected.setText(R.string.msg_warning_not_switch_wifi_connected);
			tvWarningWifiConnected.setTextColor(Color.YELLOW);
		} else {
			tvWarningWifiConnected.setVisibility(View.GONE);
		}
		governorFragment.updateView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.edit_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemCancel:
			discard();
			return true;

		case R.id.menuItemSave:
			save();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_PROFILE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void setMaxCpuFreq(int val) {
		profile.setMaxFreq(val);
	}

	@Override
	public void setMinCpuFreq(int val) {
		profile.setMinFreq(val);
	}

	@Override
	public void discard() {
		exitStatus = ExitStatus.discard;
		finish();
	}

	private boolean hasName() {
		if (ACTION_EDIT_SWITCHPROFILE.equals(getIntent().getAction())) {
			return true;
		}
		String name = profile.getProfileName();
		return name != null && !"".equals(name.trim());
	}

	private boolean isNameUnique() {
		if (ACTION_EDIT_SWITCHPROFILE.equals(getIntent().getAction())) {
			return true;
		}
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(CpuProfile.CONTENT_URI, CpuProfile.PROJECTION_ID_NAME, CpuProfile.SELECTION_NAME, new String[] { profile.getProfileName() }, null);
			if (cursor.moveToFirst()) {
				return cursor.getLong(DB.INDEX_ID) == profile.getDbId();
			}
			return true;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	@Override
	public void save() {
		updateModel();
		boolean ok = true;
		if (!hasName()) {
			Toast.makeText(this, R.string.msg_no_profile_name, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok && !isNameUnique()) {
			Toast.makeText(this, R.string.msg_profilename_exists, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok) {
			exitStatus = ExitStatus.save;
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		EditorActionbarHelper.onBackPressed(this, exitStatus, hasChange());
	}
}
