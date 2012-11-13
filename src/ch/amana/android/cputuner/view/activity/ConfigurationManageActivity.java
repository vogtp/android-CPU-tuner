package ch.amana.android.cputuner.view.activity;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.ModelAccess;
import ch.amana.android.cputuner.provider.DB;
import ch.amana.android.cputuner.view.adapter.ConfigurationsAdapter;
import ch.amana.android.cputuner.view.adapter.ConfigurationsListAdapter;
import ch.amana.android.cputuner.view.adapter.SysConfigurationsAdapter;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class ConfigurationManageActivity extends ListActivity implements OnItemClickListener, BackupRestoreCallback {

	public static final String EXTRA_ASK_LOAD_CONFIRMATION = "askLoadConfirmation";
	public static final String EXTRA_CLOSE_ON_LOAD = "closeOnLoad";
	public static final String EXTRA_FIRST_RUN = "firstRun";
	public static final String EXTRA_TITLE = "title";
	private ConfigurationsAdapter configsAdapter;
	private boolean closeOnLoad = false;
	private SysConfigurationsAdapter sysConfigsAdapter;
	private BackupRestoreHelper backupRestoreHelper;
	private boolean firstRun = false;
	private boolean loadingSuccess = false;
	private boolean askLoadConfirmation = true;
	private SettingsStorage settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration_manage);
		String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (title == null) {
			title = getString(R.string.titleManageConfigurations);
		}
		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(title);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
			cputunerActionBar.setTitle(title);
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

			if (InstallHelper.hasConfig(this)) {
				Intent intent = new Intent(getContext(), ConfigurationAutoloadListActivity.class);
				cputunerActionBar.addAction(new ActionBar.IntentAction(getContext(), intent, android.R.drawable.ic_menu_today));
				// android.R.drawable.ic_menu_my_calendar
				cputunerActionBar.addAction(new Action() {
					@Override
					public void performAction(View view) {
						add();
					}

					@Override
					public int getDrawable() {
						return android.R.drawable.ic_menu_add;
					}
				});
			}
		}

		askLoadConfirmation = getIntent().getBooleanExtra(EXTRA_ASK_LOAD_CONFIRMATION, true);

		closeOnLoad = getIntent().getBooleanExtra(EXTRA_CLOSE_ON_LOAD, false);
		firstRun = getIntent().getBooleanExtra(EXTRA_FIRST_RUN, false);

		settings = SettingsStorage.getInstance();

		backupRestoreHelper = new BackupRestoreHelper(this);

		ListView lvConfiguration = getListView();
		configsAdapter = new ConfigurationsListAdapter(this);
		lvConfiguration.setAdapter(configsAdapter);
		lvConfiguration.setOnCreateContextMenuListener(this);
		lvConfiguration.setOnItemClickListener(this);

		ListView lvSysConfigs = (ListView) findViewById(R.id.lvSysConfigs);
		try {
			sysConfigsAdapter = new SysConfigurationsAdapter(this);
			lvSysConfigs.setAdapter(sysConfigsAdapter);
			lvSysConfigs.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String name = sysConfigsAdapter.getConfigName((int) id);
					load(sysConfigsAdapter.getDirectoryName((int) id), name, false);
				}
			});
		} catch (IOException e) {
			Logger.w("Cannot load default confifgs form assets", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//		if (configsAdapter.getCount() < 1) {
		//			add(); 
		//		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (firstRun) {
			Toast.makeText(this, getString(loadingSuccess ? R.string.msgEnableCputuner : R.string.msgDisableCputuner), Toast.LENGTH_LONG).show();
			settings.setEnableProfiles(loadingSuccess);
			SettingsStorage.getInstance().firstRunDone();
			startActivity(CpuTunerViewpagerActivity.getStartIntent(this));
		}
	}

	private void saveConfig(String name) {
		if (!InstallHelper.hasConfig(this)) {
			Toast.makeText(this, R.string.msg_unusable_comfig_save, Toast.LENGTH_LONG).show();
			return;
		}
		backupRestoreHelper.backupConfiguration(name);
		settings.setCurrentConfiguration(name);
	}

	private void loadConfig(String file, String name, boolean isUserConfig) {
		try {
			backupRestoreHelper.restoreConfiguration(file, isUserConfig, false);
			settings.setCurrentConfiguration(name);
			Toast.makeText(this, getString(R.string.msg_loaded, name), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Logger.e("Cannot load configuration");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = configsAdapter.getDirectory((int) id);
		load(file.getName(), file.getName(), true);
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
		case R.id.itemManageAutoload:
			startActivity(new Intent(getContext(), ConfigurationAutoloadListActivity.class));
			return true;
		}
		return GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_SETTINGS_CONFIGURATION);
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
			load(file.getName(), file.getName(), true);
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
		if (!InstallHelper.hasConfig(this)) {
			Toast.makeText(this, R.string.msg_unusable_comfig_save, Toast.LENGTH_LONG).show();
			return;
		}
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.msg_add_current_configuration);
		alertBuilder.setMessage(R.string.msg_choose_name_for_config);
		final EditText input = new EditText(this);
		alertBuilder.setView(input);
		alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				final String name = input.getText().toString().trim();
				if (name == null || "".equals(name)) {
					Toast.makeText(ConfigurationManageActivity.this, R.string.msg_empty_configuration_name, Toast.LENGTH_LONG).show();

					return;
				}
				if (configsAdapter.hasConfig(name)) {
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ConfigurationManageActivity.this);
					alertBuilder.setTitle(R.string.title_config_name_exists);
					alertBuilder.setMessage(R.string.msg_config_name_exists);
					alertBuilder.setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							saveConfig(name);
						}
					});
					alertBuilder.setNegativeButton(R.string.cancel, null);
					alertBuilder.show();
				} else {
					saveConfig(name);
				}
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
				String name = input.getText().toString().trim();
				if (name == null || "".equals(name)) {
					Toast.makeText(ConfigurationManageActivity.this, R.string.msg_empty_configuration_name, Toast.LENGTH_LONG).show();
					return;
				}
				if (configsAdapter.hasConfig(name)) {
					Toast.makeText(ConfigurationManageActivity.this, R.string.msg_config_name_exists, Toast.LENGTH_LONG).show();
					return;
				}
				String path = file.getPath();
				int idx = path.lastIndexOf("/");
				String oldName = path.substring(idx + 1);
				File dest = new File(path.substring(0, idx), name);
				file.renameTo(dest);
				renameDB(oldName, name);
				if (oldName.equals(settings.getCurrentConfiguration())) {
					settings.setCurrentConfiguration(name);
				}
				updateListView();
			}
		});
		alertBuilder.setNegativeButton(android.R.string.cancel, null);
		alertBuilder.show();
	}

	protected void renameDB(String oldName, String name) {
		ContentValues values = new ContentValues(1);
		name = name.trim();
		values.put(DB.ConfigurationAutoload.NAME_CONFIGURATION, name);
		getContentResolver().update(DB.ConfigurationAutoload.CONTENT_URI, values, DB.ConfigurationAutoload.SELECTION_NAME, new String[] { oldName });
	}

	private void replace(final File configuration) {
		if (!InstallHelper.hasConfig(this)) {
			Toast.makeText(this, R.string.msg_unusable_comfig_save, Toast.LENGTH_LONG).show();
			return;
		}
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

	private void load(final String configFile, final String configName, final boolean isUserConfig) {
		if (askLoadConfirmation) {
			Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setTitle(R.string.menuLoad);
			alertBuilder.setMessage(getString(R.string.msg_load_configuration, configName));
			alertBuilder.setNegativeButton(R.string.no, null);
			alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doLoad(configFile, configName, isUserConfig);
				}

			});
			AlertDialog alert = alertBuilder.create();
			alert.show();
		} else {
			doLoad(configFile, configName, isUserConfig);
		}
	}

	private void doLoad(final String configFile, String configName, final boolean isUserConfig) {
		boolean isClose = closeOnLoad;
		if (!isUserConfig) {
			closeOnLoad = false;
		}
		loadConfig(configFile, configName, isUserConfig);
		if (!isUserConfig) {
			closeOnLoad = isClose;
			ModelAccess.getInstace(getContext()).updateProfileFromVirtualGovernor();
			saveConfig(configName);
		}
		updateListView();
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
		Cursor cursor = getContentResolver().query(DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, DB.ConfigurationAutoload.SELECTION_NAME,
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
		if (!InstallHelper.hasConfig(this)) {
			Toast.makeText(this, R.string.msg_error_loadconfig, Toast.LENGTH_LONG).show();
			success = false;
		}
		if (closeOnLoad && success) {
			finish();
		}
		updateListView();
	}
}
