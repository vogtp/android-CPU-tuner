package ch.amana.android.cputuner.view.widget;

import android.app.ActionBar;
import android.app.Activity;

import com.markupartist.android.widget.ActionBar.Action;

public class ActionBarWrapper {

	private ActionBar androidActionBar = null;
	private CputunerActionBar cputunerActionBar;
	private Activity activity;

	public ActionBarWrapper(Activity act, android.app.ActionBar actionBar) {
		this.androidActionBar = actionBar;
		this.activity = act;
	}

	public ActionBarWrapper(CputunerActionBar cputunerActionBar) {
		this.cputunerActionBar = cputunerActionBar;
	}

	public void removeAllActions() {
		if (cputunerActionBar != null) {
			cputunerActionBar.removeAllActions();
		}
		if (androidActionBar != null) {
			activity.invalidateOptionsMenu();
		}
	}

	public void addAction(Action action) {
		if (cputunerActionBar != null) {
			cputunerActionBar.addAction(action);
		}
		if (androidActionBar != null) {

		}
	}

}
