package ch.amana.android.cputuner.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.view.activity.CpuTunerViewpagerActivity;

import com.markupartist.android.widget.ActionBar;

public class CputunerActionBar extends ActionBar {

	private final ImageView ivLogo;
	private final TextView tvTitle;

	public CputunerActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		ivLogo = (ImageView) findViewById(R.id.actionbar_home_logo);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);

		setTitle(R.string.app_name);
		setHomeLogo(R.drawable.icon);
		setHomeAction(new ActionBar.IntentAction(context, CpuTunerViewpagerActivity.getStartIntent(context), R.drawable.icon));

	}

	@Override
	public void setHomeAction(Action action) {
		ivLogo.setOnClickListener(this);
		tvTitle.setOnClickListener(this);
		ivLogo.setTag(action);
		tvTitle.setTag(action);
	}
}
