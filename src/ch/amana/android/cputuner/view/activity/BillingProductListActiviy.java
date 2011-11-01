package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.Product;
import ch.almana.android.billing.PurchaseListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.Logger;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.view.adapter.BillingProductAdaper;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;

public class BillingProductListActiviy extends ListActivity implements PurchaseListener {

	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_PRODUCT_TYPE = "prodType";
	private BillingManager bm;
	private BillingProductAdaper billingProductAdaper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.list);
		CputunerActionBar actionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);

		String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (title == null) {
			actionBar.setTitle(R.string.title_products);
		} else {
			actionBar.setTitle(title);
		}

		actionBar.setHomeAction(new ActionBar.Action() {

			@Override
			public void performAction(View view) {
				onBackPressed();
			}

			@Override
			public int getDrawable() {
				return R.drawable.cputuner_back;
			}
		});

		bm = new BillingManager(this);
		updateView();
	}

	private void updateView() {
		Product[] products = BillingProducts.getProducts(this, getIntent().getIntExtra(EXTRA_PRODUCT_TYPE, -1), bm);
		billingProductAdaper = new BillingProductAdaper(this, products);
		getListView().setAdapter(billingProductAdaper);

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
		bm.addPurchaseListener(this);
	}

	@Override
	protected void onPause() {
		bm.removePurchaseListener(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		bm.release();
		super.onStop();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Product product = (Product) billingProductAdaper.getItem(position);
		if (!product.isManaged() || product.getCount() < 1) {
			try {
				bm.requestPurchase(product.getProductId());
			} catch (Throwable e) {
				Logger.w("Error requesting purchase", e);
			}
		}
		super.onListItemClick(l, v, position, id);
	}

	private void loadOldMarketBuyMeABeer() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:ch.almana.android.buymeabeer")));
	}

	@Override
	public void purchaseChanged(String pid, int count) {
		updateView();
		if (BillingProducts.statistics.equals(pid)) {
			SettingsStorage.getInstance().setAdvancesStatistics(count > 0);
		}
	}

}
