package ch.amana.android.cputuner.helper;

import android.content.Context;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.Product;
import ch.almana.android.billing.backend.BillingFacade.Managed;
import ch.amana.android.cputuner.R;


public class BillingProducts {

	public static final String beer = "bar.beer";

	public static final String statistics = "extension.statistics";

	public static final int PRODUCT_TYPE_EXTENTIONS = 1;

	public static Product[] getProducts(Context ctx, int type, BillingManager bm) {
		switch (type) {
		case PRODUCT_TYPE_EXTENTIONS:
			return getExtentions(ctx, bm);

		default:
			return null;
		}
	}

	public static Product[] getExtentions(Context ctx, BillingManager bm) {
		Product[] products = new Product[1];
		products[0] = new Product(statistics, ctx.getString(R.string.name_extention_stats), ctx.getString(R.string.desc_extention_stats), Managed.MANAGED);

		getPoductsCount(bm, products);
		return products;
	}

	private static void getPoductsCount(BillingManager bm, Product[] products) {
		for (int i = 0; i < products.length; i++) {
			Product p = products[i];
			p.setCount(bm.getCountOfProduct(p.getProductId()));
		}
	}

}
