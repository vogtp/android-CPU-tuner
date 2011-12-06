package ch.amana.android.cputuner.view.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.EditorActionbarHelper;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.EditorCallback;
import ch.amana.android.cputuner.helper.EditorActionbarHelper.ExitStatus;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.provider.CpuTunerProvider;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.adapter.ConfigurationsSpinnerAdapter;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class ConfigurationAutoloadEditor extends Activity implements EditorCallback {

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
	private ExitStatus exitStatus = ExitStatus.undefined;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_autoload_editor);

		String action = getIntent().getAction();
		if (Intent.ACTION_EDIT.equals(action) || CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
			Cursor c = null;
			try {
				c = getContentResolver().query(getIntent().getData(), DB.ConfigurationAutoload.PROJECTION_DEFAULT, null, null, null);
				if (c.moveToFirst()) {
					caModel = new ConfigurationAutoloadModel(c);
					origCaModel = new ConfigurationAutoloadModel(c);
					if (CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
						caModel.setDbId(-1);
						origCaModel.setDbId(-1);
					}
				}
			} finally {
				if (c != null) {
					c.close();
					c = null;
				}
			}
		}

		if (caModel == null) {
			caModel = new ConfigurationAutoloadModel();
			// caModel.setName("");
			origCaModel = new ConfigurationAutoloadModel();
		}

		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setHomeAction(new ActionBar.Action() {

			@Override
			public void performAction(View view) {
				onBackPressed();
			}

			@Override
			public int getDrawable() {
				return R.drawable.cputuner_back;
			}
		});
		actionBar.setTitle(getString(R.string.title_configuration_autoload_editor) + " " + caModel.getConfiguration());
		EditorActionbarHelper.addActions(this, actionBar);

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
		if (hasChange() && exitStatus == ExitStatus.save) {
			try {
				String configuration = caModel.getConfiguration();
				if (configuration == null || TextUtils.isEmpty(configuration.trim())) {
					return;
				}
				String action = getIntent().getAction();
				if (Intent.ACTION_INSERT.equals(action) || CpuTunerProvider.ACTION_INSERT_AS_NEW.equals(action)) {
					Uri uri = getContentResolver().insert(DB.ConfigurationAutoload.CONTENT_URI, caModel.getValues());
					long id = ContentUris.parseId(uri);
					if (id > 0) {
						caModel.setDbId(id);
					}
				} else if (Intent.ACTION_EDIT.equals(action)) {
					if (!caModel.equals(origCaModel)) {
						getContentResolver()
								.update(DB.ConfigurationAutoload.CONTENT_URI, caModel.getValues(), DB.NAME_ID + "=?", new String[] { Long.toString(caModel.getDbId()) });
					}
				}
			} catch (Exception e) {
				Logger.w("Cannot insert or update", e);
			}
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
			origCaModel = caModel;
			updateView();
			finish();
			return true;

		case R.id.menuItemSave:
			finish();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_SETTINGS_CONFIGURATION)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void discard() {
		exitStatus = ExitStatus.discard;
		finish();
	}

	@Override
	public void save() {
		updateModel();
		boolean ok = true;
		if (!isTimeUnique()) {
			Toast.makeText(this, "An other autload at the same time exists. Choose an other time and/or weekday(s)!", Toast.LENGTH_LONG).show();
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

	private boolean hasChange() {
		updateModel();
		return !origCaModel.equals(caModel);
	}

	@Override
	public Context getContext() {
		return this;
	}

	private boolean isTimeUnique() {
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(DB.ConfigurationAutoload.CONTENT_URI, DB.PROJECTION_IDE, DB.ConfigurationAutoload.SELECTION_TIME_WEEKDAY,
					new String[] { Integer.toString(caModel.getHour()), Integer.toString(caModel.getMinute()), Integer.toString(caModel.getWeekday()) }, null);
			if (cursor.moveToFirst()) {
				return cursor.getLong(DB.INDEX_ID) == caModel.getDbId();
			} else {
				return true;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
