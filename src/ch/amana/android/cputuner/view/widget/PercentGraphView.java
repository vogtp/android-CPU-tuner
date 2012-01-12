package ch.amana.android.cputuner.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import ch.amana.android.cputuner.R;

public class PercentGraphView extends View {


	private Paint normalPaint;
	private Paint activePaint;
	private float percent;
	private Paint bgPaint;
	private boolean highlight;
	private Paint bgPaintHighlight;

	public PercentGraphView(Context context) {
		super(context);
		init();
	}

	public PercentGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PercentGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		Resources resources = getContext().getResources();
		normalPaint = new Paint();
		normalPaint.setColor(resources.getColor(R.color.cputuner_green));
		activePaint = new Paint();
		activePaint.setColor(Color.RED);
		bgPaint = new Paint();
		bgPaint.setColor(Color.DKGRAY);
		bgPaintHighlight = new Paint();
		bgPaintHighlight.setColor(Color.LTGRAY);
	}

	public void setPercent(float p) {
		this.percent = p;
		requestLayout();
	}

	public void setHiglight(boolean b) {
		this.highlight = b;
		requestLayout();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int padT = getPaddingTop();
		int padB = getPaddingBottom();
		int padL = getPaddingLeft();
		int padR = getPaddingRight();
		int width = canvas.getWidth() - padL - padR;
		int height = canvas.getHeight() - padT - padB;
		int corners = height / 4;

		int lenth = Math.round(width * percent / 100f);
		if (lenth == 0 && percent > 0f) {
			lenth = 1;
		} else if (lenth > width) {
			lenth = width;
		}
		RectF rectBg = new RectF(padL, padT, width, height);
		RectF rect = new RectF(padL, padT, lenth, height);
		canvas.drawRoundRect(rectBg, corners, corners, highlight ? bgPaintHighlight : bgPaint);
		canvas.drawRoundRect(rect, corners, corners, highlight ? activePaint : normalPaint);

	}

}
