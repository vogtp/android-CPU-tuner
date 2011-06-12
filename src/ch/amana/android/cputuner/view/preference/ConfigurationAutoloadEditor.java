package ch.amana.android.cputuner.view.preference;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.adapter.ConfigurationsSpinnerAdapter;

public class ConfigurationAutoloadEditor extends Activity {

	private Spinner spConfiguration;
	private TimePicker tpLoadTime;
	private ConfigurationAutoloadModel caModel;
	private ConfigurationAutoloadModel origCaModel;
	private ConfigurationsSpinnerAdapter configurationsSpinnerAdapter;
	private CheckBox cbExactScheduling;
	private CheckBox cbMon;
	private CheckBox cbTue;
	private CheckBox cbWed;
	private CheckBox cbThu;
	private CheckBox cbFri;
	private CheckBox cbSat;
	private CheckBox cbSun;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_autoload_editor);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			Cursor c = managedQuery(getIntent().getData(), DB.ConfigurationAutoload.PROJECTION_DEFAULT, null, null, null);
			if (c.moveToFirst()) {
				caModel = new ConfigurationAutoloadModel(c);
				origCaModel = new ConfigurationAutoloadModel(c);
			}
			c.close();
		}

		if (caModel == null) {
			caModel = new ConfigurationAutoloadModel();
			// caModel.setName("");
			origCaModel = new ConfigurationAutoloadModel();
		}
		setTitle(getString(R.string.title_configuration_autoload_editor) + " " + caModel.getConfiguration());

		spConfiguration = (Spinner) findViewById(R.id.spConfiguration);
		configurationsSpinnerAdapter = new ConfigurationsSpinnerAdapter(this);
		spConfiguration.setAdapter(configurationsSpinnerAdapter);

		tpLoadTime = (TimePicker) findViewById(R.id.tpLoadTime);
		tpLoadTime.setIs24HourView(SettingsStorage.getInstance().is24Hour());

		cbExactScheduling = (CheckBox) findViewById(R.id.cbExactScheduling);

		cbMon = (CheckBox) findViewById(R.id.cbMon);
		cbTue = (CheckBox) findViewById(R.id.cbTue);
		cbWed = (CheckBox) findViewById(R.id.cbWed);
		cbThu = (CheckBox) findViewById(R.id.cbThu);
		cbFri = (CheckBox) findViewById(R.id.cbFri);
		cbSat = (CheckBox) findViewById(R.id.cbSat);
		cbSun = (CheckBox) findViewById(R.id.cbSun);

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
			String configuration = caModel.getConfiguration();
			if (configuration == null || TextUtils.isEmpty(configuration.trim())) {
				return;
			}
			String action = getIntent().getAction();
			if (Intent.ACTION_INSERT.equals(action)) {
				Uri uri = getContentResolver().insert(DB.ConfigurationAutoload.CONTENT_URI, caModel.getValues());
				long id = ContentUris.parseId(uri);
				if (id > 0) {
					caModel.setDbId(id);
				}
			} else if (Intent.ACTION_EDIT.equals(action)) {
				if (caModel.equals(origCaModel)) {
					return;
				}
				if (!caModel.equals(origCaModel)) {
					getContentResolver().update(DB.ConfigurationAutoload.CONTENT_URI, caModel.getValues(), DB.NAME_ID + "=?", new String[] { Long.toString(caModel.getDbId()) });
				}
			}
		} catch (Exception e) {
			Logger.w("Cannot insert or update", e);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		updateModel();
		caModel.saveToBundle(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (caModel == null) {
			caModel = new ConfigurationAutoloadModel(savedInstanceState);
		} else {
			caModel.readFromBundle(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	private void updateModel() {
		try {
			caModel.setConfiguration(configurationsSpinnerAdapter.getDirectory(spConfiguration.getSelectedItemPosition()).getName());
		} catch (Exception e) {
			Logger.w("No configuration chosen");
		}
		caModel.setHour(tpLoadTime.getCurrentHour());
		caModel.setMinute(tpLoadTime.getCurrentMinute());
		caModel.setExactScheduling(cbExactScheduling.isChecked());
		caModel.setWeekdayBit(Calendar.SUNDAY, cbSun.isChecked());
		caModel.setWeekdayBit(Calendar.MONDAY, cbMon.isChecked());
		caModel.setWeekdayBit(Calendar.TUESDAY, cbTue.isChecked());
		caModel.setWeekdayBit(Calendar.WEDNESDAY, cbWed.isChecked());
		caModel.setWeekdayBit(Calendar.THURSDAY, cbThu.isChecked());
		caModel.setWeekdayBit(Calendar.FRIDAY, cbFri.isChecked());
		caModel.setWeekdayBit(Calendar.SATURDAY, cbSat.isChecked());
	}

	private void updateView() {
		spConfiguration.setSelection(configurationsSpinnerAdapter.getIndexOf(caModel.getConfiguration()));
		tpLoadTime.setCurrentHour(caModel.getHour());
		tpLoadTime.setCurrentMinute(caModel.getMinute());
		cbExactScheduling.setChecked(caModel.isExactScheduling());
		cbSun.setChecked(caModel.isWeekday(Calendar.SUNDAY));
		cbMon.setChecked(caModel.isWeekday(Calendar.MONDAY));
		cbTue.setChecked(caModel.isWeekday(Calendar.TUESDAY));
		cbWed.setChecked(caModel.isWeekday(Calendar.WEDNESDAY));
		cbThu.setChecked(caModel.isWeekday(Calendar.THURSDAY));
		cbFri.setChecked(caModel.isWeekday(Calendar.FRIDAY));
		cbSat.setChecked(caModel.isWeekday(Calendar.SATURDAY));
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
			Bundle bundle = new Bundle();
			origCaModel.saveToBundle(bundle);
			caModel.readFromBundle(bundle);
			updateView();
			finish();
			return true;

		case R.id.menuItemSave:
			finish();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_CONFIGURATION)) {
				return true;
			}
		}
		return false;
	}
}
