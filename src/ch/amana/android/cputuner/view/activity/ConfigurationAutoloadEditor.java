package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.TimePicker;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.model.ConfigurationAutoloadModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.adapter.ConfigurationsSpinnerAdapter;

public class ConfigurationAutoloadEditor extends Activity {

	private Spinner spConfiguration;
	private TimePicker tpLoadTime;
	private ConfigurationAutoloadModel caModel;
	private ConfigurationAutoloadModel origCaModel;
	private ConfigurationsSpinnerAdapter configurationsSpinnerAdapter;

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
					getContentResolver().update(DB.ConfigurationAutoload.CONTENT_URI, caModel.getValues(), DB.NAME_ID + "=?", new String[] { caModel.getDbId() + "" });
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
		caModel.setConfiguration(configurationsSpinnerAdapter.getDirectory(spConfiguration.getSelectedItemPosition()).getName());
		caModel.setHour(tpLoadTime.getCurrentHour());
		caModel.setMinute(tpLoadTime.getCurrentMinute());
	}

	private void updateView() {
		spConfiguration.setSelection(configurationsSpinnerAdapter.getIndexOf(caModel.getConfiguration()));
		tpLoadTime.setCurrentHour(caModel.getHour());
		tpLoadTime.setCurrentMinute(caModel.getMinute());
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
