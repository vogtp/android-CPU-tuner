package ch.amana.android.cputuner.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.SpinnerAdapter;

public class ListPopupWindowStandalone extends ListPopupWindow {

	private ViewGroup viewGroup;
	private ListAdapter adapter;
	// Only measure this many items to get a decent max width.
	private static final int MAX_ITEMS_MEASURED = 15;

	public ListPopupWindowStandalone(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public ListPopupWindowStandalone(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ListPopupWindowStandalone(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ListPopupWindowStandalone(Context context) {
		super(context);
	}

	public ListPopupWindowStandalone(ViewGroup viewGroup) {
		super(viewGroup.getContext());
		this.viewGroup = viewGroup;
	}

	int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
		if (adapter == null || viewGroup == null) {
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
			itemView = adapter.getView(i, itemView, viewGroup);
			if (itemView.getLayoutParams() == null) {
				itemView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}

		// Add background padding to measured width
		if (background != null) {
			Rect mTempRect = new Rect();
			background.getPadding(mTempRect);
			width += mTempRect.left + mTempRect.right;
		}

		return width;
	}

	@Override
	public void show() {
		int w = measureContentWidth((SpinnerAdapter) adapter, getBackground());
		if (w > 0) {
			setContentWidth(w);
		}
		super.show();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		this.adapter = adapter;
		super.setAdapter(adapter);
	}

}
