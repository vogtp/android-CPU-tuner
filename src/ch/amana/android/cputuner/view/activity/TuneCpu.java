package ch.amana.android.cputuner.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.Notifier;
import ch.amana.android.cputuner.helper.PulseHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.BatteryHandler;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.PowerProfiles;
import ch.amana.android.cputuner.model.ProfileModel;

public class TuneCpu extends Activity {

	private static final int[] lock = new int[1];
	private CpuTunerReceiver receiver;

	private CpuHandler cpuHandler;
	private TextView tvCpuFreqMax;
	private TextView tvCpuFreqMin;
	private TextView tvBatteryLevel;
	private TextView tvAcPower;
	private TextView tvCurrentProfile;
	private TextView tvCurrentTrigger;
	private TextView labelCpuFreqMin;
	private TextView labelCpuFreqMax;
	private TextView tvMessage;
	private TextView tvBatteryCurrent;
	private TextView tvGovTreshholds;
	private PowerProfiles powerProfiles;
	private TextView tvGorvernor;
	private TextView labelGovTreshholds;
	private TextView labelBatteryCurrent;

	protected class CpuTunerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			acPowerChanged();
			batteryLevelChanged();
			if (Notifier.BROADCAST_TRIGGER_CHANGED.equals(action)
					|| Notifier.BROADCAST_PROFILE_CHANGED.equals(action)) {
				profileChanged();
			}

		}
	}

	public void registerReceiver() {
		synchronized (lock) {
			IntentFilter deviceStatusFilter = new IntentFilter(Notifier.BROADCAST_DEVICESTATUS_CHANGED);
			IntentFilter triggerFilter = new IntentFilter(Notifier.BROADCAST_TRIGGER_CHANGED);
			IntentFilter profileFilter = new IntentFilter(Notifier.BROADCAST_PROFILE_CHANGED);
			receiver = new CpuTunerReceiver();
			registerReceiver(receiver, deviceStatusFilter);
			registerReceiver(receiver, triggerFilter);
			registerReceiver(receiver, profileFilter);
			Logger.i("Registered CpuTunerReceiver");
		}
	}

	public void unregisterReceiver() {
		synchronized (lock) {
			if (receiver != null) {
				try {
					unregisterReceiver(receiver);
					receiver = null;
				} catch (Throwable e) {
					Logger.w("Could not unregister BatteryReceiver", e);
				}
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!SettingsStorage.getInstance().isUserLevelSet()) {
			UserExperianceLevelChooser uec = new UserExperianceLevelChooser(this);
			uec.show();
		}
		
		setContentView(R.layout.cur_info);
		cpuHandler = CpuHandler.getInstance();
		powerProfiles = PowerProfiles.getInstance();


		tvCurrentTrigger = (TextView) findViewById(R.id.tvCurrentTrigger);
		tvCurrentProfile = (TextView) findViewById(R.id.tvCurrentProfile);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvAcPower = (TextView) findViewById(R.id.tvAcPower);
		tvBatteryCurrent = (TextView) findViewById(R.id.tvBatteryCurrent);
		tvBatteryLevel = (TextView) findViewById(R.id.tvBatteryLevel);
		tvCpuFreqMax = (TextView) findViewById(R.id.tvCpuFreqMax);
		tvCpuFreqMin = (TextView) findViewById(R.id.tvCpuFreqMin);
		labelCpuFreqMin = (TextView) findViewById(R.id.labelCpuFreqMin);
		labelCpuFreqMax = (TextView) findViewById(R.id.labelCpuFreqMax);
		tvGorvernor = (TextView) findViewById(R.id.tvGovernor);
		tvGovTreshholds = (TextView) findViewById(R.id.tvGovTreshholds);
		labelGovTreshholds = (TextView) findViewById(R.id.labelGovTreshholds);
		labelBatteryCurrent = (TextView) findViewById(R.id.labelBatteryCurrent);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
		updateView();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	private void updateView() {
		batteryLevelChanged();
		profileChanged();
		acPowerChanged();

		if (SettingsStorage.getInstance().isEnableBeta()) {
			if (RootHandler.NOT_AVAILABLE.equals(cpuHandler.getCurCpuGov())
					|| cpuHandler.getMaxCpuFreq() < 1 || cpuHandler.getMinCpuFreq() < 1) {
				if (SettingsStorage.getInstance().isDisableDisplayIssues()) {
					if (tvMessage != null) {
						LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutMessage);
						ll.removeView(tvMessage);
						tvMessage = null;
					}
				} else {
					getMessageTextView().setText(R.string.msg_found_some_issues);
				}
			}
		}
	}

	private void batteryLevelChanged() {
		StringBuilder bat = new StringBuilder();
		bat.append(powerProfiles.getBatteryLevel()).append("%");
		bat.append(" (");
		if (powerProfiles.isBatteryHot()) {
			bat.append(R.string.label_hot).append(" ");
		}
		bat.append(powerProfiles.getBatteryTemperature()).append(" °C)");
		tvBatteryLevel.setText(bat.toString());
		StringBuilder currentText = new StringBuilder();
		int currentNow = BatteryHandler.getBatteryCurrentNow();
		if (currentNow != BatteryHandler.NO_VALUE_INT) {
			currentText.append(BatteryHandler.getBatteryCurrentNow()).append(" mA/h");
		}
		int currentAvg = BatteryHandler.getBatteryCurrentAverage();
		if (currentAvg != BatteryHandler.NO_VALUE_INT && currentAvg != currentNow) {
			currentText.append(" (").append(getString(R.string.label_avgerage)).append(" ").append(BatteryHandler.getBatteryCurrentAverage()).append(" mA/h)");
		}
		if (currentText.length() > 0) {
			labelBatteryCurrent.setVisibility(View.VISIBLE);
			tvBatteryCurrent.setVisibility(View.VISIBLE);
			tvBatteryCurrent.setText(currentText.toString());
		} else {
			labelBatteryCurrent.setVisibility(View.INVISIBLE);
			tvBatteryCurrent.setVisibility(View.INVISIBLE);
		}
	}

	private void profileChanged() {
		if (SettingsStorage.getInstance().isEnableProfiles()) {
			CharSequence profile = powerProfiles.getCurrentProfileName();
			if (PulseHelper.getInstance(this).isPulsing()) {
				int res = PulseHelper.getInstance(this).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
				profile = profile + " " + getString(res);
			}
			tvCurrentProfile.setText(profile);
			tvCurrentTrigger.setText(powerProfiles.getCurrentTriggerName());
		} else {
			tvCurrentProfile.setText(R.string.notEnabled);
			tvCurrentTrigger.setText(R.string.notEnabled);
		}

		String curCpuGov = cpuHandler.getCurCpuGov();
		tvGorvernor.setText(curCpuGov);
		if (CpuHandler.GOV_USERSPACE.equals(curCpuGov)) {
			labelCpuFreqMax.setText(R.string.labelCpuFreq);
			labelCpuFreqMin.setVisibility(View.INVISIBLE);
			tvCpuFreqMin.setVisibility(View.INVISIBLE);

		} else {
			labelCpuFreqMax.setText(R.string.labelMax);
			labelCpuFreqMin.setVisibility(View.VISIBLE);
			tvCpuFreqMin.setVisibility(View.VISIBLE);
			tvCpuFreqMin.setText(ProfileModel.convertFreq2GHz(cpuHandler.getMinCpuFreq()));
		}
		tvCpuFreqMax.setText(ProfileModel.convertFreq2GHz(cpuHandler.getMaxCpuFreq()));

		int govThresholdUp = cpuHandler.getGovThresholdUp();
		int govThresholdDown = cpuHandler.getGovThresholdDown();
		StringBuilder sb = new StringBuilder();
		if (govThresholdUp > 0) {
			sb.append(getString(R.string.label_tresh_up)).append(" ").append(govThresholdUp).append("% ");
		}
		if (govThresholdDown > 0) {
			sb.append(getString(R.string.label_tresh_down)).append(" ").append(govThresholdDown).append("%");
		}
		if (sb.length() > 0) {
			labelGovTreshholds.setVisibility(View.VISIBLE);
			tvGovTreshholds.setVisibility(View.VISIBLE);
			tvGovTreshholds.setText(sb.toString());
		} else {
			labelGovTreshholds.setVisibility(View.INVISIBLE);
			tvGovTreshholds.setVisibility(View.INVISIBLE);
		}
	}

	private void acPowerChanged() {
		tvAcPower.setText(getString(powerProfiles.isAcPower() ? R.string.yes : R.string.no));
	}

	private TextView getMessageTextView() {
		if (tvMessage == null) {
			tvMessage = new TextView(this);
			tvMessage.setTextColor(Color.RED);
			// tvMessage.setTextSize(18);
			tvMessage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startActivity(new Intent(TuneCpu.this, CapabilityCheckerActivity.class));
				}
			});
			LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayoutMessage);
			ll.addView(tvMessage);
		}
		return tvMessage;
	}
}