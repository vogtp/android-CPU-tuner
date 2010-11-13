package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.model.TriggerModel;
import ch.amana.android.cputuner.provider.db.DB;

public class TriggerEditor extends Activity {

	private Spinner spBattery;
	private Spinner spPower;
	private Spinner spScreenLocked;
	private String[] availProfiles;
	private TriggerModel triggerModel;
	private EditText etName;
	private EditText etBatteryLevel;
	private SeekBar sbBatteryLevel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trigger_editor);

		// FIXME get profiles
		availProfiles = CpuHandler.getInstance().getAvailCpuGov();

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.Trigger.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				triggerModel = new TriggerModel(c);
			}
			c.close();
		}

		if (triggerModel == null) {
			triggerModel = new TriggerModel();
		}

		etName = (EditText) findViewById(R.id.etName);
		etBatteryLevel = (EditText) findViewById(R.id.etBatteryLevel);
		sbBatteryLevel = (SeekBar) findViewById(R.id.sbBatteryLevel);
		spBattery = (Spinner) findViewById(R.id.spBattery);
		spScreenLocked = (Spinner) findViewById(R.id.spScreenLocked);
		spPower = (Spinner) findViewById(R.id.spPower);

		setProfilesAdapter(spBattery);
		setProfilesAdapter(spScreenLocked);
		setProfilesAdapter(spPower);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
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

}
