package ch.amana.android.cputuner.helper;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.amana.android.cputuner.log.Logger;

public class TimeInStateParser {

	private final Map<Integer, Long> states = new TreeMap<Integer, Long>();
	private TimeInStateParser baseline = null;

	public TimeInStateParser(String start, String end) {
		this(end);
		setBaseline(new TimeInStateParser(start));
	}

	public TimeInStateParser(String timeinstate) {
		try {
			String[] lines = timeinstate.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String[] vals = lines[i].split(" +");
				int freq = Integer.parseInt(vals[0]);
				long time = Long.parseLong(vals[1]);
				states.put(freq, time);
			}
			//				parseOk = lines.length == states.size();
		} catch (Exception e) {
			Logger.w("cannot parse timeinstate");
		}
	}

	public Set<Integer> getStates() {
		return states.keySet();
	}

	public long getTime(int f) {
		Long time = states.get(f);
		if (baseline != null && baseline.states != null) {
			time = time - baseline.states.get(f);
		}
		if (time < 0) {
			time = 0l;

		}
		return time;
	}

	public void setBaseline(TimeInStateParser bl) {
		this.baseline = bl;
	}

}