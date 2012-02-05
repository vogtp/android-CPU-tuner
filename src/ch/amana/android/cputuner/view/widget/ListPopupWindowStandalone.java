package ch.amana.android.cputuner.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SpinnerAdapter;

public class ListPopupWindowStandalone extends PopupWindow {

	private ViewGroup viewGroup;
	private ListAdapter adapter;
	private ListView list;
	private View anchor;
	// Only measure this many items to get a decent max width.
	private static final int MAX_ITEMS_MEASURED = 15;

	@SuppressWarnings("unused")
	private ListPopupWindowStandalone(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@SuppressWarnings("unused")
	private ListPopupWindowStandalone(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@SuppressWarnings("unused")
	private ListPopupWindowStandalone(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressWarnings("unused")
	private ListPopupWindowStandalone(Context context) {
		super(context);
	}

	public ListPopupWindowStandalone(ViewGroup viewGroup, View anchor) {
		super(viewGroup.getContext());
		this.viewGroup = viewGroup;
		this.anchor = anchor;
		setFocusable(true);
		list = new ListView(viewGroup.getContext());
		setContentView(list);
		list.setVerticalScrollBarEnabled(false);
		list.setHorizontalScrollBarEnabled(false);
		list.setScrollContainer(false);
	}

	void measureContentWidth(SpinnerAdapter adapter, Drawable background) {
		if (adapter == null || viewGroup == null) {
			return;
		}

		int width = 0;
		int height = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec =
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec =
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

		// Make sure the number of items we'll measure is capped. If it's a huge data set
		// with wildly varying sizes, oh well.
		int start = Math.max(0, list.getSelectedItemPosition());
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
			height = Math.max(height, itemView.getMeasuredHeight());
		}

		height *= count;
		height += count + 2;

		// Add background padding to measured width
		if (background != null) {
			Rect mTempRect = new Rect();
			background.getPadding(mTempRect);
			width += mTempRect.left + mTempRect.right;
			height += mTempRect.top + mTempRect.left;
		}

		setWidth(width);
		setHeight(height);
	}

	public void show() {
		measureContentWidth((SpinnerAdapter) adapter, getBackground());
		super.showAsDropDown(anchor, anchor.getPaddingLeft(), 1);
	}

	public void setAdapter(ListAdapter adapter) {
		this.adapter = adapter;
		list.setAdapter(adapter);
	}

	public void setAnchorView(View anchor) {
		this.anchor = anchor;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		list.setOnItemClickListener(onItemClickListener);
	}

}
