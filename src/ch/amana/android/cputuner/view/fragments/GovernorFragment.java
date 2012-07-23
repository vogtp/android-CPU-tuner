package ch.amana.android.cputuner.view.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.GovernorConfigHelper;
import ch.amana.android.cputuner.helper.GovernorConfigHelper.GovernorConfig;
import ch.amana.android.cputuner.helper.GuiUtils;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.CpuHandlerMulticore;
import ch.amana.android.cputuner.model.IGovernorModel;
import ch.amana.android.cputuner.view.widget.SpinnerWrapper;

public class GovernorFragment extends GovernorBaseFragment {

	private TextView tvExplainGov;
	private TextView labelGovThreshUp;
	private TextView labelGovThreshDown;
	private EditText etGovTreshUp;
	private EditText etGovTreshDown;
	private SpinnerWrapper spinnerSetGov;
	private EditText etScript;
	private LinearLayout llFragmentTop;
	private String[] availCpuGovs;
	private String origThreshUp;
	private String origThreshDown;
	private SeekBar sbPowersaveBias;
	private boolean disableScript;
	private LinearLayout llPowersaveBias;
	private LinearLayout llGovernorThresholds;
	private SpinnerWrapper spUseCpus;
	private int numberOfCpus;

	private CpuHandler cpuHandler;
	private LinearLayout llUseCpus;

	public GovernorFragment() {
		super();
	}

	public GovernorFragment(GovernorFragmentCallback callback, IGovernorModel governor) {
		this(callback, governor, false);
	}

