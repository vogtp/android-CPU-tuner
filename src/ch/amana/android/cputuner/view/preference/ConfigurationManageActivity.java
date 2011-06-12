package ch.amana.android.cputuner.view.preference;

import java.io.File;

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
import ch.amana.android.cputuner.view.adapter.ConfigurationsAdapter;
import ch.amana.android.cputuner.view.adapter.ConfigurationsListAdapter;

public class ConfigurationManageActivity extends ListActivity implements OnItemClickListener, BackupRestoreCallback {

	private static final String SELECT_CONFIG_BY_NAME = DB.ConfigurationAutoload.NAME_CONFIGURATION + "=?";
	private ConfigurationsAdapter configurationsAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.titleManageConfigurations);

		ListView lvConfiguration = getListView();
		configurationsAdapter = new ConfigurationsListAdapter(this);
		lvConfiguration.setAdapter(configurationsAdapter);
		lvConfiguration.setOnCreateContextMenuListener(this);
		lvConfiguration.setOnItemClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (configurationsAdapter.getCount() < 1) {
			add();
		}
	}

	private void saveConfig(String name) {
		BackupRestoreHelper.backupConfiguration(this, name);
	}

	private void loadConfig(String name) {
		try {
			BackupRestoreHelper.restoreConfiguration(this, name, false);
			Toast.makeText(this, getString(R.string.msg_loaded, name), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Logger.e("Cannot load configuration");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		File file = configurationsAdapter.getDirectory((int) id);
		load(file);
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
		File file = configurationsAdapter.getDirectory((int) itemId);
		switch (item.getItemId()) {
		case R.id.itemAdd:
			add();
			return true;
		case R.id.itemReplace:
			replace(file);
			return true;
		case R.id.itemLoad:
			load(file);
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
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				saveConfig(name);
			}

		});
		alertBuilder.setNegativeButton(android.R.string.cancel, null);
		alertBuilder.show();
	}

	private void rename(final File file) {
		// FIXME check if configuration is used
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.msg_rename_configuration);
		alertBuilder.setMessage(R.string.msg_choose_name_for_config);
		final EditText input = new EditText(this);
		input.setText(file.getName());
		alertBuilder.setView(input);
		alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = input.getText().toString();
				String path = file.getPath();
				int idx = path.lastIndexOf("/");
				String oldName = path.substring(idx + 1);
				File dest = new File(path.substring(0, idx), name);
				file.renameTo(dest);
				renameDB(oldName, name);
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

	private void load(final File configuration) {
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle(R.string.menuLoad);
		alertBuilder.setMessage(getString(R.string.msg_load_configuration, configuration.getName()));
		alertBuilder.setNegativeButton(R.string.no, null);
		alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String configName = configuration.getName();
				loadConfig(configName);
				SettingsStorage.getInstance().setCurrentConfiguration(configName);
				updateListView();
			}

		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	private void delete(final File configuration) {
		// FIXME check if configuration is used
		Cursor cursor = getContentResolver().query(DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, SELECT_CONFIG_BY_NAME,
				new String[] { configuration.getName() }, DB.ConfigurationAutoload.SORTORDER_DEFAULT);

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
			alertBuilder.setMessage(getString(R.string.msg_delete_configuration, configuration.getName()));
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
		configurationsAdapter.notifyDataSetChanged();
		getListView().refreshDrawableState();
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void hasFinished(boolean success) {
		updateListView();
	}
}
