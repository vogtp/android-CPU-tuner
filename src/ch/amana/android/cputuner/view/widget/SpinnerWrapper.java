package ch.amana.android.cputuner.view.widget;

import java.util.HashSet;
import java.util.Set;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import ch.amana.android.cputuner.helper.GuiUtils;

public class SpinnerWrapper implements OnItemSelectedListener {

	private static final int INITIAL = Integer.MIN_VALUE;
	private final Spinner spinner;
	private final Set<OnItemSelectedListener> listeners = new HashSet<AdapterView.OnItemSelectedListener>();
	private int possition = INITIAL;

	public SpinnerWrapper(Spinner spinner) {
		this.spinner = spinner;
		spinner.setOnItemSelectedListener(this);
	}

	public void setAdapter(SpinnerAdapter adapter) {
		spinner.setAdapter(adapter);
		if (possition != INITIAL) {
			spinner.setSelection(possition);
		}
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		listeners.add(onItemSelectedListener);
	}

	public void setSelection(int i) {
		possition = i;
		spinner.setSelection(i);
	}

	public void setSelectionDbId(long id) {
		possition = GuiUtils.setSpinner(spinner, id);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		if (pos == INITIAL || view == null || parent == null) {
			// the spinner has been initalised to this value (no need to set it)
			return;
		}
		possition = pos;
		for (OnItemSelectedListener listener : listeners) {
			if (listener != null) {
				listener.onItemSelected(parent, view, pos, id);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		for (OnItemSelectedListener listener : listeners) {
			listener.onNothingSelected(parent);
		}
	}

	public SpinnerAdapter getAdapter() {
		return spinner.getAdapter();
	}

}
