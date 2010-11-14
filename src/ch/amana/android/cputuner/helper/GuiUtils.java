package ch.amana.android.cputuner.helper;

import android.widget.Spinner;
import android.widget.SpinnerAdapter;

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
}
