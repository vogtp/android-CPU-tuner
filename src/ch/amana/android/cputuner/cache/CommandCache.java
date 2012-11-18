package ch.amana.android.cputuner.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import ch.amana.android.cputuner.hw.RootHandler;

import com.stericson.RootTools.execution.Command;

public class CommandCache extends Cache {

	private static final int NO_PROFILE = -1;
	Map<Long, String[]> commands = new TreeMap<Long, String[]>();
	List<String> lines = new ArrayList<String>();
	private long profileId = NO_PROFILE;

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#removeScripts(android.content.Context)
	 */
	@Override
	public void clear(Context ctx) {
		commands.clear();
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#runScript(android.content.Context, long)
	 */
	@Override
	public boolean execute(Context ctx, long pid) {
		String[] cmds = commands.get(pid);
		Command command = new Command(0, cmds) {

			@Override
			public void output(int id, String line) {
			}
		};
		return RootHandler.execute(command);
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#hasScript(android.content.Context, long)
	 */
	@Override
	public boolean exists(Context ctx, long pid) {
		return commands.containsKey(pid);
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#startRecording(android.content.Context, long)
	 */
	@Override
	public void startRecording(Context ctx, long pid) {
		lines.clear();
		profileId = pid;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#endRecording()
	 */
	@Override
	public void endRecording() {
		String[] cmds = lines.toArray(new String[lines.size()]);
		commands.put(profileId, cmds);
		profileId = NO_PROFILE;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#isRecoding()
	 */
	@Override
	public boolean isRecoding() {
		return profileId != NO_PROFILE;
	}

	/* (non-Javadoc)
	 * @see ch.amana.android.cputuner.cache.ICache#recordLine(java.lang.String)
	 */
	@Override
	public void recordLine(String cmd) {
		lines.add(cmd);
	}

}
