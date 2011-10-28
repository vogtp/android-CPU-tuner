package ch.amana.android.cputuner.view.activity;

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class HelpActivity extends Activity {

	public static final String EXTRA_HELP_PAGE = "helpPage";

	public static final String PAGE_INDEX = "index.html";
	public static final String PAGE_PROFILE = "profile.html";
	public static final String PAGE_TRIGGER = "trigger.html";
	public static final String PAGE_VIRTUAL_GOVERNOR = "virtual_governor.html";
	public static final String PAGE_CAPABILITY_CHECK = "capability_check.html";

	public static final String PAGE_SETTINGS = "settings/index.html";
	public static final String PAGE_SETTINGS_GUI = "settings/gui.html";
	public static final String PAGE_SETTINGS_BACKEND = "settings/backend.html";
	public static final String PAGE_SETTINGS_PROFILE = "settings/profiles_triggers.html";
	public static final String PAGE_SETTINGS_CPU = "settings/cpu.html";
	public static final String PAGE_SETTINGS_SERVICE_SWITCHES = "settings/service_switches.html";
	public static final String PAGE_SETTINGS_CONFIGURATION = "settings/configuration.html";

	private WebView wvHelp;

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
		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		actionBar.setHomeAction(new ActionBar.Action() {

			@Override
			public void performAction(View view) {
				onBackPressed();
			}

			@Override
			public int getDrawable() {
				return R.drawable.cputuner_back;
			}
		});

		WebSettings webSettings = wvHelp.getSettings();
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDefaultFontSize(16);

		actionBar.setTitle(R.string.title_help_pages);
		actionBar.setHomeAction(new ActionBar.IntentAction(this, CpuTunerViewpagerActivity.getStartIntent(this), R.drawable.cputuner_back));

		actionBar.addAction(new Action() {
			@Override
			public void performAction(View view) {
				wvHelp.goBack();
			}
			@Override
			public int getDrawable() {
				return R.drawable.back;
			}
		});
		actionBar.addAction(new Action() {
			@Override
			public void performAction(View view) {
				wvHelp.goForward();
			}
			@Override
			public int getDrawable() {
				return R.drawable.forward;
			}
		});
		actionBar.addAction(new Action() {
			@Override
			public void performAction(View view) {
				go(PAGE_INDEX);
			}
			@Override
			public int getDrawable() {
				return R.drawable.home;
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
	
	private String getFilePath(String page) {
		String language = SettingsStorage.getInstance().getLanguage();
		if ("".equals(language)) {
			language = Locale.getDefault().getLanguage().toLowerCase();
		}
		String langHelpDir = "help-" + language;
		try {
			String[] list = getAssets().list(langHelpDir);
			for (int i = 0; i < list.length; i++) {
				if (list[i].equals(page)) {
					return "file:///android_asset/" + langHelpDir + "/" + page;
				}
			}

		} catch (IOException e) {
			Logger.e("Cannot open language asset for language " + language);
		}
		return "file:///android_asset/help/" + page;
	}

	private void go(String file) {
		wvHelp.loadUrl(getFilePath(file));
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
