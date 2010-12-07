package ch.amana.android.cputuner.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.RootHandler;
import ch.amana.android.cputuner.model.CpuModel;

public class CapabilityChecker {

	public static final String NO_GOVERNORS = "No governors found";
	public static final String NO_FREQUENCIES = "Not enough frequencies found";

	public enum CheckResult {
		NOT_CHECKED,
		SUCCESS,
		FAILURE,
		DOES_NOT_APPLY,
		CANNOT_CHECK;
	}

	public class GovernorResult {
		public String governor;

		public CheckResult writeGovernor = CheckResult.NOT_CHECKED;
		public CheckResult readGovernor = CheckResult.NOT_CHECKED;

		public CheckResult writeMinFreq = CheckResult.NOT_CHECKED;
		public CheckResult readMinFreq = CheckResult.NOT_CHECKED;

		public CheckResult writeMaxFreq = CheckResult.NOT_CHECKED;
		public CheckResult readMaxFreq = CheckResult.NOT_CHECKED;

		public CheckResult writeUpThreshold = CheckResult.NOT_CHECKED;
		public CheckResult readUpThreshold = CheckResult.NOT_CHECKED;

		public CheckResult writeDownThreshold = CheckResult.NOT_CHECKED;
		public CheckResult readDownThreshold = CheckResult.NOT_CHECKED;

		private GovernorResult() {
			super();
		}

		public GovernorResult(String governor) {
			this();
			this.governor = governor;
		}

		private boolean hasCheckFailed(CheckResult res) {
			return res == CheckResult.FAILURE;
		}

		public boolean hasFailure() {
			boolean notOk = hasCheckFailed(writeGovernor) || hasCheckFailed(readGovernor) || hasCheckFailed(writeMinFreq) || hasCheckFailed(readMinFreq) ||
					hasCheckFailed(writeMaxFreq) || hasCheckFailed(readMaxFreq) || hasCheckFailed(writeUpThreshold) || hasCheckFailed(readUpThreshold) ||
					hasCheckFailed(writeDownThreshold) || hasCheckFailed(readDownThreshold);
			return notOk;
		}

		private boolean hasCheckCannotCheck(CheckResult res) {
			return res == CheckResult.CANNOT_CHECK;
		}

		public boolean hasCannotCheck() {
			boolean notOk = hasCheckCannotCheck(writeGovernor) || hasCheckCannotCheck(readGovernor) || hasCheckCannotCheck(writeMinFreq)
					|| hasCheckCannotCheck(readMinFreq) ||
					hasCheckCannotCheck(writeMaxFreq) || hasCheckCannotCheck(readMaxFreq) || hasCheckCannotCheck(writeUpThreshold)
					|| hasCheckCannotCheck(readUpThreshold) ||
					hasCheckCannotCheck(writeDownThreshold) || hasCheckCannotCheck(readDownThreshold);
			return notOk;
		}

		private boolean hasCheckNotCheck(CheckResult res) {
			return res == CheckResult.NOT_CHECKED;
		}

		public boolean hasNotChecked() {
			boolean notOk = hasCheckNotCheck(writeGovernor) || hasCheckNotCheck(readGovernor) || hasCheckNotCheck(writeMinFreq)
					|| hasCheckNotCheck(readMinFreq) ||
					hasCheckNotCheck(writeMaxFreq) || hasCheckNotCheck(readMaxFreq) || hasCheckNotCheck(writeUpThreshold)
					|| hasCheckNotCheck(readUpThreshold) ||
					hasCheckNotCheck(writeDownThreshold) || hasCheckNotCheck(readDownThreshold);
			return notOk;
		}

		public boolean hasIssues() {
			return hasFailure();
		}

		public CheckResult getWorstIssue() {
			if (hasFailure()) {
				return CheckResult.FAILURE;
			}
			if (hasCannotCheck()) {
				return CheckResult.CANNOT_CHECK;
			}
			if (hasNotChecked()) {
				return CheckResult.NOT_CHECKED;
			}
			return CheckResult.SUCCESS;
		}

	}