	public GovernorFragment(GovernorFragmentCallback callback, IGovernorModel governor, boolean disableScript) {
		super(callback, governor);
		this.origThreshUp = governor.getGovernorThresholdUp() + "";
		this.origThreshDown = governor.getGovernorThresholdDown() + "";
		this.disableScript = disableScript;
		this.cpuHandler = CpuHandler.getInstance();
		this.availCpuGovs = cpuHandler.getAvailCpuGov();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.governor_fragment, container, false);
		llFragmentTop = (LinearLayout) v.findViewById(R.id.llGovernorFragment);
		tvExplainGov = (TextView) v.findViewById(R.id.tvExplainGov);
		llGovernorThresholds = (LinearLayout) v.findViewById(R.id.llGovernorThresholds);
		labelGovThreshUp = (TextView) v.findViewById(R.id.labelGovThreshUp);
		labelGovThreshDown = (TextView) v.findViewById(R.id.labelGovThreshDown);
		etGovTreshUp = (EditText) v.findViewById(R.id.etGovTreshUp);
		etGovTreshDown = (EditText) v.findViewById(R.id.etGovTreshDown);
		spinnerSetGov = new SpinnerWrapper((Spinner) v.findViewById(R.id.SpinnerCpuGov));
		etScript = (EditText) v.findViewById(R.id.etScript);
		llPowersaveBias = (LinearLayout) v.findViewById(R.id.llPowersaveBias);
		sbPowersaveBias = (SeekBar) v.findViewById(R.id.sbPowersaveBias);
		llUseCpus = (LinearLayout) v.findViewById(R.id.llUseCpus);
		spUseCpus = new SpinnerWrapper((Spinner) v.findViewById(R.id.spUseCpus));
		if (disableScript || !SettingsStorage.getInstance().isEnableScriptOnProfileChange()) {
			llFragmentTop.removeView(v.findViewById(R.id.llScript));
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		FragmentActivity act = getActivity();
		if (cpuHandler == null) {
			this.cpuHandler = CpuHandler.getInstance();
			this.availCpuGovs = cpuHandler.getAvailCpuGov();
		}

		numberOfCpus = cpuHandler.getNumberOfCpus();
		if (cpuHandler instanceof CpuHandlerMulticore) {
			ArrayAdapter<Integer> cpuAdapter = new ArrayAdapter<Integer>(act, android.R.layout.simple_spinner_item);
			cpuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			for (int i = numberOfCpus; i >= 1; i--) {
				cpuAdapter.add(i);
			}
			spUseCpus.setAdapter(cpuAdapter);
			spUseCpus.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

					final int num = numberOfCpus - position;
					final IGovernorModel govModel = getGovernorModel();
					if (num != numberOfCpus && num != govModel.getUseNumberOfCpus()) {
						OnClickListener yesClick = new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								govModel.setUseNumberOfCpus(num);
							}
						};
						OnClickListener noClick = new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								govModel.setUseNumberOfCpus(numberOfCpus);
								updateView();
							}
						};
						new AlertDialog.Builder(getActivity())
								.setIconAttribute(android.R.attr.alertDialogIcon)
								.setTitle(R.string.dia_title_switch_off_cpus)
								.setMessage(R.string.dia_msg_switch_off_cpus)
								.setPositiveButton(R.string.dia_yes_switch_off_cpus, yesClick)
								.setNegativeButton(R.string.dia_no_switch_off_cpus, noClick)
								.create().show();
					} else {
						govModel.setUseNumberOfCpus(num);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
		} else {
			llFragmentTop.removeView(act.findViewById(R.id.llUseCpus));
			spUseCpus = null;
		}

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(act, android.R.layout.simple_spinner_item, availCpuGovs);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerSetGov.setAdapter(arrayAdapter);
		spinnerSetGov.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (callback != null) {
					callback.updateModel();
				}
				String gov = parent.getItemAtPosition(pos).toString();
				getGovernorModel().setGov(gov);
				if (callback != null) {
					callback.updateView();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				if (callback != null) {
					callback.updateView();
				}
			}

		});

		OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && etGovTreshUp.getVisibility() == View.VISIBLE) {
					String upthresh = etGovTreshUp.getText().toString();
					String downthresh = etGovTreshDown.getText().toString();
					try {
						int up = Integer.parseInt(upthresh);
						int down = 0;
						if (etGovTreshDown.getVisibility() == View.VISIBLE) {
							down = Integer.parseInt(downthresh);
						}
						if (up > 100 || up < 0) {
							Toast.makeText(getActivity(), R.string.msg_up_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshUp.setText(origThreshUp);
						}
						if (down > 100 || down < 0) {
							Toast.makeText(getActivity(), R.string.msg_down_threshhold_has_to_be_between_0_and_100, Toast.LENGTH_LONG).show();
							etGovTreshDown.setText(origThreshDown);
						}
						if (up > down) {
							// all OK
							return;
						}
						Toast.makeText(getActivity(), R.string.msg_up_threshhold_smaler_than_the_down_threshold, Toast.LENGTH_LONG).show();
						down = up - 10;
						etGovTreshDown.setText(down + "");
					} catch (Exception e) {
						Toast.makeText(getActivity(), R.string.msg_threshhold_NaN, Toast.LENGTH_LONG).show();
					}
				}

			}
		};
		etGovTreshUp.setOnFocusChangeListener(onFocusChangeListener);
		etGovTreshDown.setOnFocusChangeListener(onFocusChangeListener);
	}

	@Override
	public void updateModel() {
		IGovernorModel governorModel = getGovernorModel();
		governorModel.setGovernorThresholdUp(etGovTreshUp.getText().toString());
		governorModel.setGovernorThresholdDown(etGovTreshDown.getText().toString());
		if (SettingsStorage.getInstance().isEnableScriptOnProfileChange()) {
			governorModel.setScript(etScript.getText().toString());
		} else {
			governorModel.setScript("");
		}
		governorModel.setPowersaveBias(sbPowersaveBias.getProgress());
	}

	@Override
	public void updateView() {
		if (spinnerSetGov == null) {
			// we have no been created yet
			return;
		}
		IGovernorModel governorModel = getGovernorModel();
		String curGov = governorModel.getGov();
		if (spinnerSetGov == null) {
			// we have no been created yet
			return;
		}
		for (int i = 0; i < availCpuGovs.length; i++) {
			if (curGov.equals(availCpuGovs[i])) {
				spinnerSetGov.setSelection(i);
			}
		}
		tvExplainGov.setText(GuiUtils.getExplainGovernor(getActivity(), curGov));
		boolean powerUser = SettingsStorage.getInstance().isPowerUser();
		if (powerUser) {
			etScript.setText(governorModel.getScript());
		}
		sbPowersaveBias.setProgress(governorModel.getPowersaveBias());
		updateGovernorFeatures();

		int position = numberOfCpus - governorModel.getUseNumberOfCpus();
		if (powerUser) {
			llUseCpus.setVisibility(View.VISIBLE);
			if (spUseCpus != null && position < spUseCpus.getAdapter().getCount()) {
				spUseCpus.setSelection(position);
			}
		} else {
			llUseCpus.setVisibility(View.GONE);
		}
	}

	private void updateGovernorFeatures() {
		IGovernorModel governorModel = getGovernorModel();
		GovernorConfig governorConfig = GovernorConfigHelper.getGovernorConfig(governorModel.getGov());

		int up = governorModel.getGovernorThresholdUp();
		int down = governorModel.getGovernorThresholdDown();

		if (governorConfig.hasThreshholdUpFeature()) {
			llGovernorThresholds.setVisibility(View.VISIBLE);
			labelGovThreshUp.setVisibility(View.VISIBLE);
			etGovTreshUp.setVisibility(View.VISIBLE);
			if (up < 2) {
				up = Integer.parseInt(origThreshUp);
			}
			if (up < 2) {
				up = 90;
			}
			etGovTreshUp.setText(up + "");
		} else {
			llGovernorThresholds.setVisibility(View.GONE);
			governorModel.setGovernorThresholdUp(0);
			labelGovThreshUp.setVisibility(View.GONE);
			etGovTreshUp.setVisibility(View.GONE);
			etGovTreshUp.setText("-1");
		}

		if (governorConfig.hasThreshholdDownFeature()) {
			labelGovThreshDown.setVisibility(View.VISIBLE);
			etGovTreshDown.setVisibility(View.VISIBLE);
			if (down < 1) {
				down = Integer.parseInt(origThreshDown);
			}
			if (down >= up || down < 1) {
				if (up > 30) {
					down = up - 10;
				} else {
					down = up - 1;
				}
			}
			etGovTreshDown.setText(Integer.toString(down));
		} else {
			governorModel.setGovernorThresholdDown(0);
			labelGovThreshDown.setVisibility(View.GONE);
			etGovTreshDown.setVisibility(View.GONE);
			etGovTreshDown.setText("-1");
		}

		if (governorConfig.hasPowersaveBias()) {
			llPowersaveBias.setVisibility(View.VISIBLE);
		} else {
			llPowersaveBias.setVisibility(View.GONE);
		}

	}
}
