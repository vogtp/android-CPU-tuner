package ch.amana.android.cputuner.hw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.os.Environment;
import ch.amana.android.cputuner.cache.ScriptCache;
import ch.amana.android.cputuner.log.Logger;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.Shell;

public class RootHandler {
	public static final String NOT_AVAILABLE = "not available";

	private static boolean isRoot = false;
	private static boolean checkedIsRoot = false;

	private static boolean checkedSystemApp = false;
	private static boolean isSystemApp = false;

	private static Writer logWriter;

	public static boolean execute(Command command) {
		if (command == null) {
			Logger.e("Command is null!", new NullPointerException());
			return false;
		}
		try {
			getShell().add(command);
			command.waitForFinish();
			return command.exitCode() == 0;
		} catch (IOException e) {
			Logger.e("IO error from: " + command.getCommand(), e);
		} catch (TimeoutException e) {
			Logger.e("Timeout getting shell", e);
		} catch (RootDeniedException e) {
			Logger.e("Could not run as root", e);
		} catch (InterruptedException e) {
			Logger.e("Command did not finish: " + command.getCommand(), e);
		}
		return false;
	}

	public static boolean execute(String cmd) {
		if (cmd == null) {
			return false;
		}
		Command command = new Command(0, 1000, cmd) {
			@Override
			public void output(int id, String line) {
			}
		};
		return execute(command);
	}

	public static boolean execute(String cmd, final StringBuilder result) {
		if (cmd == null) {
			return false;
		}
		Command command = new Command(0, 1000, cmd) {
			@Override
			public void output(int id, String line) {
				result.append(line);
			}
		};
		return execute(command);
	}

	private static synchronized Shell getShell() throws IOException, TimeoutException, RootDeniedException {
		return RootTools.getShell(true, 1000);
	}

	public static void writeLog(String line) {
		if (logWriter != null) {
			try {
				logWriter.write(line);
				logWriter.write("\n");
				logWriter.flush();
			} catch (Exception e) {
				Logger.w("Cannot write >" + line + "< to log file", e);
			}
		}
	}

	public static boolean isRoot() {
		if (!checkedIsRoot) {
			isRoot = RootTools.isRootAvailable();
			checkedIsRoot = true;
		}
		return isRoot;
	}

	public static boolean isSystemApp(Context ctx) {
		if (!checkedSystemApp) {
			String[] fileList = findAppPath(ctx, Environment.getRootDirectory());
			isSystemApp = fileList != null && fileList.length > 0;
			checkedSystemApp = true;
			Logger.i("Is system app: " + isSystemApp);
		}
		return isSystemApp;
	}

	public static String[] findAppPath(Context ctx, File root) {
		if (!root.isDirectory()) {
			return new String[] {};
		}
		File appsRoot = new File(root, "app");
		final String packageName = ctx.getPackageName();
		String[] fileList = appsRoot.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(packageName);
			}
		});
		return fileList;
	}

	static String readFile(File file) {
		if (file == null || file == CpuHandler.DUMMY_FILE || !file.exists()) {
			return NOT_AVAILABLE;
		}
		synchronized (file) {
			StringBuilder val = new StringBuilder();
			BufferedReader reader = null;
			try {
				if (file.canRead()) {
					reader = new BufferedReader(new FileReader(file), 256);
					String line = reader.readLine();
					while (line != null && !line.trim().equals("")) {
						if (Logger.DEBUG) {
							writeLog(line);
						}
						if (val.length() > 0) {
							val.append("\n");
						}
						val.append(line);
						line = reader.readLine();
					}
					reader.close();
				} else if (execute("cat " + file.getAbsolutePath(), val)) {
				} else {
					if (Logger.DEBUG) {
						String msg = "Cannot read from file >" + file + "< it does not exist.";
						Logger.v(msg);
						writeLog(msg);
					}
				}
			} catch (Throwable e) {
				Logger.e("Cannot open file for reading ", e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
					reader = null;
				}
			}
			String ret = val.toString();
			if (ret.trim().equals("")) {
				ret = NOT_AVAILABLE;
			}
			if (Logger.DEBUG) {
				String msg = "Reading file " + file + " yielded >" + ret + "<";
				Logger.v(msg);
				writeLog(msg);
			}
			return ret;
		}
	}

	public static boolean writeFile(File file, String val) {
		if (file == CpuHandler.DUMMY_FILE || !file.exists()) {
			if (Logger.DEBUG) {
				Logger.logStacktrace(file.getAbsolutePath() + " does not exist!");
			}
			return false;
		}
		String cmd = "echo " + val + " > " + file.getAbsolutePath();
		if (ScriptCache.getInstance().isRecoding()) {
			ScriptCache.getInstance().recordLine(cmd);
			return true;
		}
		synchronized (file) {
			return execute(cmd);
		}
	}

	public static void setLogLocation(File file) {
		if (file == null) {
			logWriter = null;
			return;
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				Logger.w("Cannot creat log file " + file.toString(), e);
				logWriter = null;
				return;
			}
		}
		if (file.isFile() && file.canWrite()) {
			if (Logger.DEBUG) {
				Logger.i("Opening logfile " + file.getAbsolutePath());
			}
			try {
				logWriter = new FileWriter(file);
			} catch (IOException e) {
				Logger.w("Cannot open logfile", e);
			}
		} else {
			logWriter = null;
		}
	}

	public static void clearLogLocation() {
		if (logWriter != null) {
			try {
				logWriter.flush();
				logWriter.close();
			} catch (IOException e) {
				Logger.w("Cannot close logfile", e);
			}
		}
		logWriter = null;
	}
}