	private final CpuHandler cpuHandler;
	private int[] freqs = null;
	private static CapabilityChecker instance;
	private boolean rooted = false;
	private final CpuModel currentCpuSettings;
	private Map<String, GovernorResult> govChecks = null;
	private String[] governors = null;

	private int minFreq;

	private int maxFreq;

	private int minCheckFreq;

	private int maxCheckFreq;

	public CapabilityChecker() {
		super();
		boolean powerUser = SettingsStorage.getInstance().isPowerUser();
		cpuHandler = new CpuHandler();
		currentCpuSettings = cpuHandler.getCurrentCpuSettings();
		try {
			SettingsStorage.getInstance().enablePowerUser = true;
			rooted = RootHandler.isRoot();

			governors = cpuHandler.getAvailCpuGov();
			govChecks = new HashMap<String, CapabilityChecker.GovernorResult>(governors.length);

			if (RootHandler.NOT_AVAILABLE.equals(governors[0])) {
				GovernorResult res = new GovernorResult(NO_GOVERNORS);
				res.readGovernor = CheckResult.FAILURE;
				res.writeGovernor = CheckResult.FAILURE;
				govChecks.put(NO_GOVERNORS, res);
				return;
			}

			freqs = cpuHandler.getAvailCpuFreq(true);
			minFreq = freqs[0];
			maxFreq = freqs[freqs.length - 1];
			if (freqs.length < 2 || minFreq == maxFreq) {
				GovernorResult res = new GovernorResult(NO_FREQUENCIES);
				res.readMaxFreq = CheckResult.CANNOT_CHECK;
				res.writeMaxFreq = CheckResult.CANNOT_CHECK;
				res.readMinFreq = CheckResult.CANNOT_CHECK;
				res.writeMinFreq = CheckResult.CANNOT_CHECK;
				govChecks.put(NO_FREQUENCIES, res);
				return;
			}
			if (freqs.length > 3) {
				minCheckFreq = freqs[1];
				maxCheckFreq = freqs[freqs.length - 2];
			}

			for (int i = 0; i < governors.length; i++) {
				checkGov(governors[i]);
			}
		} catch (Throwable t) {
			Logger.w("Capability check threw ", t);
		} finally {
			SettingsStorage.getInstance().enablePowerUser = powerUser;
			cpuHandler.applyCpuSettings(currentCpuSettings);
		}
	}

	private void checkGov(String gov) {
		RootHandler.writeLog("********************************************");
		RootHandler.writeLog("checking governor: " + gov);
		RootHandler.writeLog("********************************************");
		GovernorResult result = new GovernorResult(gov);
		govChecks.put(gov, result);

		RootHandler.writeLog("*** check setting governor ***");
		cpuHandler.setCurGov(gov);
		String activeGov = cpuHandler.getCurCpuGov();
		if (RootHandler.NOT_AVAILABLE.equals(activeGov)) {
			result.readGovernor = CheckResult.FAILURE;
			result.writeGovernor = CheckResult.FAILURE;
		} else {
			if (activeGov.equals(gov)) {
				result.readGovernor = CheckResult.SUCCESS;
				result.writeGovernor = CheckResult.SUCCESS;
			} else {
				result.readGovernor = CheckResult.SUCCESS;
				result.writeGovernor = CheckResult.FAILURE;
			}
		}

		if (CpuHandler.GOV_USERSPACE.equals(gov)) {
			result.readMinFreq = CheckResult.DOES_NOT_APPLY;
			result.writeMinFreq = CheckResult.DOES_NOT_APPLY;
			checkUserCpuFreq(result);
		} else {
			checkMaxCpuFreq(result);
			checkMinCpuFreq(result);
		}

		if (cpuHandler.hasUpThreshold()) {
			checkUpThreshold(result);
		} else {
			result.readUpThreshold = CheckResult.DOES_NOT_APPLY;
			result.writeUpThreshold = CheckResult.DOES_NOT_APPLY;
		}

		if (cpuHandler.hasDownThreshold()) {
			checkDownThreshold(result);
		} else {
			result.readDownThreshold = CheckResult.DOES_NOT_APPLY;
			result.writeDownThreshold = CheckResult.DOES_NOT_APPLY;
		}

	}

