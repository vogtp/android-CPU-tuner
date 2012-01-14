package ch.amana.android.cputuner.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;
import ch.almana.android.billing.Product;
import ch.amana.android.cputuner.R;

public class BillingProductAdaper extends BaseAdapter {

	private final Product[] products;
	private final LayoutInflater layoutInflator;
	private final Context ctx;

	public BillingProductAdaper(Context ctx, Product[] products) {
		this.ctx = ctx;
		this.products = products;
		this.layoutInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return products.length;
	}

	@Override
	public Object getItem(int position) {
		return products[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineListItem view = (convertView != null) ? (TwoLineListItem) convertView : createView(parent);
		Product p = products[position];
		view.getText1().setText(p.getName());
		StringBuilder sb = new StringBuilder();
		if (p.isManaged()) {
			boolean installed = p.getCount() > 0;
			sb.append(ctx.getString(installed ? R.string.extention_installed : R.string.not_installed));
		}else {
			sb.append(ctx.getString(R.string.purchased));
			sb.append(" ");
			sb.append(p.getCount());
		}
		sb.append("\n");
		sb.append(p.getDesc());
		view.getText2().setText(sb.toString());
		return view;
	}

	private TwoLineListItem createView(ViewGroup parent) {
		TwoLineListItem item = (TwoLineListItem) layoutInflator.inflate(android.R.layout.simple_list_item_2, parent, false);
		item.getText1().setSingleLine();
		//		item.getText2().setSingleLine();
		//		item.getText1().setEllipsize(TextUtils.TruncateAt.END);
		//		item.getText2().setEllipsize(TextUtils.TruncateAt.END);
		return item;
	}
}
