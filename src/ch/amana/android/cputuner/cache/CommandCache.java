package ch.amana.android.cputuner.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.amana.android.cputuner.hw.RootHandler;

import com.stericson.RootTools.execution.Command;

public class CommandCache extends Cache {

	private static final int NO_PROFILE = -1;
	Map<Long, String[]> commands = new TreeMap<Long, String[]>();
	List<String> lines = new ArrayList<String>();
	private long profileId = NO_PROFILE;

	@Override
	public void clear() {
		commands.clear();
	}

	@Override
	public boolean execute(long pid) {
		String[] cmds = commands.get(pid);
		Command command = new Command(0, cmds) {

			@Override
			public void output(int id, String line) {
			}
		};
		return RootHandler.execute(command);
	}

	@Override
	public boolean exists(long pid) {
		return commands.containsKey(pid);
	}

	@Override
	public void startRecording(long pid) {
		lines.clear();
		profileId = pid;
	}

	@Override
	public void endRecording() {
		String[] cmds = lines.toArray(new String[lines.size()]);
		commands.put(profileId, cmds);
		profileId = NO_PROFILE;
	}

	@Override
	public boolean isRecoding() {
		return profileId != NO_PROFILE;
	}

	@Override
	public void recordLine(String cmd) {
		lines.add(cmd);
	}

}
