package ch.amana.android.cputuner.helper;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.hw.CpuHandler;

public class GuiUtils {

	public static void setSpinner(Spinner spinner, long dbId) {
		SpinnerAdapter adapter = spinner.getAdapter();
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItemId(i) == dbId) {
				spinner.setSelection(i);
				return;
			}
		}
	}

	public static void setSpinner(Spinner spinner, String text) {
		SpinnerAdapter adapter = spinner.getAdapter();
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i) == text) {
				spinner.setSelection(i);
				return;
			}
		}
	}

	public static CharSequence getExplainGovernor(Context ctx, String gov) {
		if (CpuHandler.GOV_ONDEMAND.equals(gov)) {
			return ctx.getString(R.string.explainGovOnDemand);
		} else if (CpuHandler.GOV_CONSERVATIVE.equals(gov)) {
			return ctx.getString(R.string.explainGovConservative);
		} else if (CpuHandler.GOV_POWERSAVE.equals(gov)) {
			return ctx.getString(R.string.explainGovPowersave);
		} else if (CpuHandler.GOV_PERFORMANCE.equals(gov)) {
			return ctx.getString(R.string.explainGovPerformance);
		} else if (CpuHandler.GOV_INTERACTIVE.equals(gov)) {
			return ctx.getString(R.string.explainGovInteractive);
		} else if (CpuHandler.GOV_USERSPACE.equals(gov)) {
			return ctx.getString(R.string.explainGovUserspace);
		}
		return ctx.getString(R.string.explainNotAvailable);
	}

	public static void setLanguage(Context ctx) {
		String lang = SettingsStorage.getInstance().getLanguage();
		if (!"".equals(lang)) {
			GuiUtils.setLanguage(ctx, lang);
		}
	}

	public static void setLanguage(Context ctx, String lang) {
			Configuration config = new Configuration();
			config.locale = new Locale(lang);
			ctx.getResources().updateConfiguration(config, ctx.getResources().getDisplayMetrics());
	}

	public static void showViews(ViewGroup group, View[] views) {
		if (group == null || views == null || views.length < 1) {
			return;
		}
		int childCount = group.getChildCount();
		if (childCount == 0) {
			for (int i = 0; i < views.length; i++) {
				group.addView(views[i]);
			}
		}
	}

	public static void hideViews(ViewGroup group, View[] views) {
		if (group == null || views == null || views.length < 1) {
			return;
		}
		int childCount = group.getChildCount();
		if (childCount != 0) {
			for (int i = 0; i < views.length; i++) {
				group.removeView(views[i]);
			}
		}
	}

}
