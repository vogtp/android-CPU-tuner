package ch.amana.android.cputuner.view.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;

public abstract class ConfigurationsAdapter extends BaseAdapter {

	protected static final FilenameFilter FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			File file = new File(dir, filename);
			return file.isDirectory();
		}
	};

	protected static final Comparator<File> FILE_SORT = new Comparator<File>() {

		@Override
		public int compare(File f1, File f2) {
			return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		}
	};

	private static final File[] NO_CONFIGS = new File[0];

	private File[] configDirs;

	protected LayoutInflater layoutInflator;

	private final File configurationsDir;

	public ConfigurationsAdapter(Context ctx) {
		super();
		layoutInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		configurationsDir = BackupRestoreHelper.getStoragePath(ctx, BackupRestoreHelper.DIRECTORY_CONFIGURATIONS);
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
		if (configDirs == null || position < 0 || position > configDirs.length) {
			return null;
		}
		return configDirs[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected long getNewestFile(int position) {
		File directory = getDirectory(position);
		File[] files = directory.listFiles();
		long ts = directory.lastModified();
		for (int i = 0; i < files.length; i++) {
			ts = Math.max(ts, files[i].lastModified());
		}
		return ts;
	}

	@Override
	public void notifyDataSetChanged() {
		refresh();
		super.notifyDataSetChanged();
	}

	private void refresh() {
		configDirs = configurationsDir.listFiles(FILTER);
		if (configDirs == null) {
			configDirs = NO_CONFIGS;
		}
		Arrays.sort(configDirs, FILE_SORT);
	}

	public boolean hasConfig(String name) {
		if (name == null) {
			return false;
		}
		name = name.trim();
		if (configDirs == null) {
			return false;
		}
		for (int i = 0; i < getCount(); i++) {
			if (name.equals(getDirectory(i).getName().trim())) {
				return true;
			}
		}
		return false;
	}
}
