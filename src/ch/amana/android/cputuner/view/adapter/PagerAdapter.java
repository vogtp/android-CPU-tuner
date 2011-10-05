package ch.amana.android.cputuner.view.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import ch.amana.android.cputuner.view.widget.PagerHeader;

public class PagerAdapter extends FragmentPagerAdapter
		implements ViewPager.OnPageChangeListener, PagerHeader.OnHeaderClickListener {

	private final Context mContext;
	private final ViewPager mPager;
	private final PagerHeader mHeader;
	private final ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();
	private Fragment currentPage;
	private boolean first = true;

	public interface PagerItem {

		boolean onOptionsItemSelected(Activity act, MenuItem item);

		void onPrepareOptionsMenu(Menu menu);

		void setCurrentPage(Fragment f);

	}



	

	static final class PageInfo {
		private final Class<? extends PagerItem> clss;
		private final Bundle args;

		PageInfo(Class<? extends PagerItem> _clss, Bundle _args) {
			clss = _clss;
			args = _args;
		}
	}

	public PagerAdapter(FragmentActivity activity, ViewPager pager,
			PagerHeader header) {
		super(activity.getSupportFragmentManager());
		mContext = activity;
		mPager = pager;
		mHeader = header;
		mHeader.setOnHeaderClickListener(this);
		mPager.setAdapter(this);
		mPager.setOnPageChangeListener(this);
	}

	public void addPage(Class<? extends PagerItem> clss, int res) {
		addPage(clss, null, res);
	}

	public void addPage(Class<? extends PagerItem> clss, String title) {
		addPage(clss, null, title);
	}

	public void addPage(Class<? extends PagerItem> clss, Bundle args, int res) {
		addPage(clss, null, mContext.getResources().getString(res));
	}

	public void addPage(Class<? extends PagerItem> clss, Bundle args, String title) {
		PageInfo info = new PageInfo(clss, args);
		mPages.add(info);
		mHeader.add(0, title);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mPages.size();
	}

	@Override
	public Fragment getItem(int position) {
		PageInfo info = mPages.get(position);
		Fragment f = Fragment.instantiate(mContext, info.clss.getName(), info.args);
		if (first && position == 0) {
			first = false;
			((PagerItem) f).setCurrentPage(f);
		}
		return f;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		mHeader.setPosition(position, positionOffset, positionOffsetPixels);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onHeaderClicked(int position) {

	}

	@Override
	public void onHeaderSelected(int position) {
		mPager.setCurrentItem(position);
	}

	@Override
	public void onPageSelected(int position) {
		mHeader.setDisplayedPage(position);
		currentPage = getItem(position);
		((PagerItem) currentPage).setCurrentPage(currentPage);
	}

	public void onPrepareOptionsMenu(Menu menu) {
		currentPage.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return currentPage.onOptionsItemSelected(item);
	}

	public Fragment getCurrentItem() {
		Fragment f = currentPage;
		if (f == null) {
			f = getItem(0);
		}
		return f;
	}

}