package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggerEditor extends Activity {

	private Spinner spBattery;
	private Spinner spPower;
	private Spinner spScreenLocked;
	private TriggerModel triggerModel;
	private EditText etName;
	private EditText etBatteryLevel;
	private SeekBar sbBatteryLevel;
	private Object origTriggerModel;

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
			origTriggerModel = new TriggerModel();
		}
		setTitle("Trigger Editor: " + triggerModel.getName());

		etName = (EditText) findViewById(R.id.etName);
		etBatteryLevel = (EditText) findViewById(R.id.etBatteryLevel);
		sbBatteryLevel = (SeekBar) findViewById(R.id.sbBatteryLevel);
		// FIXME
		sbBatteryLevel.setVisibility(View.INVISIBLE);
		spBattery = (Spinner) findViewById(R.id.spBattery);
		spScreenLocked = (Spinner) findViewById(R.id.spScreenLocked);
		spPower = (Spinner) findViewById(R.id.spPower);

		// FIXME if battery == 100 make non editble
		sbBatteryLevel.setMax(100);

		setProfilesAdapter(spBattery);
		setProfilesAdapter(spScreenLocked);
		setProfilesAdapter(spPower);
		updateView();
	}

	private void updateView() {
		etName.setText(triggerModel.getName());
		etBatteryLevel.setText(triggerModel.getBatteryLevel() + "");
		sbBatteryLevel.setProgress(triggerModel.getBatteryLevel());
		GuiUtils.setSpinner(spBattery, triggerModel.getBatteryProfileId());
		GuiUtils.setSpinner(spScreenLocked, triggerModel.getScreenOffProfileId());
		GuiUtils.setSpinner(spPower, triggerModel.getPowerProfileId());
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
				// not yet implemented
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (origTriggerModel.equals(triggerModel)) {
					return;
				}
				getContentResolver().update(DB.Trigger.CONTENT_URI, triggerModel.getValues(), DB.NAME_ID + "=?", new String[] { triggerModel.getDbId() + "" });
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);

		}
	}

}
