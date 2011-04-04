package ch.amana.android.cputuner.view.activity;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class HelpActivity extends Activity {

	public static final String EXTRA_HELP_PAGE = "helpPage";

	public static final String PAGE_INDEX = "index.html";
	public static final String PAGE_PROFILE = "profile.html";
	public static final String PAGE_TRIGGER = "trigger.html";
	public static final String PAGE_VIRTUAL_GOVERNOR = "virtual_governor.html";
	public static final String PAGE_CAPABILITY_CHECK = "capability_check.html";

	public static final String PAGE_SETTINGS = "settings/index.html";
	public static final String PAGE_SETTINGS_GUI = "settings/gui.html";


	private WebView wvHelp;
	private Button buHome;
	private Button buBack;
	private Button buForward;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.help);

		String page = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			page = extras.getString(EXTRA_HELP_PAGE);
		}

		if (TextUtils.isEmpty(page) || TextUtils.isEmpty(page.trim())) {
			page = PAGE_INDEX;
		}

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
				go(PAGE_INDEX);
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

		go(page);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (wvHelp.canGoBack()) {
				wvHelp.goBack();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private String getIndexFilePath() {
		String language = SettingsStorage.getInstance().getLanguage();
		if ("".equals(language)) {
			language = Locale.getDefault().getLanguage().toLowerCase();
		}
		Logger.i("Found language code " + language);
		String langHelpDir = "help-" + language;
		try {
			AssetManager assets = getAssets();
			if (assets.list(langHelpDir).length > 0) {
				return "file:///android_asset/" + langHelpDir + "/";
			}
		} catch (IOException e) {
			Logger.e("Cannot open language asset for language " + language);
		}
		return "file:///android_asset/help/";
	}

	private void go(String file) {
		wvHelp.loadUrl(getIndexFilePath() + file);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.gerneral_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (GeneralMenuHelper.onOptionsItemSelected(this, item, null)) {
			return true;
		}
		return false;
	}
}
