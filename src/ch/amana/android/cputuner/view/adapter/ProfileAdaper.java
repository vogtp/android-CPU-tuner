package ch.amana.android.cputuner.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import ch.amana.android.cputuner.provider.db.DB;

public class ProfileAdaper extends BaseAdapter implements SpinnerAdapter {

	private static final String AUTO = "Auto";
	private final Cursor cursor;
	private final Context context;
	private final LayoutInflater layoutInflator;

	public ProfileAdaper(Context context, Cursor c) {
		super();
		this.context = context;
		this.cursor = c;
		this.layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return cursor.getCount() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position > 0) {
			return cursor.move(position - 1);
		} else {
			return AUTO;
		}
	}

	@Override
	public long getItemId(int position) {
		return position - 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = convertView != null ? (TextView) convertView : createView(parent);
		String text = AUTO;
		if (position > 0) {
			if (cursor.moveToPosition(position - 1)) {
				text = cursor.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
			}
		}
		view.setText(text);
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
}
