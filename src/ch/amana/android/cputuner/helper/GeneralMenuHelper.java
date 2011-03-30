package ch.amana.android.cputuner.helper;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.HelpActivity;
import ch.amana.android.cputuner.view.preference.SettingsPreferenceActivity;

public class GeneralMenuHelper {

	public static boolean onOptionsItemSelected(Context ctx, MenuItem item, String helpPage) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.itemSettings:
			i = new Intent(ctx, SettingsPreferenceActivity.class);
			ctx.startActivity(i);
			return true;

		case R.id.itemMenuHelp:
			i = new Intent(ctx, HelpActivity.class);
			if (helpPage != null) {
				i.putExtra(HelpActivity.EXTRA_HELP_PAGE, helpPage);
			}
			ctx.startActivity(i);
			return true;
		default:
			return false;

		}

	}

}
