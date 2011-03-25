package ch.amana.android.cputuner.view.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.model.IGovernorModel;
import ch.amana.android.cputuner.model.VirtualGovernorModel;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.provider.db.DB.VirtualGovernor;

public class VirtualGovernorFragment extends GovernorBaseFragment {

	private static final String SELECTION_VIRT_GOV = DB.NAME_ID + "=?";
	private Spinner spinnerSetGov;
	private TextView tvExplainGov;
	private Cursor cursor;

	public VirtualGovernorFragment(GovernorFragmentCallback callback, IGovernorModel governor) {
		super(callback, governor);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.virtual_governor_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentActivity act = getActivity();

		tvExplainGov = (TextView) act.findViewById(R.id.tvExplainGov);
		spinnerSetGov = (Spinner) act.findViewById(R.id.SpinnerCpuGov);

		cursor = act.managedQuery(DB.VirtualGovernor.CONTENT_URI, DB.VirtualGovernor.PROJECTION_DEFAULT, null, null, DB.VirtualGovernor.SORTORDER_DEFAULT);
		SimpleCursorAdapter arrayAdapter = new SimpleCursorAdapter(act, android.R.layout.simple_spinner_item, cursor,
				new String[] { DB.VirtualGovernor.NAME_VIRTUAL_GOVERNOR_NAME }, new int[] { android.R.id.text1 });
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				callback.updateModel();
				getGovernorModel().setVirtualGovernor(id);
				callback.updateView();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// callback.updateView();
			}

		});
	}

	private VirtualGovernorModel getVirtualGovernorModel(long virtualGovernor) {
		Cursor c = getActivity().managedQuery(VirtualGovernor.CONTENT_URI, VirtualGovernor.PROJECTION_DEFAULT, SELECTION_VIRT_GOV, new String[] { Long.toString(virtualGovernor) },
				VirtualGovernor.SORTORDER_DEFAULT);
		if (c.moveToFirst()) {
			return new VirtualGovernorModel(c);
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
			Toast.makeText(getActivity(), R.string.msg_cannot_load_virtual_governor, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void updateView() {
		long virtualGovernor = getGovernorModel().getVirtualGovernor();
		GuiUtils.setSpinner(spinnerSetGov, virtualGovernor);
		VirtualGovernorModel virtualGov = getVirtualGovernorModel(virtualGovernor);
		if (virtualGov != null) {
			tvExplainGov.setText(virtualGov.getDescription(getActivity()));
		}
	}

}
