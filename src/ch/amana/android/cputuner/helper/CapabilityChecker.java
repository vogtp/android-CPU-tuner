package ch.amana.android.cputuner.helper;

import android.util.Log;
import ch.amana.android.cputuner.hw.CpuHandler;
import ch.amana.android.cputuner.hw.HardwareHandler;
import ch.amana.android.cputuner.hw.RootHandler;

public class CapabilityChecker {

	private static CapabilityChecker instance;
	private boolean rooted = false;
	private boolean writeGovernor = false;
	private boolean writeMinFreq = false;
	private boolean writeMaxFreq = false;
	private boolean writeUserCpuFreq = false;
	private boolean readUserCpuFreq = false;
	private boolean readGovernor = false;
	private boolean readMaxFreq = false;
	private boolean readMinFreq = false;
	private final CpuHandler cpuHandler;
	private final int[] freqs;

	public CapabilityChecker() {
		super();
		rooted = RootHandler.isRoot();
		// if (rooted) {
		cpuHandler = new CpuHandler();
		freqs = cpuHandler.getAvailCpuFreq();
		checkSetGovernor();
		checkSetMinFreq();
		checkSetMaxFreq();
		if (cpuHandler.hasGovernor(CpuHandler.GOV_USERSPACE)) {
			checkSetUserCpuFreq();
		} else {
			readUserCpuFreq = true;
			writeUserCpuFreq = true;
		}
		// }
	}

	public boolean hasIssues() {
		boolean ok = rooted &&
				writeGovernor &&
				writeMinFreq &&
				writeMaxFreq &&
				writeUserCpuFreq &&
				readGovernor &&
				readMaxFreq &&
				readMinFreq &&
				readUserCpuFreq;
		return !ok;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CapabilityChecker\n");
		sb.append("rooted: ").append(rooted ? "Yes" : "No").append("\n");
		sb.append("readGovernor: ").append(readGovernor ? "Yes" : "No").append("\n");
		sb.append("writeGovernor: ").append(writeGovernor ? "Yes" : "No").append("\n");
		sb.append("readMinFreq: ").append(readMinFreq ? "Yes" : "No").append("\n");
		sb.append("writeMinFreq: ").append(writeMinFreq ? "Yes" : "No").append("\n");
		sb.append("readMaxFreq: ").append(readMaxFreq ? "Yes" : "No").append("\n");
		sb.append("writeMaxFreq: ").append(writeMaxFreq ? "Yes" : "No").append("\n");
		sb.append("readUserCpuFreq: ").append(readUserCpuFreq ? "Yes" : "No").append("\n");
		sb.append("writeUserCpuFreq: ").append(writeUserCpuFreq ? "Yes" : "No").append("\n");
		return sb.toString();
	}

	private void checkSetGovernor() {
		RootHandler.writeLog("********************************************");
		RootHandler.writeLog("checkSetGovernor");
		RootHandler.writeLog("********************************************");
		String[] govs = cpuHandler.getAvailCpuGov();
		String activeGov = cpuHandler.getCurCpuGov();
		if (RootHandler.NOT_AVAILABLE.equals(activeGov)) {
			readGovernor = false;
			return;
		}

		// MDJ Clean 2.4 + Kernel S6.1
		if ("smartass".equals(activeGov)) {
			readGovernor = false;
			return;
		}
		readGovernor = true;
		String newGov = activeGov;
		for (int i = 0; activeGov.equals(newGov) && i < govs.length; i++) {
			newGov = govs[i];
		}
		RootHandler.writeLog("*** Writing to sysfs ***");
		cpuHandler.setCurGov(newGov);
		String finalGov = cpuHandler.getCurCpuGov();
		Log.i(Logger.TAG, "Checking governor: acitiv: " + activeGov + " new: " + newGov + " final: " + finalGov);
		writeGovernor = finalGov.equals(newGov);
		cpuHandler.setCurGov(activeGov);
	}

