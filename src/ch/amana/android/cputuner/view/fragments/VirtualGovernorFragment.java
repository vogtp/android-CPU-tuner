package ch.amana.android.cputuner.view.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.model.IGovernorModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;
import ch.amana.android.cputuner.view.widget.SpinnerWrapper;

public class VirtualGovernorFragment extends GovernorBaseFragment {

	private static final String SELECTION_VIRT_GOV = DB.NAME_ID + "=?";
	private SpinnerWrapper spinnerSetGov;
	private TextView tvExplainGov;
	private Cursor cursor;

	public VirtualGovernorFragment() {
		super();
	}

	public VirtualGovernorFragment(GovernorFragmentCallback callback, IGovernorModel governor) {
		super(callback, governor);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.virtual_governor_fragment, container, false);
		tvExplainGov = (TextView) v.findViewById(R.id.tvExplainGov);
		spinnerSetGov = new SpinnerWrapper((Spinner) v.findViewById(R.id.SpinnerCpuGov));
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentActivity act = getActivity();

		CursorLoader cursorLoader = new CursorLoader(act, DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, null, null, DB.VirtualGovernor.SORTORDER_DEFAULT);
		cursor = cursorLoader.loadInBackground();

		SimpleCursorAdapter arrayAdapter = new SimpleCursorAdapter(act, android.R.layout.simple_spinner_item, cursor,
				new String[] { DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME }, new int[] { android.R.id.text1 });
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		try {
			spinnerSetGov.setSelectionDbId(getGovernorModel().getVirtualGovernor());
		} catch (Exception e) {
			Logger.w("Cannot set virtual governor", e);
			spinnerSetGov.setSelectionDbId(-1);
		}
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				callback.updateModel();
				getGovernorModel().setVirtualGovernor(id);
				callback.updateModel(); // FIXME need it twice to get real gov right
				callback.updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// callback.updateView();
			}

		});
		updateView();
	}

	private VirtualGovernorModel getVirtualGovernorModel(long virtualGovernor) {
		if (getActivity() == null) {
			return null;
		}
		Cursor c = null;
		try {
			c = getActivity().getContentResolver().query(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, SELECTION_VIRT_GOV,
					new String[] { Long.toString(virtualGovernor) },
					VirtualGovernor.SORTORDER_DEFAULT);
			if (c.moveToFirst()) {
				return new VirtualGovernorModel(c);
			}
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}
		return null;
	}

	@Override
	public void updateModel() {
		IGovernorModel governorModel = getGovernorModel();
		VirtualGovernorModel virtGov = getVirtualGovernorModel(governorModel.getVirtualGovernor());
		if (virtGov != null) {
			governorModel.setGov(virtGov.getGov());
			governorModel.setGovernorThresholdUp(virtGov.getGovernorThresholdUp());
			governorModel.setGovernorThresholdDown(virtGov.getGovernorThresholdDown());
			governorModel.setScript(virtGov.getScript());
			governorModel.setPowersaveBias(virtGov.getPowersaveBias());
		} else {
			Logger.e("Cannot load virtual governor");
		}
	}


	@Override
	public void updateView() {
		IGovernorModel governorModel = getGovernorModel();
		if (getActivity() == null || governorModel == null || tvExplainGov == null) {
			return;
		}
		long virtualGovernor = governorModel.getVirtualGovernor();
		spinnerSetGov.setSelectionDbId(virtualGovernor);
		CharSequence description = governorModel.getDescription(getActivity());
		tvExplainGov.setText(description);
	}
}
