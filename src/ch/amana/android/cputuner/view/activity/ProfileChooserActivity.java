package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.service.TunerService;
import ch.amana.android.cputuner.view.adapter.ProfileAdaper;

public class ProfileChooserActivity extends ListActivity {


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setTheme(android.R.style.Theme_Dialog);
		CursorLoader cursorLoader = new CursorLoader(this, DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
		Cursor cursor = cursorLoader.loadInBackground();

		setListAdapter(new ProfileAdaper(this, cursor, R.layout.profilechooser_item, R.id.text1));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (id == PowerProfiles.AUTOMATIC_PROFILE && !SettingsStorage.getInstance().isEnableProfiles()) {
			return;
		}
		Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_MANUAL_PROFILE);
		i.putExtra(TunerService.EXTRA_IS_MANUAL_PROFILE, id != PowerProfiles.AUTOMATIC_PROFILE);
		i.putExtra(TunerService.EXTRA_PROFILE_ID, id);
		startService(i);
		close();
	}

	@Override
	public void onBackPressed() {
		close();
	}

	private void close() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
		//		finish();
	}

	public static Intent getStartIntent(Context ctx) {
		Intent intent = new Intent(ctx.getApplicationContext(), ProfileChooserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
		return intent;
	}

}