	private void checkSetMaxFreq() {
		RootHandler.writeLog("********************************************");
		RootHandler.writeLog("checkSetMaxFreq");
		RootHandler.writeLog("********************************************");
		int activeFreq = cpuHandler.getMaxCpuFreq();
		if (activeFreq < 0) {
			readMaxFreq = false;
			return;
		}
		readMaxFreq = true;
		int newFreq = activeFreq;
		for (int i = freqs.length - 1; activeFreq == newFreq && i > -1; i--) {
			newFreq = freqs[i];
		}
		RootHandler.writeLog("*** Writing to sysfs ***");
		cpuHandler.setMaxCpuFreq(newFreq);
		int finalFreq = cpuHandler.getMaxCpuFreq();
		Log.i(Logger.TAG, "Checking maxCpuFreq: acitiv: " + activeFreq + " new: " + newFreq + " final: " + finalFreq);
		writeMaxFreq = finalFreq == newFreq;
		cpuHandler.setMaxCpuFreq(activeFreq);
	}

	private void checkSetMinFreq() {
		RootHandler.writeLog("********************************************");
		RootHandler.writeLog("checkSetMinFreq");
		RootHandler.writeLog("********************************************");
		int activeFreq = cpuHandler.getMinCpuFreq();
		if (activeFreq < 0) {
			readMinFreq = false;
			return;
		}
		readMinFreq = true;
		int newFreq = activeFreq;
		for (int i = 0; activeFreq == newFreq && i < freqs.length; i++) {
			newFreq = freqs[i];
		}
		RootHandler.writeLog("*** Writing to sysfs ***");
		cpuHandler.setMinCpuFreq(newFreq);
		int finalFreq = cpuHandler.getMinCpuFreq();
		Log.i(Logger.TAG, "Checking minCpuFreq: acitiv: " + activeFreq + " new: " + newFreq + " final: " + finalFreq);
		writeMinFreq = finalFreq == newFreq;
		cpuHandler.setMinCpuFreq(activeFreq);
	}

	private void checkSetUserCpuFreq() {
		RootHandler.writeLog("********************************************");
		RootHandler.writeLog("checkSetMinFreq");
		RootHandler.writeLog("********************************************");
		String gov = cpuHandler.getCurCpuGov();
		cpuHandler.setCurGov(CpuHandler.GOV_USERSPACE);
		int activeFreq = cpuHandler.getUserCpuFreq();
		if (activeFreq < 0) {
			readUserCpuFreq = false;
			return;
		}
		readUserCpuFreq = true;
		int newFreq = activeFreq;
		for (int i = 0; activeFreq == newFreq && i < freqs.length; i++) {
			newFreq = freqs[i];
		}
		RootHandler.writeLog("*** Writing to sysfs ***");
		cpuHandler.setUserCpuFreq(newFreq);
		int finalFreq = cpuHandler.getUserCpuFreq();
		Log.i(Logger.TAG, "Checking userCpuFreq: acitiv: " + activeFreq + " new: " + newFreq + " final: " + finalFreq);
		writeUserCpuFreq = true; // FIXME finalFreq == newFreq;
		cpuHandler.setUserCpuFreq(activeFreq);
		cpuHandler.setCurGov(gov);
	}

	public boolean isRooted() {
		return rooted;
	}

	public boolean isWriteGovernor() {
		return writeGovernor;
	}

	public boolean isWriteMinFreq() {
		return writeMinFreq;
	}

	public boolean isWriteMaxFreq() {
		return writeMaxFreq;
	}

	public boolean isWriteUserCpuFreq() {
		return writeUserCpuFreq;
	}

	public boolean isReadGovernor() {
		return readGovernor;
	}

	public boolean isReadMaxFreq() {
		return readMaxFreq;
	}

	public boolean isReadMinFreq() {
		return readMinFreq;
	}

	public HardwareHandler getCpuHandler() {
		return cpuHandler;
	}

	public int[] getFreqs() {
		return freqs;
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
			return "The device does not seem to be rooted. All CPU related features will not work.";
		} else if (hasIssues()) {
			return "Found some issues... Some features might not work.";
		}
		return "No issues found...";
	}

	public boolean isReadUserCpuFreq() {
		return readUserCpuFreq;
	}

}
