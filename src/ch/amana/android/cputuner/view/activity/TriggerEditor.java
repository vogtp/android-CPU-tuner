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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggerEditor extends Activity {

	private Spinner spBattery;
	private Spinner spPower;
	private Spinner spScreenLocked;
	private Spinner spHot;
	private TriggerModel triggerModel;
	private TriggerModel origTriggerModel;
	private EditText etName;
	private EditText etBatteryLevel;
	private SeekBar sbBatteryLevel;
	private CheckBox cbHot;
	private Spinner spCall;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_editor);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.Trigger.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				triggerModel = new TriggerModel(c);
				origTriggerModel = new TriggerModel(c);
			}
			c.close();
		}

		if (triggerModel == null) {
			triggerModel = new TriggerModel();
			triggerModel.setName("");
			origTriggerModel = new TriggerModel();
		}
		setTitle(getString(R.string.title_trigger_editor) + " " + triggerModel.getName());

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
		updateModel();
		triggerModel.saveToBundle(outState);
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

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor,
				new String[] { DB.CpuProfile.NAME_PROFILE_NAME },
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
			if (Intent.ACTION_INSERT.equals(action)) {
				Uri uri = getContentResolver().insert(DB.Trigger.CONTENT_URI, triggerModel.getValues());
				long id = ContentUris.parseId(uri);
				if (id > 0) {
					triggerModel.setDbId(id);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (!triggerModel.equals(origTriggerModel)) {
					getContentResolver().update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?",
							new String[] { triggerModel.getDbId() + "" });
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
			Bundle bundle = new Bundle();
			origTriggerModel.saveToBundle(bundle);
			triggerModel.readFromBundle(bundle);
			updateView();
			finish();
			break;
		case R.id.menuItemSave:
			finish();
			break;
		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_TRIGGER)) {
				return true;
			}
		}
		return false;
	}
}
