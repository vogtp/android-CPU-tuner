package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class HelpActivity extends Activity {

	private WebView wvHelp;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		wvHelp = new WebView(this);
		setContentView(wvHelp);
		WebSettings webSettings = wvHelp.getSettings();
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDefaultFontSize(16);

		wvHelp.loadUrl("file:///android_asset/help/index.html");
	}

}
