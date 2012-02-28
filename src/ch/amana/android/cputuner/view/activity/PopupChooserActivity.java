package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.hw.PowerProfiles;
import ch.amana.android.cputuner.hw.PowerProfiles.ServiceType;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.provider.db.DB;
import ch.amana.android.cputuner.service.TunerService;
import ch.amana.android.cputuner.view.adapter.ProfileAdaper;
import ch.amana.android.cputuner.view.appwidget.ProfileAppwidgetProvider;
import ch.amana.android.cputuner.view.widget.ServiceSwitcher;

public class PopupChooserActivity extends ListActivity {

	private static final int MAX_ITEMS_MEASURED = 15;

	private static final String ACTION_POPUP_CHOOSER = "ch.amana.android.cputuner.ACTION_POPUP_CHOOSER";
	public static final String EXTRA_CHOOSER_TYPE = "EXTRA_CHOOSER_TYPE";
	public static final String EXTRA_SERVICE_TYPE = "EXTRA_SERVICE_TYPE";
	public static final int CHOOSER_TYPE_PROFILE = 1;
	public static final int CHOOSER_TYPE_SERVICE = 2;

	private int chooserType = 0;

	private ServiceType serviceType;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		//		setTheme(R.style.Theme_Transparent_Dialog);
		CursorLoader cursorLoader = new CursorLoader(this, DB.CpuProfile.CONTENT_URI, DB.CpuProfile.PROJECTION_PROFILE_NAME, null, null, DB.CpuProfile.SORTORDER_DEFAULT);
		Cursor cursor = cursorLoader.loadInBackground();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		chooserType = extras.getInt(EXTRA_CHOOSER_TYPE);

		String st = extras.getString(EXTRA_SERVICE_TYPE);
		if (!TextUtils.isEmpty(st)) {
			serviceType = ServiceType.valueOf(st);
		}
		} else {
			String msg = "PopupChooserActivity get null extras... what sould I do?";
			Logger.e(msg);
			if (Logger.DEBUG) {
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			}
			finish();
		}

		if (chooserType == CHOOSER_TYPE_PROFILE) {
			setTitle(R.string.labelCurrentProfile);
			setListAdapter(new ProfileAdaper(this, cursor, R.layout.profilechooser_item, R.id.ID));
		} else if (chooserType == CHOOSER_TYPE_SERVICE) {
			setTitle(PowerProfiles.getServiceTypeName(this, serviceType));
			ArrayAdapter<CharSequence> adapter = ServiceSwitcher.getServiceStateAdapter(this, serviceType);
			adapter.setDropDownViewResource(R.layout.profilechooser_item);
			setListAdapter(adapter);
		}
		if (SettingsStorage.getInstance(this).hasWidget()) {
			// the widget checks if it exists
			ProfileAppwidgetProvider.updateView(this);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (chooserType == CHOOSER_TYPE_PROFILE) {
			if (id == PowerProfiles.AUTOMATIC_PROFILE && !SettingsStorage.getInstance().isEnableProfiles()) {
				return;
			}
			Intent i = new Intent(TunerService.ACTION_TUNERSERVICE_MANUAL_PROFILE);
			i.putExtra(TunerService.EXTRA_IS_MANUAL_PROFILE, id != PowerProfiles.AUTOMATIC_PROFILE);
			i.putExtra(TunerService.EXTRA_PROFILE_ID, id);
			startService(i);
		} else if (chooserType == CHOOSER_TYPE_SERVICE) {
			ServiceSwitcher.setServiceStatusFromPosition(this, serviceType, position);
		}
		close();
	}

	int measureContentWidth(ListAdapter adapter) {
		if (adapter == null) {
			return 0;
		}

		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec =
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec =
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

		// Make sure the number of items we'll measure is capped. If it's a huge data set
		// with wildly varying sizes, oh well.
		int start = Math.max(0, getSelectedItemPosition());
		final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
		final int count = end - start;
		start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
		for (int i = start; i < end; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			itemView = adapter.getView(i, itemView, getListView());
			if (itemView.getLayoutParams() == null) {
				itemView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}

		return width;
	}

	@Override
	public void onResume() {
		int w = measureContentWidth(getListAdapter());
		if (w > 0) {
			getListView().setMinimumWidth(w);
			//			getListView().setM
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		close();
	}

	private void close() {
		//		Intent intent = new Intent(Intent.ACTION_MAIN);
		//		intent.addCategory(Intent.CATEGORY_HOME);
		//		startActivity(intent);

		finish();
	}

	public static Intent getStartIntent(Context ctx) {
		Intent intent = new Intent(ACTION_POPUP_CHOOSER);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // works post 11
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		return intent;
	}

}
