package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import ch.amana.android.cputuner.R;

public class HelpActivity extends Activity {

	private WebView wvHelp;
	private Button buHome;
	private Button buBack;
	private Button buForward;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.help);

		wvHelp = (WebView) findViewById(R.id.wvHelp);
		buHome = (Button) findViewById(R.id.buHome);
		buBack = (Button) findViewById(R.id.buBack);
		buForward = (Button) findViewById(R.id.buForward);

		WebSettings webSettings = wvHelp.getSettings();
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDefaultFontSize(16);

		buHome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goHome();
			}
		});
		buBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wvHelp.goBack();
			}
		});
		buForward.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				wvHelp.goForward();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		goHome();
	}

	private void goHome() {
		wvHelp.loadUrl("file:///android_asset/help/index.html");
	}

}
