package ch.amana.android.cputuner.view.preference;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;

public class ConfigurationManageActivity extends Activity {

	public static final String DIRECTORY = "configurations";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.titleManageConfigurations);
		setContentView(R.layout.configuration_manage);

		ListView lvConfiguration = (ListView) findViewById(R.id.lvConfiguration);
		lvConfiguration.setAdapter(new ConfigurationsAdapter(this));

		Button buAddConfiguration = (Button) findViewById(R.id.buAddConfiguration);
		buAddConfiguration.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BackupRestoreHelper.backup(ConfigurationManageActivity.this, new File(DIRECTORY, "current"));
			}
		});
	}

}
