package ch.amana.android.cputuner.view.adapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;
import ch.amana.android.cputuner.helper.Logger;

public class SysConfigurationsAdapter extends BaseAdapter {

	private static final String INFOTAG_NAME = "title";
	private static final String INFOTAG_DESC = "description";
	private static final String INFO_FILENAME = "info.json";
	private final String[] configurationDirs;
	private final LayoutInflater layoutInflator;
	private final Context ctx;
	private final HashMap<Integer, JSONObject> infoHash = new HashMap<Integer, JSONObject>();

	public SysConfigurationsAdapter(Context ctx) throws IOException {
		super();
		this.ctx = ctx;
		layoutInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		configurationDirs = ctx.getAssets().list(BackupRestoreHelper.DIRECTORY_CONFIGURATIONS);
		refresh();
	}

	@Override
	public int getCount() {
		return configurationDirs.length;
	}

	@Override
	public Object getItem(int position) {
		return getDirectoryName(position);
	}

	public String getDirectoryName(int position) {
		if (configurationDirs == null || position < 0 || position > configurationDirs.length) {
			return null;
		}
		return configurationDirs[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void notifyDataSetChanged() {
		refresh();
		super.notifyDataSetChanged();
	}

	private void refresh() {
		Arrays.sort(configurationDirs);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
		try {
			JSONObject info = getInfo(position);
			view.getText1().setText(info.getString(INFOTAG_NAME));
			view.getText2().setText(info.getString(INFOTAG_DESC));
		} catch (Throwable e) {
			view.getText1().setText(getDirectoryName(position));
			Logger.w("Cannot get configuration information from json object", e);
		}
		return view;
	}

	private TwoLineListItem createView(ViewGroup parent) {
		TwoLineListItem item = (TwoLineListItem) layoutInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
		item.getText1().setSingleLine();
		//		item.getText2().setSingleLine();
		item.getText1().setEllipsize(TextUtils.TruncateAt.END);
		//		item.getText2().setEllipsize(TextUtils.TruncateAt.END);
		return item;
	}

	private JSONObject getInfo(int position) throws IOException, JSONException {
		JSONObject info = infoHash.get(position);
		if (info == null) {
			info = readJsonInfo(position);
			infoHash.put(position, info);
		}
		return info;
	}

	private JSONObject readJsonInfo(int position) throws IOException, JSONException {
		String directoryName = getDirectoryName(position);
		BufferedReader reader = new BufferedReader(new InputStreamReader(ctx.getAssets().open(
				BackupRestoreHelper.DIRECTORY_CONFIGURATIONS + "/" + directoryName + "/" + INFO_FILENAME)));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return new JSONObject(sb.toString());
	}
}
