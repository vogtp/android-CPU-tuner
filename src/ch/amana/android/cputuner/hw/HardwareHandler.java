package ch.amana.android.cputuner.hw;

import android.util.Log;
import ch.amana.android.cputuner.helper.Logger;

public class HardwareHandler {

	public HardwareHandler() {
		super();
	}

	protected static int getIntFromStr(String intString) {
		int i = -1;
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Log.w(Logger.TAG, "Cannot parse " + intString + " as interger");
		}
		return i;
	}

	protected String[] moveCurListElementTop(String[] list, String topElement) {
		if (list == null || list.length < 2) {
			return list;
		}
		String firstElement = list[0];
		for (int i = 0; i < list.length; i++) {
			if (topElement.equals(list[i])) {
				list[i] = firstElement;
				list[0] = topElement;
			}
		}
		return list;
	}

	protected int[] createListInt(String listString) {
		Log.d(Logger.TAG, "Creating array from >" + listString + "<");
		if (RootHandler.NOT_AVAILABLE.equals(listString)) {
			int[] list = new int[1];
			list[0] = -1;
			return list;
		}
		String[] strList = listString.split(" ");
		int[] lst = new int[strList.length];
		for (int i = 0; i < strList.length; i++) {
			try {
				lst[i] = Integer.parseInt(strList[i]);
			} catch (Exception e) {
				lst[i] = -1;
			}
		}
		return lst;
	}

	protected String[] createListStr(String listString) {
		Log.d(Logger.TAG, "Creating array from >" + listString + "<");
		if (RootHandler.NOT_AVAILABLE.equals(listString)) {
			String[] list = new String[1];
			list[0] = listString;
			return list;
		}
		return listString.split(" +");
	}

}