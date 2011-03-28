package ch.amana.android.cputuner.view.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class VirtualGovernorListActivity extends ListActivity {

	private Cursor displayCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.virtual_governor_list);
		displayCursor = managedQuery(DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, null, null, DB.VirtualGovernor.SORTORDER_DEFAULT);

	}

	@Override
	protected void onResume() {
		super.onResume();


		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.virtual_governor_item, displayCursor,
				new String[] { DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, DB.VirtualGovernor.NAME_REAL_GOVERNOR,
						DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP },
				new int[] { R.id.tvVirtualGovernor, R.id.tvGorvernor, R.id.tvThresholdDown, R.id.tvThresholdUp });

		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == VirtualGovernor.INDEX_GOVERNOR_THRESHOLD_UP) {
					if (cursor.getInt(columnIndex) < 1) {
						((TextView) view).setText("");
						((View) view.getParent()).findViewById(R.id.labelThresholdUp).setVisibility(View.INVISIBLE);
						return true;
					}
					((View) view.getParent()).findViewById(R.id.labelThresholdUp).setVisibility(View.VISIBLE);
				}else if (columnIndex == VirtualGovernor.INDEX_GOVERNOR_THRESHOLD_DOWN) {
					if (cursor.getInt(columnIndex) < 1) {
						((TextView) view).setText("");
						((View) view.getParent()).findViewById(R.id.labelThresholdDown).setVisibility(View.INVISIBLE);
						return true;
					}
					((View) view.getParent()).findViewById(R.id.labelThresholdDown).setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		
		getListView().setAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
		getListView().setEnabled(SettingsStorage.getInstance().isUseVirtualGovernors());
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Uri uri = ContentUris.withAppendedId(DB.VirtualGovernor.CONTENT_URI, id);

		startActivity(new Intent(Intent.ACTION_EDIT, uri));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.list_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (handleCommonMenu(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.profilelist_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Logger.e("bad menuInfo", e);
			return false;
		}

		final Uri uri = ContentUris.withAppendedId(DB.VirtualGovernor.CONTENT_URI, info.id);
		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			deleteVirtualGovernor(uri);
			return true;

		case R.id.menuItemEdit:
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			return true;
		}

		if (handleCommonMenu(item)) {
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private boolean handleCommonMenu(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemInsert:
			startActivity(new Intent(Intent.ACTION_INSERT, DB.VirtualGovernor.CONTENT_URI));
			return true;
		}
		return false;
	}

	private void deleteVirtualGovernor(final Uri uri) {
		String id = ContentUris.parseId(uri) + "";
		Cursor cursor = getContentResolver().query(DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_DEFAULT, DB.CpuProfile.NAME_VIRTUAL_GOVERNOR + "=? ", new String[] { id },
				DB.CpuProfile.SORTORDER_DEFAULT);
		Builder alertBuilder = new AlertDialog.Builder(this);
		if (cursor != null && cursor.getCount() > 0) {
			// no not delete
			alertBuilder.setTitle(R.string.menuItemDelete);
			alertBuilder.setMessage(R.string.msgDeleteVirtGovNotPossible);
			alertBuilder.setNegativeButton(android.R.string.ok, null);
		} else {
			alertBuilder.setTitle(R.string.menuItemDelete);
			alertBuilder.setMessage(R.string.msg_delete_selected_item);
			alertBuilder.setNegativeButton(android.R.string.no, null);
			alertBuilder.setPositiveButton(android.R.string.yes, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					getContentResolver().delete(uri, null, null);
				}
			});
		}
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}
}
