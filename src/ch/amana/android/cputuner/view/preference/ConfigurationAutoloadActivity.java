package ch.amana.android.cputuner.view.preference;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.ConfigurationAutoload;
import ch.amana.android.cputuner.view.activity.HelpActivity;

public class ConfigurationAutoloadActivity extends ListActivity {

	private SimpleCursorAdapter adapter;
	private Cursor cursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setTitle(R.string.prefConfigurationsAutoLoad);

		cursor = managedQuery(DB.ConfigurationAutoload.CONTENT_URI, DB.ConfigurationAutoload.PROJECTION_DEFAULT, null, null, DB.ConfigurationAutoload.SORTORDER_DEFAULT);

		adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, new String[] { DB.ConfigurationAutoload.NAME_HOUR,
				DB.ConfigurationAutoload.NAME_CONFIGURATION }, new int[] { android.R.id.text1, android.R.id.text2 });

		getListView().setAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onResume() {
		cursor.requery();
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == DB.ConfigurationAutoload.INDEX_HOUR) {
					StringBuffer sb = new StringBuffer();
					appendDigit(sb, cursor.getInt(ConfigurationAutoload.INDEX_HOUR));
					sb.append(":");
					appendDigit(sb, cursor.getInt(ConfigurationAutoload.INDEX_MINUTE));
					sb.append("\t");
					sb.append(cursor.getString(ConfigurationAutoload.INDEX_CONFIGURATION));
					((TextView) view).setText(sb.toString());
					return true;
				} else if (columnIndex == DB.ConfigurationAutoload.INDEX_CONFIGURATION) {
					((TextView) view).setText("");
					return true;
				}
				return false;
			}

			private void appendDigit(StringBuffer sb, int i) {
				if (i < 10) {
					sb.append("0");
				}
				sb.append(i);
			}

		});
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.list_option, menu);
		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (handleCommonMenu(item)) {
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_CONFIGURATION)) {
			return true;
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.ConfigurationAutoload.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	private boolean handleCommonMenu(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			startActivity(new Intent(Intent.ACTION_INSERT, DB.ConfigurationAutoload.CONTENT_URI));
			return true;
		}
		return false;
	}

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo
	// menuInfo) {
	// super.onCreateContextMenu(menu, v, menuInfo);
	// getMenuInflater().inflate(R.menu.profilelist_context, menu); genaralise
	// }
	//
	// @Override
	// public boolean onContextItemSelected(MenuItem item) {
	// super.onContextItemSelected(item);
	//
	// AdapterView.AdapterContextMenuInfo info;
	// try {
	// info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
	// } catch (ClassCastException e) {
	// Logger.e("bad menuInfo", e);
	// return false;
	// }
	//
	// final Uri uri = ContentUris.withAppendedId(DB.CpuProfile.CONTENT_URI,
	// info.id);
	// switch (item.getItemId()) {
	// case R.id.menuItemDelete:
	// deleteProfile(uri);
	// return true;
	//
	// case R.id.menuItemEdit:
	// startActivity(new Intent(Intent.ACTION_EDIT, uri));
	// return true;
	//
	// default:
	// return handleCommonMenu(item);
	// }
	//
	// }
}
