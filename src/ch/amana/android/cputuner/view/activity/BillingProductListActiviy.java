package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.Product;
import ch.almana.android.billing.PurchaseListener;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.adapter.BillingProductAdaper;
import ch.amana.android.cputuner.view.widget.CputunerActionBar;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class BillingProductListActiviy extends ListActivity implements PurchaseListener {

	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_PRODUCT_TYPE = "prodType";
	private BillingManager bm;
	private BillingProductAdaper billingProductAdaper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list);

		String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (title == null) {
			title = getString(R.string.title_products);
		}

		CputunerActionBar cputunerActionBar = (CputunerActionBar) findViewById(R.id.abCpuTuner);
		if (SettingsStorage.getInstance(this).hasHoloTheme()) {
			getActionBar().setSubtitle(title);
			cputunerActionBar.setVisibility(View.GONE);
		} else {
			cputunerActionBar.setTitle(title);
			cputunerActionBar.setHomeAction(new ActionBar.Action() {

				@Override
				public void performAction(View view) {
					onBackPressed();
				}

				@Override
				public int getDrawable() {
					return R.drawable.cputuner_back;
				}
			});

			cputunerActionBar.addAction(new Action() {
				@Override
				public void performAction(View view) {
					refreshFromMarket();
				}

				@Override
				public int getDrawable() {
					return R.drawable.ic_menu_refresh;
				}
			});
		}

		bm = new BillingManager(this);
		bm.addPurchaseListener(this);
		updateView();
	}

	private void reinitaliseOwnedItems() {
		SettingsStorage.getInstance().setAdvancesStatistics(bm.getCountOfProduct(BillingProducts.statistics) > 0);
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
		super.onPause();
		//		reinitaliseOwnedItems();
	}

	@Override
	protected void onDestroy() {
		bm.removePurchaseListener(this);
		bm.release();
		super.onDestroy();
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
		if (product.isManaged() && product.getCount() > 0) {
			if (BillingProducts.statistics.equals(product.getProductId())) {
				SettingsStorage settings = SettingsStorage.getInstance();
				if (settings.isAdvancesStatistics()) {
					settings.setAdvancesStatistics(false);
					Toast.makeText(this, product.getName() + ": " + getString(R.string.not_enabled), Toast.LENGTH_SHORT).show();
				} else {
					settings.setAdvancesStatistics(true);
					Toast.makeText(this, product.getName() + ": " + getString(R.string.enabled), Toast.LENGTH_SHORT).show();
				}
				updateView();
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

	@Override
	public void billingSupported(boolean supported) {
		if (!supported) {
			Toast.makeText(this, "Billing not supported!", Toast.LENGTH_LONG).show();
			getListView().setEnabled(false);
			if (getIntent().getIntExtra(EXTRA_PRODUCT_TYPE, -1) == BillingProducts.PRODUCT_TYPE_BUY_ME_BEER) {
				loadOldMarketBuyMeABeer();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.refresh_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.itemRefresh:
			refreshFromMarket();
			return true;

		default:
			if (GeneralMenuHelper.onOptionsItemSelected(this, item, HelpActivity.PAGE_INDEX)) {
				return true;
			}
		}
		return false;
	}

	private void refreshFromMarket() {
		bm.restoreTransactionsFromMarket();
		reinitaliseOwnedItems();
	}

}
