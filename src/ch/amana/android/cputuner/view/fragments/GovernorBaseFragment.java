package ch.amana.android.cputuner.view.fragments;

import android.support.v4.app.Fragment;
import ch.amana.android.cputuner.model.IGovernorModel;

public abstract class GovernorBaseFragment extends Fragment {

	protected GovernorFragmentCallback callback;
	protected boolean updateVirtGov = false;

	private IGovernorModel governor;

	public GovernorBaseFragment() {
		super();
	}

	public GovernorBaseFragment(GovernorFragmentCallback callback, IGovernorModel governor) {
		this();
		this.callback = callback;
		this.governor = governor;
	}

	public void setGovernorModel(IGovernorModel governor) {
		this.governor = governor;
	}

	public IGovernorModel getGovernorModel() {
		return governor;
	}

	public abstract void updateModel();

	public abstract void updateView();

	public void updateVirtGov(boolean b) {
		updateVirtGov = true;
	}

}
