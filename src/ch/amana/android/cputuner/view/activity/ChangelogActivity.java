package ch.amana.android.cputuner.view.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;

public class ChangelogActivity extends Activity {

	private static final String CHANGELOG = "CHANGELOG";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelog);
		setTitle(R.string.prefChangelog);
		TextView tvChangelog = (TextView) findViewById(R.id.tvChangelog);

		try {
			InputStream is = getResources().getAssets().open(CHANGELOG);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (int i= 0; i<5;i++) {
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
