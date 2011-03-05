package ch.amana.android.cputuner.helper;

import android.content.Context;
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
}
