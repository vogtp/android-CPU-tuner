package ch.amana.android.cputuner.view.fragments;

import android.support.v4.app.Fragment;
import ch.amana.android.cputuner.model.ProfileModel;

public abstract class GovernorBaseFragment extends Fragment {

	protected GovernorFragmentCallback callback;

	protected ProfileModel profile;
	protected ProfileModel origProfile;

	private GovernorBaseFragment() {
		super();
	}

	public GovernorBaseFragment(GovernorFragmentCallback callback, ProfileModel profile, ProfileModel origProfile) {
		this();
		this.callback = callback;
		this.profile = profile;
		if (origProfile == null) {
			origProfile = profile;
		}
		this.origProfile = origProfile;
	}

	public void setProfile(ProfileModel profile) {
		this.profile = profile;
	}

	public ProfileModel getProfile() {
		return profile;
	}

	public void setOrigProfile(ProfileModel origProfile) {
		this.origProfile = origProfile;
	}

	public ProfileModel getOrigProfile() {
		return origProfile;
	}

	public abstract void updateModel();

	public abstract void updateView();

}
