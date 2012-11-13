package ch.amana.android.cputuner.view.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.log.SwitchLog;
import ch.amana.android.cputuner.provider.DB;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity.StateChangeListener;
import ch.amana.android.cputuner.view.activity.HelpActivity;

import com.markupartist.android.widget.ActionBar.Action;

public class LogFragment extends PagerListFragment implements StateChangeListener, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	private final Date now = new Date();

	private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss");

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();
		if (act == null) {
			return;
		}

		setListShown(false);
		getLoaderManager().initLoader(0, null, this);

		adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, null,
				new String[] { DB.SwitchLogDB.NAME_MESSAGE },
				new int[] { android.R.id.text1 }, 0);

		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if (columnIndex == DB.SwitchLogDB.INDEX_MESSAGE) {
					now.setTime(cursor.getLong(DB.SwitchLogDB.INDEX_TIME));
					StringBuilder sb = new StringBuilder();
					sb.append(logDateFormat.format(now)).append(": ");
					String msg = cursor.getString(DB.SwitchLogDB.INDEX_MESSAGE);
					if (msg == null) {
						msg = cursor.getString(DB.SwitchLogDB.INDEX_TRIGGER) + " -> " + cursor.getString(DB.SwitchLogDB.INDEX_PROFILE);
					}
					sb.append(msg);
					TextView textView = (TextView) view;
					textView.setText(sb.toString());
					textView.setTextColor(Color.LTGRAY);
					return true;
				}
				return false;
			}
		});

		setListAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		final Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).addStateChangeListener(this);
		}
		requestUpdate();
	}

	@Override
	public void onPause() {

		Activity act = getActivity();
		if (act instanceof CpuTunerViewpagerActivity) {
			((CpuTunerViewpagerActivity) act).removeStateChangeListener(this);
		}
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh_option, menu);
		inflater.inflate(R.menu.upgrade_option, menu);
	}

	@Override
	public boolean onOptionsItemSelected(Activity act, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			requestUpdate();
			return true;
		case R.id.itemUpgrade:
			Intent i = new Intent(act, BillingProductListActiviy.class);
			i.putExtra(BillingProductListActiviy.EXTRA_TITLE, act.getString(R.string.title_extentions));
			i.putExtra(BillingProductListActiviy.EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_EXTENTIONS);
			act.startActivity(i);
			return true;
		}
		if (GeneralMenuHelper.onOptionsItemSelected(act, item, HelpActivity.PAGE_PROFILE)) {
			return true;
		}
		return false;
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>(1);
		actions.add(new Action() {
			@Override
			public void performAction(View view) {
				requestUpdate();
			}

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}
		});
		return actions;
	}

	@Override
	public void profileChanged() {
		requestUpdate();
	}

	@Override
	public void deviceStatusChanged() {
		requestUpdate();
	}

	@Override
	public void triggerChanged() {
		requestUpdate();
	}

	private void requestUpdate() {
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				FragmentActivity activity = getActivity();
				if (activity != null) {
					activity.sendBroadcast(new Intent(SwitchLog.ACTION_FLUSH_LOG));
				}
			}
		}, 1000);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loader, Bundle bundle) {
		return new CursorLoader(getActivity(), DB.SwitchLogDB.CONTENT_URI, DB.SwitchLogDB.PROJECTION_NORMAL_LOG, null, null, DB.SwitchLogDB.SORTORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		adapter.swapCursor(c);

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
