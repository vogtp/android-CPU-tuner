package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.EditorActionbarHelper;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.EditorCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.ExitStatus;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class TriggerEditor extends Activity implements EditorCallback {

	private Spinner spBattery;
	private Spinner spPower;
	private Spinner spScreenLocked;
	private Spinner spHot;
	private TriggerModel triggerModel;
	private EditText etName;
	private EditText etBatteryLevel;
	private SeekBar sbBatteryLevel;
	private CheckBox cbHot;
	private Spinner spCall;
	private ExitStatus exitStatus = ExitStatus.save;
	private ModelAccess modelAccess;
	private TriggerModel origTriggerModel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_editor);

		modelAccess = ModelAccess.getInstace(this);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			triggerModel = modelAccess.getTrigger(getIntent().getData());
		}

		if (triggerModel == null) {
			triggerModel = new TriggerModel();
			triggerModel.setName("");
		}

		Bundle bundle = new Bundle();
		triggerModel.saveToBundle(bundle);
		origTriggerModel = new TriggerModel(bundle);

		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public void performAction(View view) {
			}

			@Override
			public int getDrawable() {
				return R.drawable.icon;
			}
		});
		actionBar.setTitle(getString(R.string.title_trigger_editor) + " " + triggerModel.getName());
		EditorActionbarHelper.addActions(this, actionBar);

		etName = (EditText) findViewById(R.id.etName);
		etBatteryLevel = (EditText) findViewById(R.id.etBatteryLevel);
		sbBatteryLevel = (SeekBar) findViewById(R.id.sbBatteryLevel);
		// TODO: battery slider?
		sbBatteryLevel.setVisibility(View.INVISIBLE);
		spBattery = (Spinner) findViewById(R.id.spBattery);
		spScreenLocked = (Spinner) findViewById(R.id.spScreenLocked);
		spPower = (Spinner) findViewById(R.id.spPower);
		spCall = (Spinner) findViewById(R.id.spCall);
		spHot = (Spinner) findViewById(R.id.spHot);
		cbHot = (CheckBox) findViewById(R.id.cbHot);

		cbHot.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				spHot.setEnabled(isChecked);
			}
		});

		sbBatteryLevel.setMax(100);

		setProfilesAdapter(spBattery);
		setProfilesAdapter(spScreenLocked);
		setProfilesAdapter(spPower);
		setProfilesAdapter(spCall);
		setProfilesAdapter(spHot);

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

	@Override
	protected void onResume() {
		updateView();
		super.onResume();
	}

	private void updateView() {
		boolean hasHotProfile = triggerModel.getHotProfileId() > -1;
		cbHot.setChecked(hasHotProfile);
		spHot.setEnabled(hasHotProfile);
		spCall.setEnabled(SettingsStorage.getInstance().isEnableCallInProgressProfile());
		etName.setText(triggerModel.getName());
		etBatteryLevel.setText(triggerModel.getBatteryLevel() + "");
		sbBatteryLevel.setProgress(triggerModel.getBatteryLevel());
		GuiUtils.setSpinner(spBattery, triggerModel.getBatteryProfileId());
		GuiUtils.setSpinner(spScreenLocked, triggerModel.getScreenOffProfileId());
		GuiUtils.setSpinner(spPower, triggerModel.getPowerProfileId());
		GuiUtils.setSpinner(spHot, triggerModel.getHotProfileId());
		GuiUtils.setSpinner(spCall, triggerModel.getCallInProgessProfileId());
	}

	private void updateModel() {
		triggerModel.setName(etName.getText().toString());
		try {
			triggerModel.setBatteryLevel(Integer.parseInt(etBatteryLevel.getText().toString()));
		} catch (Exception e) {
			Logger.w("Cannot parse int from input " + etBatteryLevel.getText(), e);
		}
		triggerModel.setBatteryProfileId(spBattery.getSelectedItemId());
		triggerModel.setScreenOffProfileId(spScreenLocked.getSelectedItemId());
		triggerModel.setPowerProfileId(spPower.getSelectedItemId());
		triggerModel.setCallInProgessProfileId(spCall.getSelectedItemId());
		if (cbHot.isChecked()) {
			triggerModel.setHotProfileId(spHot.getSelectedItemId());
		} else {
			triggerModel.setHotProfileId(-1);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (exitStatus != ExitStatus.discard) {
			updateModel();
			triggerModel.saveToBundle(outState);
		} else {
			origTriggerModel.saveToBundle(outState);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (triggerModel == null) {
			triggerModel = new TriggerModel(savedInstanceState);
		} else {
			triggerModel.readFromBundle(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void setProfilesAdapter(Spinner spinner) {
		Cursor cursor = managedQuery(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, null, null, DB.CpuProfile.SORTORDER_DEFAULT);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, new String[] { DB.CpuProfile.NAME_PROFILE_NAME },
				new int[] { android.R.id.text1 });
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		updateModel();
		try {
			String action = getIntent().getAction();
			if (exitStatus == ExitStatus.save && hasChange()) {
				if (Intent.ACTION_INSERT.equals(action)) {
					modelAccess.insertTrigger(triggerModel);
				} else if (Intent.ACTION_EDIT.equals(action)) {
					modelAccess.updateTrigger(triggerModel);
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.edit_option, menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemCancel:
			discard();
			break;
		case R.id.menuItemSave:
			save();
			break;
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_TRIGGER)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void discard() {
		exitStatus = ExitStatus.discard;
		triggerModel = origTriggerModel;
		//		updateView();
		finish();
	}

	@Override
	public void save() {
		exitStatus = ExitStatus.save;
		finish();
	}

	//	@Override
	//	public void onBackPressed() {
	//		updateModel();
	//		EditorActionbarHelper.onBackPressed(this, exitStatus, hasChange());
	//	}

	private boolean hasChange() {
		return !origTriggerModel.equals(triggerModel);
	}

	@Override
	public Context getContext() {
		return this;
	}
}
