package ch.amana.android.cputuner.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;
import ch.amana.android.cputuner.R;

import com.markupartist.android.widget.ActionBar;

public class CputunerActionBar extends ActionBar {

	private final ImageView ivLogo;
	private final TextView tvTitle;
	private final TextView tvSubtitle;

	public CputunerActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		ivLogo = (ImageView) findViewById(R.id.actionbar_home_logo);
		tvTitle = (TextView) findViewById(R.id.actionbar_title);
		tvSubtitle = (TextView) findViewById(R.id.actionbar_subtitle);

	}

	public void setHomeTitleAction(Action action) {
		ivLogo.setOnClickListener(this);
		tvTitle.setOnClickListener(this);
		ivLogo.setTag(action);
		tvTitle.setTag(action);
	}

	public void setSubTitle2(String s) {
		tvSubtitle.setText(s);
		tvSubtitle.setVisibility(VISIBLE);
	}

	public void setSubTitle2(int id) {
		tvSubtitle.setText(id);
		tvSubtitle.setVisibility(VISIBLE);
	}
}
