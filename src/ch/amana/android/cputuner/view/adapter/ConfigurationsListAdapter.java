package ch.amana.android.cputuner.view.adapter;

import java.util.Date;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TwoLineListItem;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;

public class ConfigurationsListAdapter extends ConfigurationsAdapter {

	public ConfigurationsListAdapter(Context ctx) {
		super(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
		String name = getDirectory(position).getName();
		view.getText1().setText(name);
		if (name != null && name.equals(SettingsStorage.getInstance().getCurrentConfiguration())) {
			view.getText1().setTextColor(view.getResources().getColor(R.color.cputuner_green));
		}
		StringBuilder savedAtStr = new StringBuilder();
		savedAtStr.append(parent.getResources().getText(R.string.saved_at)).append(" ");
		savedAtStr.append(SettingsStorage.getInstance().getSimpledateformat().format(new Date(getNewestFile(position))));
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
