package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class VirtualGovernorListActivity extends ListActivity {

	private Cursor displayCursor;
	private CheckBox cbEnableVirtualGovernors;

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

		cbEnableVirtualGovernors = (CheckBox) findViewById(R.id.cbEnableVirtualGovernors);
		SettingsStorage settings = SettingsStorage.getInstance();
		cbEnableVirtualGovernors.setChecked(SettingsStorage.getInstance().isUseVirtualGovernors());
		cbEnableVirtualGovernors.setEnabled(!settings.isBeginnerUser());

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.virtual_governor_item, displayCursor,
				new String[] { DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME, DB.VirtualGovernor.NAME_REAL_GOVERNOR,
						DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_DOWN, DB.VirtualGovernor.NAME_GOVERNOR_THRESHOLD_UP },
				new int[] { R.id.tvVirtualGovernor, R.id.tvGorvernor, R.id.tvThresholdDown, R.id.tvThresholdUp });

		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == VirtualGovernor.INDEX_GOVERNOR_THRESHOLD_UP) {
					if (cursor.getInt(columnIndex) < 1) {
						((TextView) view).setText("-");
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

		cbEnableVirtualGovernors.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SettingsStorage.getInstance().setUseVirtualGovernors(isChecked);
			}
		});

	}

}
