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

	public static final int PRODUCT_TYPE_BUY_ME_BEER = 2;

	public static Product[] getProducts(Context ctx, int type, BillingManager bm) {
		switch (type) {
		case PRODUCT_TYPE_EXTENTIONS:
			return getExtentions(ctx, bm);
		case PRODUCT_TYPE_BUY_ME_BEER:
			return getBuyBeer(ctx, bm);

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

	public static Product[] getBuyBeer(Context ctx, BillingManager bm) {
		Product[] products = new Product[4];
		products[0] = new Product("kids.cookie", ctx.getString(R.string.name_buy_cookie), ctx.getString(R.string.desc_buy_cookie), Managed.UNMANAGED);
		products[1] = new Product("bar.beer", ctx.getString(R.string.name_buy_me_a_beer), ctx.getString(R.string.desc_buy_beer), Managed.UNMANAGED);
		products[2] = new Product("bar.whiskey", ctx.getString(R.string.name_buy_me_a_whiskey), ctx.getString(R.string.desc_buy_whiskey), Managed.UNMANAGED);
		products[3] = new Product("kids.toys", ctx.getString(R.string.name_buy_toy), ctx.getString(R.string.desc_buy_toy), Managed.UNMANAGED);

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
