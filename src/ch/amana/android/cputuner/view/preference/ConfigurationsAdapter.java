package ch.amana.android.cputuner.view.preference;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;

public class ConfigurationsAdapter extends BaseAdapter {

	public static final FilenameFilter FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			File file = new File(dir, filename);
			return file.isDirectory();
		}
	};

	private File[] configDirs;

	private LayoutInflater layoutInflator;

	private File configurationsDir;

	public ConfigurationsAdapter(Context ctx) {
		super();
		layoutInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		configurationsDir = BackupRestoreHelper.getStoragePath(ctx, ConfigurationManageActivity.DIRECTORY);
		refresh();
	}

	@Override
	public int getCount() {
		return configDirs.length;
	}

	@Override
	public Object getItem(int position) {
		return getDirectory(position);
	}

	public File getDirectory(int position) {
		return configDirs[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
		view.getText1().setText(getDirectory(position).getName());
		view.getText2().setText(SimpleDateFormat.getInstance().format(new Date(getNewestFile(position))));
		return view;
	}

	private long getNewestFile(int position) {
		File directory = getDirectory(position);
		File[] files = directory.listFiles();
		long ts = directory.lastModified();
		for (int i = 0; i < files.length; i++) {
			ts = Math.max(ts, files[i].lastModified());
		}
		return ts;
	}

	private TwoLineListItem createView(ViewGroup parent) {
		TwoLineListItem item = (TwoLineListItem) layoutInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
		item.getText1().setSingleLine();
		item.getText2().setSingleLine();
		item.getText1().setEllipsize(TextUtils.TruncateAt.END);
		item.getText2().setEllipsize(TextUtils.TruncateAt.END);
		return item;
	}

	@Override
	public void notifyDataSetChanged() {
		refresh();
		super.notifyDataSetChanged();
	}

	private void refresh() {
		configDirs = configurationsDir.listFiles(FILTER);
	}
}
