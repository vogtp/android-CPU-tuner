package ch.amana.android.cputuner.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigurationsSpinnerAdapter extends ConfigurationsAdapter {

	public ConfigurationsSpinnerAdapter(Context ctx) {
		super(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (convertView != null) ? (TextView) convertView : createView(parent);
		view.setText(getDirectory(position).getName());
		return view;
	}

	private TextView createView(ViewGroup parent) {
		TextView item;
		if (parent instanceof Spinner) {
			item = (TextView) layoutInflator.inflate(android.R.layout.simple_spinner_item, parent, false);
		} else {
			item = (TextView) layoutInflator.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
		}
		item.setSingleLine();
		item.setEllipsize(TextUtils.TruncateAt.END);
		return item;
	}

	public int getIndexOf(String configuration) {
		if (configuration == null) {
			return -1;
		}
		for (int i = 0; i < getCount(); i++) {
			if (configuration.equals(getDirectory(i).getName())) {
				return i;
			}
		}
		return -1;
	}

}
