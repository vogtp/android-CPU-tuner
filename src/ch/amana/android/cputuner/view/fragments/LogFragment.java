package ch.amana.android.cputuner.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;

public class LogFragment extends PagerFragment {

	private TextView tvStats;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.stats, container, false);
		tvStats = (TextView) v.findViewById(R.id.tvStats);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tvStats.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateView();
			}
		});
	}

	@Override
	public void onResume() {
		updateView();
		super.onResume();
	}

	private void updateView() {
		tvStats.setText(Logger.getLog(getActivity()));
	}



}
