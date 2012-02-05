package ch.amana.android.cputuner.view.appwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.activity.BillingProductListActiviy;

public class AppwidgetConfigureAction extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		if (!SettingsStorage.getInstance(this).hasWidget()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_install_widget_dia);
			builder.setMessage(R.string.msg_install_widget_dia);
			builder.setNegativeButton(R.string.buNeg_install_widget_dia, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.setPositiveButton(R.string.buPos_install_widget_dia, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(BillingProductListActiviy.getExtentionsIntent(AppwidgetConfigureAction.this));
					finish();
				}
			});
			builder.create().show();
			return;
		}

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {

		}
		int mAppWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		//		RemoteViews views = new RemoteViews(getPackageName(), R.layout.appwidget_base);
		//		appWidgetManager.updateAppWidget(mAppWidgetId, views);
		appWidgetManager.updateAppWidget(mAppWidgetId, ProfileAppwidgetProvider.createAppWidgetView(getApplicationContext()));
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

}
