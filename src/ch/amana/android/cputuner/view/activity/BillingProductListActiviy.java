package ch.amana.android.cputuner.view.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import ch.almana.android.billing.BillingManager;
import ch.almana.android.billing.PurchaseListener;
import ch.almana.android.billing.products.Product;
import ch.amana.android.cputuner.R;
import ch.amana.android.cputuner.helper.BillingProducts;
import ch.amana.android.cputuner.helper.GeneralMenuHelper;
import ch.amana.android.cputuner.helper.SettingsStorage;
import ch.amana.android.cputuner.log.Logger;
import ch.amana.android.cputuner.view.adapter.BillingProductAdaper;
import ch.amana.android.cputuner.view.preference.AdvStatisticsExtensionSettings;
import ch.amana.android.cputuner.view.preference.AppwidgetExtensionSettings;
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
	public void onCreate(final Bundle savedInstanceState) {
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
				public void performAction(final View view) {
					onBackPressed();
				}

				@Override
				public int getDrawable() {
					return R.drawable.cputuner_back;
				}
			});

			cputunerActionBar.addAction(new Action() {
				@Override
				public void performAction(final View view) {
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
		SettingsStorage settings = SettingsStorage.getInstance();
		settings.setAdvancesStatistics(bm.getCountOfProduct(BillingProducts.statistics) > 0);
		settings.setHasWidget(bm.getCountOfProduct(BillingProducts.widget) > 0);
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
	protected void onDestroy() {
		bm.removePurchaseListener(this);
		bm.release();
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
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
				startActivity(new Intent(this, AdvStatisticsExtensionSettings.class));
			}
			if (BillingProducts.widget.equals(product.getProductId())) {
				startActivity(new Intent(this, AppwidgetExtensionSettings.class));
			}
		}
		super.onListItemClick(l, v, position, id);
	}

	private void loadOldMarketBuyMeABeer() {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:ch.almana.android.buymeabeer")));
	}

	@Override
	public void purchaseChanged(final String pid, final int count) {
		updateView();
		if (BillingProducts.statistics.equals(pid)) {
			boolean installed = count > 0;
			SettingsStorage settings = SettingsStorage.getInstance();
			settings.setAdvancesStatistics(installed);
			settings.setRunStatisticsService(installed);
		} else if (BillingProducts.widget.equals(pid)) {
			SettingsStorage.getInstance().setHasWidget(count > 0);
		}
	}

	@Override
	public void billingSupported(final boolean supported) {
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
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		//		getMenuInflater().inflate(R.menu.gerneral_help_menu, menu);
		getMenuInflater().inflate(R.menu.refresh_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
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

	public static Intent getBeerIntent(final Context ctx) {
		Intent i = new Intent(ctx, BillingProductListActiviy.class);
		i.putExtra(EXTRA_TITLE, ctx.getString(R.string.prefBuyMeABeer));
		i.putExtra(EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_BUY_ME_BEER);
		return i;
	}

	public static Intent getExtentionsIntent(final Context ctx) {
		Intent i = new Intent(ctx, BillingProductListActiviy.class);
		i.putExtra(EXTRA_TITLE, ctx.getString(R.string.title_extentions));
		i.putExtra(EXTRA_PRODUCT_TYPE, BillingProducts.PRODUCT_TYPE_EXTENTIONS);
		return i;
	}
}