	private void checkDownThreshold(GovernorResult result) {
		RootHandler.writeLog("*** check setting down threshold of " + result.governor + " ***");
		int thresh = cpuHandler.getGovThresholdDown();
		if (thresh < 1) {
			result.readDownThreshold = CheckResult.FAILURE;
			return;
		}
		result.readDownThreshold = CheckResult.SUCCESS;
		cpuHandler.setGovThresholdUp(99);
		RootHandler.writeLog("*** Writing down threshold 1 ***");
		cpuHandler.setGovThresholdDown(80);
		int readThresh = cpuHandler.getGovThresholdDown();
		if (readThresh != 80) {
			result.writeDownThreshold = CheckResult.FAILURE;
			return;
		}
		RootHandler.writeLog("*** Writing down threshold 2 ***");
		cpuHandler.setGovThresholdDown(90);
		readThresh = cpuHandler.getGovThresholdDown();
		if (readThresh == 90) {
			result.writeDownThreshold = CheckResult.SUCCESS;
		} else {
			result.writeDownThreshold = CheckResult.FAILURE;
		}
	}

	private void checkUpThreshold(GovernorResult result) {
		RootHandler.writeLog("*** check setting up threshold of " + result.governor + " ***");
		int thresh = cpuHandler.getGovThresholdUp();
		if (thresh < 1) {
			result.readUpThreshold = CheckResult.FAILURE;
			return;
		}
		result.readUpThreshold = CheckResult.SUCCESS;
		cpuHandler.setGovThresholdDown(95);
		RootHandler.writeLog("*** Writing up threshold 1 ***");
		cpuHandler.setGovThresholdUp(98);
		int readThresh = cpuHandler.getGovThresholdUp();
		if (readThresh != 98) {
			result.writeUpThreshold = CheckResult.FAILURE;
			return;
		}
		RootHandler.writeLog("*** Writing up threshold 2 ***");
		cpuHandler.setGovThresholdUp(99);
		readThresh = cpuHandler.getGovThresholdUp();
		if (readThresh == 99) {
			result.writeUpThreshold = CheckResult.SUCCESS;
		} else {
			result.writeUpThreshold = CheckResult.FAILURE;
		}

	}

	private void checkMinCpuFreq(GovernorResult result) {
		RootHandler.writeLog("*** check setting min frequencies of " + result.governor + " ***");
		int min = cpuHandler.getMinCpuFreq();
		if (min < 1) {
			result.readMinFreq = CheckResult.FAILURE;
			return;
		}
		result.readMinFreq = CheckResult.SUCCESS;
		cpuHandler.setMaxCpuFreq(maxFreq);
		RootHandler.writeLog("*** Writing min frequency 1 ***");
		cpuHandler.setMinCpuFreq(minFreq);
		int readFreq = cpuHandler.getMinCpuFreq();
		if (readFreq != minFreq) {
			result.writeMinFreq = CheckResult.FAILURE;
			return;
		}
		RootHandler.writeLog("*** Writing min frequency 2 ***");
		cpuHandler.setMinCpuFreq(minCheckFreq);
		readFreq = cpuHandler.getMinCpuFreq();
		if (readFreq == minCheckFreq) {
			result.writeMinFreq = CheckResult.SUCCESS;
		} else {
			result.writeMinFreq = CheckResult.FAILURE;
		}

	}

	private void checkMaxCpuFreq(GovernorResult result) {
		RootHandler.writeLog("*** check setting max frequencies of " + result.governor + " ***");
		int max = cpuHandler.getMaxCpuFreq();
		if (max < 1) {
			result.readMaxFreq = CheckResult.FAILURE;
			return;
		}
		result.readMaxFreq = CheckResult.SUCCESS;
		cpuHandler.setMinCpuFreq(minFreq);
		RootHandler.writeLog("*** Writing max frequency 1 ***");
		cpuHandler.setMaxCpuFreq(maxFreq);
		int readFreq = cpuHandler.getMaxCpuFreq();
		if (readFreq != maxFreq) {
			result.writeMaxFreq = CheckResult.FAILURE;
			return;
		}
		RootHandler.writeLog("*** Writing max frequency 2 ***");
		cpuHandler.setMaxCpuFreq(maxCheckFreq);
		readFreq = cpuHandler.getMaxCpuFreq();
		if (readFreq == maxCheckFreq) {
			result.writeMaxFreq = CheckResult.SUCCESS;
		} else {
			result.writeMaxFreq = CheckResult.FAILURE;
		}

	}

