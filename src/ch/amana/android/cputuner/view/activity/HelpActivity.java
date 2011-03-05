package ch.amana.android.cputuner.view.activity;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;

public class HelpActivity extends Activity {

	private WebView wvHelp;
	private Button buHome;
	private Button buBack;
	private Button buForward;
	private String indexFilePath;

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
		indexFilePath = getIndexFilePath();
		goHome();
	}
	
	private String getIndexFilePath() {
		String language = Locale.getDefault().getLanguage().toLowerCase();
		Logger.i("Found language code " + language);
		String langHelpDir = "help-" + language;
		try {
			AssetManager assets = getAssets();
			if (assets.list(langHelpDir).length > 0) {
				return "file:///android_asset/" + langHelpDir + "/index.html";
			}
		} catch (IOException e) {
			Logger.e("Cannot open language asset", e);
		}
		return "file:///android_asset/help/index.html";
	}

	private void goHome() {
		wvHelp.loadUrl(indexFilePath);
	}

}
