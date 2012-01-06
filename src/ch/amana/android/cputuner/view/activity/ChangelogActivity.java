package ch.amana.android.cputuner.view.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class ChangelogActivity extends Activity {

	private static final String CHANGELOG = "CHANGELOG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelog);
		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(R.string.title_changelog);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
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
			cputunerActionBar.setTitle(R.string.title_changelog);
		}
		TextView tvChangelog = (TextView) findViewById(R.id.tvChangelog);

		try {
			InputStream is = getResources().getAssets().open(CHANGELOG);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (int i = 0; i < 5; i++) {
				reader.readLine();
			}
			StringBuffer sb = new StringBuffer();
			String line = reader.readLine();
			while (line != null && !line.startsWith("V 1.6.1")) {
				sb.append(line).append("\n");
				line = reader.readLine();
			}
			tvChangelog.setText(sb.toString());
		} catch (IOException e) {
			Logger.w("Cannot read the changelog", e);
		}

	}
}
