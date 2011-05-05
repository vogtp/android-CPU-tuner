package ch.amana.android.cputuner.view.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TwoLineListItem;

public class ConfigurationsListAdapter extends ConfigurationsAdapter {

	public ConfigurationsListAdapter(Context ctx) {
		super(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
		view.getText1().setText(getDirectory(position).getName());
		view.getText2().setText(SimpleDateFormat.getInstance().format(new Date(getNewestFile(position))));
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
