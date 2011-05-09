package ch.amana.android.cputuner.view.preference;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.ProfileModel;

public class FindFrequenciesActivity extends Activity {

	private EditText etStart;
	private EditText etEnd;
	private EditText etStep;
	private TextView tvResults;
	// private TextView tvCur;
	private Button buFindFrequencies;

	private class CheckForCpuFrequencies extends AsyncTask<Void, Integer, SortedSet<Integer>> {

		private final File scalingMaxFile = new File(CpuHandler.CPU_DIR, CpuHandler.SCALING_MAX_FREQ);
		private final int minFreq;
		private final int maxFreq;
		private final int step;
		private final FindFrequenciesActivity act;
		private ProgressDialog pd;

		public CheckForCpuFrequencies(FindFrequenciesActivity act, int minFreq, int maxFreq, int step) {
			super();
			this.act = act;
			this.minFreq = minFreq;
			this.maxFreq = maxFreq;
			this.step = step;
		}

		@Override
		protected SortedSet<Integer> doInBackground(Void... params) {
			CpuHandler cpuHandler = CpuHandler.getInstance();
			SortedSet<Integer> freqs = new TreeSet<Integer>();
			// StringBuilder sb = new StringBuilder();
			PowerProfiles.setUpdateTrigger(false);
			ProfileModel cpuSettings = cpuHandler.getCurrentCpuSettings();
			cpuHandler.setCurGov(CpuHandler.GOV_ONDEMAND);

			for (int i = minFreq; i < maxFreq; i += step) {
				act.updateProgress(i);
				if (pd != null) {
					// pd.setMessage(i + "");
					pd.setProgress(i);
				}
				RootHandler.writeFile(scalingMaxFile, i + "");
				int f = cpuHandler.getMaxCpuFreq();
				// int cf = cpuHandler.getMaxCpuFreq();
				// n1: 245000 384000 422400 460800 499200 537600 576000 614400
				// 652800 691200 729600 768000 806400 844800 883200 921600
				// 960000 998400 1036800 1075200 1113600
				if (f == i) {
					freqs.add(i);
					Logger.i("Found frequency: " + i);
				}
			}
			PowerProfiles.setUpdateTrigger(true);
			cpuHandler.applyCpuSettings(cpuSettings);
			return freqs;
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(act, ProgressDialog.STYLE_HORIZONTAL);
			pd.setTitle("Scanning frequencies");
			pd.setProgress(0);
			pd.setMax(maxFreq);
			pd.show();
			act.startThread();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(SortedSet<Integer> result) {
			if (pd != null) {
				pd.dismiss();
				pd = null;
			}
			act.finishedThread(result);
			super.onPostExecute(result);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.find_frequencies);
		etStart = (EditText) findViewById(R.id.etStart);
		etEnd = (EditText) findViewById(R.id.etEnd);
		etStep = (EditText) findViewById(R.id.etStep);
		tvResults = (TextView) findViewById(R.id.tvResults);
		// tvCur = (TextView) findViewById(R.id.tvCur);

		CpuHandler cpuHandler = CpuHandler.getInstance();
		int min = Math.min(cpuHandler.getMinCpuFreq(), cpuHandler.getCpuInfoMinFreq());
		if (min > 0) {
			etStart.setText(min + "");
		} else {
			etStart.setText("422400");
		}

		int max = Math.max(cpuHandler.getMaxCpuFreq(), cpuHandler.getCpuInfoMaxFreq());
		if (max > 0) {
			etEnd.setText(max + "");
		} else {
			etEnd.setText("460800");
		}

		buFindFrequencies = (Button) findViewById(R.id.buFindFrequencies);
		buFindFrequencies.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					int min = Integer.parseInt(etStart.getText().toString());
					int max = Integer.parseInt(etEnd.getText().toString());
					int step = Integer.parseInt(etStep.getText().toString());
					CheckForCpuFrequencies checker = new CheckForCpuFrequencies(FindFrequenciesActivity.this, min, max, step);
					checker.execute();
				} catch (Exception e) {
					Logger.w("Cannot find freqs", e);
				}
			}
		});
	}

	public void startThread() {
		// buFindFrequencies.setEnabled(false);
	}

	public void finishedThread(SortedSet<Integer> result) {
		StringBuilder sb = new StringBuilder("Found frequencies:\n");
		for (Integer freq : result) {
			sb.append(freq).append(" ");
		}
		tvResults.setText(sb.toString());
		// buFindFrequencies.setEnabled(true);

	}

	protected void updateProgress(int i) {

	}
}
