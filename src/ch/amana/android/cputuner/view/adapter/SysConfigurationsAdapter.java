package ch.amana.android.cputuner.view.adapter;

import java.util.Arrays;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;

public class SysConfigurationsAdapter extends BaseAdapter {

	private final String[] configurationDirs;
	private final LayoutInflater layoutInflator;

	public SysConfigurationsAdapter(Context ctx, String[] dirs) {
		super();
		layoutInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		configurationDirs = dirs;
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
		view.getText1().setText(getDirectoryName(position));
		StringBuilder savedAtStr = new StringBuilder();
		//		savedAtStr.append(parent.getResources().getText(R.string.saved_at)).append(" ");
		//		savedAtStr.append(SettingsStorage.getInstance().getSimpledateformat().format(new Date(getNewestFile(position))));
		view.getText2().setText(savedAtStr);
		return view;
	}

	private TwoLineListItem createView(ViewGroup parent) {
		TwoLineListItem item = (TwoLineListItem) layoutInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
		item.getText1().setSingleLine();
		item.getText2().setSingleLine();
		item.getText1().setEllipsize(TextUtils.TruncateAt.END);
		item.getText2().setEllipsize(TextUtils.TruncateAt.END);
		return item;
	}
}
