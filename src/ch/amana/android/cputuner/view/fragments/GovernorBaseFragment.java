package ch.amana.android.cputuner.view.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import ch.amana.android.cputuner.model.IGovernorModel;

public abstract class GovernorBaseFragment extends Fragment {

	protected GovernorFragmentCallback callback;

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
		if (governor == null) {
			return new IGovernorModel() {

				@Override
				public void setVirtualGovernor(long id) {
				}

				@Override
				public void setUseNumberOfCpus(int position) {
				}

				@Override
				public void setScript(String string) {
				}

				@Override
				public void setPowersaveBias(int powersaveBias) {
				}

				@Override
				public void setGovernorThresholdUp(String string) {
				}

				@Override
				public void setGovernorThresholdUp(int i) {
				}

				@Override
				public void setGovernorThresholdDown(String string) {
				}

				@Override
				public void setGovernorThresholdDown(int i) {
				}

				@Override
				public void setGov(String gov) {
				}

				@Override
				public boolean hasScript() {
					return false;
				}

				@Override
				public long getVirtualGovernor() {
					return 0;
				}

				@Override
				public int getUseNumberOfCpus() {
					return 0;
				}

				@Override
				public String getScript() {
					return null;
				}

				@Override
				public int getPowersaveBias() {
					return 0;
				}

				@Override
				public int getGovernorThresholdUp() {
					return 0;
				}

				@Override
				public int getGovernorThresholdDown() {
					return 0;
				}

				@Override
				public String getGov() {
					return null;
				}

				@Override
				public CharSequence getDescription(Context ctx) {
					return null;
				}
			};
		}
		return governor;
	}

	public abstract void updateModel();

	public abstract void updateView();

	public void updateVirtGov(boolean b) {
	}

}
