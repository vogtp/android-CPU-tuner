package ch.amana.android.cputuner.hw;

import ch.amana.android.cputuner.helper.Logger;

public class HardwareHandler {

	public static final int NO_VALUE_INT = Integer.MIN_VALUE;

	public HardwareHandler() {
		super();
	}

	protected static int getIntFromStr(String intString) {
		int i = NO_VALUE_INT;
		try {
			i = Integer.parseInt(intString);
		} catch (Exception e) {
			Logger.w("Cannot parse " + intString + " as interger");
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
		Logger.d("Creating array from >" + listString + "<");
		if (RootHandler.NOT_AVAILABLE.equals(listString)) {
			int[] list = new int[1];
			list[0] = NO_VALUE_INT;
			return list;
		}
		String[] strList = listString.split(" +");
		int[] lst = new int[strList.length];
		for (int i = 0; i < strList.length; i++) {
			try {
				lst[i] = Integer.parseInt(strList[i]);
			} catch (Exception e) {
				lst[i] = NO_VALUE_INT;
			}
		}
		return lst;
	}

	protected String[] createListStr(String listString) {
		Logger.d("Creating array from >" + listString + "<");
		if (RootHandler.NOT_AVAILABLE.equals(listString)) {
			String[] list = new String[1];
			list[0] = listString;
			return list;
		}
		return listString.split(" +");
	}

}