	private void checkUserCpuFreq(GovernorResult result) {
		RootHandler.writeLog("*** check setting userspace frequencies ***");
		int activeFreq = cpuHandler.getUserCpuFreq();
		if (activeFreq < 1) {
			result.readMaxFreq = CheckResult.FAILURE;
			return;
		}
		result.readMaxFreq = CheckResult.SUCCESS;
		RootHandler.writeLog("*** Writing userfrequency 1 ***");
		cpuHandler.setUserCpuFreq(maxFreq);
		int readFreq = cpuHandler.getUserCpuFreq();
		if (readFreq != maxFreq) {
			result.writeMaxFreq = CheckResult.FAILURE;
			return;
		}
		RootHandler.writeLog("*** Writing userfrequency 2 ***");
		cpuHandler.setUserCpuFreq(maxCheckFreq);
		readFreq = cpuHandler.getUserCpuFreq();
		if (readFreq == maxCheckFreq) {
			result.writeMaxFreq = CheckResult.SUCCESS;
		} else {
			result.writeMaxFreq = CheckResult.FAILURE;
		}

	}

	public boolean hasIssues() {
		if (!rooted) {
			return true;
		}
		for (GovernorResult res : govChecks.values()) {
			if (res.hasIssues()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CapabilityChecker:\n");
		sb.append("rooted: ").append(rooted ? "Yes" : "No").append("\n");

		for (Iterator<String> itr = govChecks.keySet().iterator(); itr.hasNext();) {
			String g = itr.next();
			getGovernorResults(sb, g);
		}

		return sb.toString();
	}

	private void getGovernorResults(StringBuilder sb, String g) {
		GovernorResult gr = govChecks.get(g);
		sb.append("Governor: ").append(g).append("\n");
		sb.append("  Governor: read: ").append(checkresultToString(gr.readGovernor));
		sb.append(" - write: ").append(checkresultToString(gr.writeGovernor)).append("\n");
		sb.append("  Min Freq: read: ").append(checkresultToString(gr.readMinFreq));
		sb.append(" - write: ").append(checkresultToString(gr.writeMinFreq)).append("\n");
		sb.append("  Max Freq: read: ").append(checkresultToString(gr.readMaxFreq));
		sb.append(" - write: ").append(checkresultToString(gr.writeMaxFreq)).append("\n");
		sb.append("  Up Threshold: read: ").append(checkresultToString(gr.readUpThreshold));
		sb.append(" - write: ").append(checkresultToString(gr.writeUpThreshold)).append("\n");
		sb.append("  DownThreshold: read: ").append(checkresultToString(gr.readDownThreshold));
		sb.append(" - write: ").append(checkresultToString(gr.writeDownThreshold)).append("\n");
	}

	private String checkresultToString(CheckResult res) {
		switch (res) {
		case NOT_CHECKED:
			return "not checked";
		case SUCCESS:
			return "success";
		case FAILURE:
			return "failure";
		case DOES_NOT_APPLY:
			return "does not apply";
		case CANNOT_CHECK:
			return "cannot check";
		}
		return "unknown";
	}

	public boolean isRooted() {
		return rooted;
	}

	public static CapabilityChecker getInstance() {
		return getInstance(false);
	}

	public static CapabilityChecker getInstance(boolean recheck) {
		if (instance == null || recheck) {
			instance = new CapabilityChecker();
		}
		return instance;
	}

	public CharSequence getSummary() {
		if (!rooted) {
			return "CPU tuner does not have root access!\n All CPU tuning related features will not work.\n\nDid root your device?\nDid you grant CPU tuner root access?";
		} else if (hasIssues()) {
			return "Found some issues... Some features might not work.";
		}
		return "No issues found...";
	}

	public Collection<GovernorResult> getGovernorsCheckResults() {
		return govChecks.values();
	}

}
