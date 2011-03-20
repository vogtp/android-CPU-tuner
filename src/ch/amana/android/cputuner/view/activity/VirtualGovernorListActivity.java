package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.provider.db.DB;

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

		getListView().setAdapter(adapter);
		getListView().setOnCreateContextMenuListener(this);
	}

}
