package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import ch.amana.android.cputuner.helper.InstallHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class FirstRunActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		InstallHelper.ensureSetup(this);
	}

	@Override
	protected void onResume() {
		if (!SettingsStorage.getInstance().isFirstRun()) {
			finish();
		}
		super.onResume();
	}

}
