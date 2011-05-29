package ch.amana.android.cputuner.view.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import ch.amana.android.cputuner.helper.BackupRestoreHelper;

public abstract class ConfigurationsAdapter extends BaseAdapter {

	public static final FilenameFilter FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String filename) {
			File file = new File(dir, filename);
			return file.isDirectory();
		}
	};

	private static final File[] NO_CONFIGS = new File[0];

	private File[] configDirs;

	protected LayoutInflater layoutInflator;

	private File configurationsDir;

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
		Arrays.sort(configDirs);
	}
}
