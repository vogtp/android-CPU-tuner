package ch.amana.android.cputuner.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;

public class AdvStatsFilterAdaper extends BaseAdapter implements SpinnerAdapter {

	public static final long ALL_ID = Long.MIN_VALUE;
	private final String ALL;
	private final Cursor cursor;
	private final LayoutInflater layoutInflator;
	private final Context ctx;

	public AdvStatsFilterAdaper(Context context, Uri contentUri, String[] projection, String sortOrder) {
		super();
		this.ctx = context;
		this.layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.ALL = context.getString(R.string.all);
		CursorLoader cursorLoader = new CursorLoader(context, contentUri, projection, null, null, sortOrder);
		this.cursor = cursorLoader.loadInBackground();
	}

	@Override
	public int getCount() {
		if (cursor == null) {
			return 1;
		}
		return cursor.getCount() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return ALL;
		} else if (position > 0) {
			return cursor.move(position - 1);
		} else {
			return ctx.getString(R.string.not_enabled);
		}
	}

	@Override
	public long getItemId(int position) {
		if (position == 0) {
			return ALL_ID;
		} else if (position == Integer.MAX_VALUE) {
			return position;
		}
		cursor.moveToPosition(position - 1);
		return cursor.getLong(DB.INDEX_ID);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = convertView != null ? (TextView) convertView : createView(parent);
		String text = "";
		if (position > 0) {
			try {
				if (cursor.moveToPosition(position - 1)) {
					text = cursor.getString(DB.CpuProfile.INDEX_PROFILE_NAME);
				}
			} catch (Throwable e) {
				Logger.i("Cannot get profilename from cursor", e);
			}
		} else {
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				text = ALL;
				view.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC);
			} else {
				text = ctx.getString(R.string.not_enabled);
				view.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD);
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
