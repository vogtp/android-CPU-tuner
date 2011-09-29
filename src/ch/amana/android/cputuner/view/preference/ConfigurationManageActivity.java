package ch.amana.android.cputuner.view.preference;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import ch.almana.android.importexportdb.BackupRestoreCallback;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.activity.UserExperianceLevelChooser;
import ch.amana.android.cputuner.view.adapter.ConfigurationsAdapter;
import ch.amana.android.cputuner.view.adapter.ConfigurationsListAdapter;
import ch.amana.android.cputuner.view.adapter.SysConfigurationsAdapter;

public class ConfigurationManageActivity extends ListActivity implements OnItemClickListener, BackupRestoreCallback {

	private static final String SELECT_CONFIG_BY_NAME = DB.ConfigurationAutoload.NAME_CONFIGURATION + "=?";
	public static final String EXTRA_CLOSE_ON_LOAD = "closeOnLoad";
	public static final String EXTRA_DISABLE_ON_NOLOAD = "disableOnNoload";
	private ConfigurationsAdapter configsAdapter;
	private boolean closeOnLoad = false;
	private SysConfigurationsAdapter sysConfigsAdapter;
	private BackupRestoreHelper backupRestoreHelper;
	private boolean disableOnNoload = false;
	private boolean loadingSuccess = false;
	private SettingsStorage settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_manage);
		String title = getString(R.string.titleManageConfigurations);
		setTitle(title);

		closeOnLoad = getIntent().getBooleanExtra(EXTRA_CLOSE_ON_LOAD, false);
		disableOnNoload = getIntent().getBooleanExtra(EXTRA_DISABLE_ON_NOLOAD, false);

		settings = SettingsStorage.getInstance();

		if (!SettingsStorage.getInstance().isUserLevelSet()) {
			UserExperianceLevelChooser uec = new UserExperianceLevelChooser(this);
			uec.show();
		}

		backupRestoreHelper = new BackupRestoreHelper(this);

		ListView lvConfiguration = getListView();
		configsAdapter = new ConfigurationsListAdapter(this);
		lvConfiguration.setAdapter(configsAdapter);
		lvConfiguration.setOnCreateContextMenuListener(this);
		lvConfiguration.setOnItemClickListener(this);

		ListView lvSysConfigs = (ListView) findViewById(R.id.lvSysConfigs);
		try {
			sysConfigsAdapter = new SysConfigurationsAdapter(this, getAssets().list(BackupRestoreHelper.DIRECTORY_CONFIGURATIONS));
			lvSysConfigs.setAdapter(sysConfigsAdapter);
			lvSysConfigs.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String fileName = sysConfigsAdapter.getDirectoryName((int) id);
					load(fileName, false);
				}
			});
		} catch (IOException e) {
			Logger.w("Cannot load default confifgs form assets", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (configsAdapter.getCount() < 1) {
			add();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (disableOnNoload) {
			Toast.makeText(this, getString(loadingSuccess ? R.string.msgEnableCputuner : R.string.msgDisableCputuner), Toast.LENGTH_LONG).show();
			settings.setEnableProfiles(loadingSuccess);
		}
	}

	private void saveConfig(String name) {
		backupRestoreHelper.backupConfiguration(name);
	}

	private void loadConfig(String name, boolean isUserConfig) {
		try {
			backupRestoreHelper.restoreConfiguration(name, isUserConfig, true);
			settings.setCurrentConfiguration(isUserConfig ? name : name + " (modified)");
			Toast.makeText(this, getString(R.string.msg_loaded, name), Toast.LENGTH_LONG).show();

		} catch (Exception e) {
			Logger.e("Cannot load configuration");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = configsAdapter.getDirectory((int) id);
		load(file.getName(), true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.configuration_option, menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAdd:
			add();
			return true;
		}
		return GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_CONFIGURATION);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.configuration_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// long selectedItemId = getListView().getSelectedItemId();
		long itemId = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).id;
		File file = configsAdapter.getDirectory((int) itemId);
		switch (item.getItemId()) {
		case R.id.itemAdd:
			add();
			return true;
		case R.id.itemReplace:
			replace(file);
			return true;
		case R.id.itemLoad:
			load(file.getName(), true);
			return true;
		case R.id.itemDelete:
			delete(file);
			return true;
		case R.id.itemRename:
			rename(file);
			return true;

		}

		return super.onContextItemSelected(item);
	}

	private void add() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.msg_add_current_configuration);
		alertBuilder.setMessage(R.string.msg_choose_name_for_config);
		final EditText input = new EditText(this);
		alertBuilder.setView(input);
		alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				saveConfig(name);
			}

		});
		alertBuilder.setNegativeButton(android.R.string.cancel, null);
		alertBuilder.show();
	}

	private void rename(final File file) {
		String name = file.getName();
		if (name == null) {
			return;
		}
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.msg_rename_configuration);
		alertBuilder.setMessage(R.string.msg_choose_name_for_config);
		final EditText input = new EditText(this);
		input.setText(name);
		alertBuilder.setView(input);
		alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				String path = file.getPath();
				int idx = path.lastIndexOf("/");
				String oldName = path.substring(idx + 1);
				File dest = new File(path.substring(0, idx), name);
				file.renameTo(dest);
				renameDB(oldName, name);
				settings.setCurrentConfiguration(name);
				updateListView();
			}
		});
		alertBuilder.setNegativeButton(android.R.string.cancel, null);
		alertBuilder.show();
	}

	protected void renameDB(String oldName, String name) {
		ContentValues values = new ContentValues(1);
		values.put(DB.ConfigurationAutoload.NAME_CONFIGURATION, name);
		getContentResolver().update(DB.ConfigurationAutoload.CONTENT_URI, values, SELECT_CONFIG_BY_NAME, new String[] { oldName });
	}

	private void replace(final File configuration) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.menuReplaceWithCurrent);
		alertBuilder.setMessage(getString(R.string.msg_replace_with_current, configuration.getName()));
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveConfig(configuration.getName());
				updateListView();
			}

		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void load(final String configName, final boolean isUserConfig) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.menuLoad);
		alertBuilder.setMessage(getString(R.string.msg_load_configuration, configName));
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadConfig(configName, isUserConfig);
				updateListView();
			}

		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void delete(final File configuration) {
		String name = configuration.getName();
		if (name == null) {
			return;
		}
		if (name.equals(settings.getCurrentConfiguration())) {
			Toast.makeText(this, R.string.msg_cannot_delete_current_configuration, Toast.LENGTH_LONG).show();
			return;
		}
		Cursor cursor = getContentResolver().query(DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, SELECT_CONFIG_BY_NAME,
				new String[] { name }, DB.ConfigurationAutoload.SORTORDER_DEFAULT);

		// while (cursor.moveToNext()) {
		// String string =
		// cursor.getString(DB.ConfigurationAutoload.INDEX_CONFIGURATION);
		// System.out.println(string);
		// }

		Builder alertBuilder = new AlertDialog.Builder(this);
		if (cursor.moveToNext()) {
			alertBuilder.setTitle(R.string.msg_cannot_delete_configuration);
			alertBuilder.setNegativeButton(android.R.string.ok, null);
		} else {
			alertBuilder.setTitle(R.string.menuItemDelete);
			alertBuilder.setMessage(getString(R.string.msg_delete_configuration, name));
			alertBuilder.setNegativeButton(R.string.no, null);
			alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					File[] files = configuration.listFiles();
					for (int i = 0; i < files.length; i++) {
						files[i].delete();
					}
					configuration.delete();
					updateListView();
				}

			});
		}
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void updateListView() {
		configsAdapter.notifyDataSetChanged();
		getListView().refreshDrawableState();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void hasFinished(boolean success) {
		loadingSuccess = success;
		if (closeOnLoad && success) {
			finish();
		}
		updateListView();
	}
}
