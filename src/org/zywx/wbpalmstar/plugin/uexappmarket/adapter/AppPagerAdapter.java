package org.zywx.wbpalmstar.plugin.uexappmarket.adapter;

import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class AppPagerAdapter extends PagerAdapter {

	public List<AppBean> mList = new ArrayList<AppBean>();
	private List<View> mViewContainer = new ArrayList<View>();

	private final int PAGER_COUNT = 8;

	private Context mContext;
	private ViewPager mViewPager;

	private LayoutInflater mInflater;

	private AppMarketMainActivity mActivity;
	private UpdateTaskList mAppTaskList;

	public AppPagerAdapter(Context mContext, ArrayList<AppBean> list,
			ViewPager viewPager) {
		// TODO Auto-generated constructor stub

		this.mContext = mContext;
		EUExUtil.init(mContext);
		mInflater = LayoutInflater.from(mContext);

		if (list != null && list.size() > 0) {
			mList.addAll(list);
		}
		mList.add(new AppBean());
		mViewPager = viewPager;
		// density = mContext.getResources().getDisplayMetrics().density;
		init();
	}

	// 分页逻辑

	void init() {

		int pages = mList.size() / PAGER_COUNT;
		pages = mList.size() % PAGER_COUNT == 0 ? pages : ++pages;

		System.out.println("MainActivity: pages : " + pages);

		if (mViewContainer.size() < pages) {
			for (int i = 0, len = (pages - mViewContainer.size()); i < len; i++) {

				View view = mInflater.inflate(EUExUtil
						.getResLayoutID("plugin_appmarket_ex_view_pager_item"),
						null);
				mViewContainer.add(view);
			}
		} else if (pages < mViewContainer.size()) {
			for (int i = 0, len = (mViewContainer.size() - pages); i < len; i++) {
				mViewContainer.remove(i);
			}
		}
		for (int i = 0, len = mViewContainer.size(); i < len; i++) {
			DragGridView gridView = (DragGridView) mViewContainer.get(i);

			AppsListAdapter adapter = (AppsListAdapter) gridView.getAdapter();

			boolean isLastPager = (getCount() - 1) == i;
			List<AppBean> beans = getGridData(mList, i);

			Log.i("beans", "beans===" + beans.size());

			if (adapter == null) {
				adapter = new AppsListAdapter(mContext, beans, this, gridView,
						isLastPager, mViewPager, mList);
				gridView.setAdapter(adapter);
			} else {
				adapter.refresh(beans, isLastPager, mList);
			}
		}

		// 改变门户界面的高度，回调
		if (mList.size() <= 4) {
			EUExAppMarketEx.onCallBack.cbOpen(EUExAppMarketEx.webWidth
					/ Tools.ITEM_HEIGHT);
		} else {
			EUExAppMarketEx.onCallBack
					.cbOpen((EUExAppMarketEx.webWidth / Tools.ITEM_HEIGHT) * 2);
		}
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mViewContainer.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		try {
			container.removeView((View) object);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		container.addView(mViewContainer.get(position), 0);
		return mViewContainer.get(position);
	}

	private List<AppBean> getGridData(List<AppBean> list, int pos) {

		if (list == null || list.size() == 0) {
			return null;
		}

		int start = pos * PAGER_COUNT;
		int end = (pos + 1) * PAGER_COUNT;

		if (start >= list.size()) {
			start = list.size();
		}

		if (end > list.size()) {
			end = list.size();
		}

		if (start > end) {
			start = end;
		}

		List<AppBean> dataSet = list.subList(start, end);
		return dataSet;
	}

	public void refresh(List<AppBean> apps) {
		if (apps != null && apps.size() > 0) {
			mList.clear();
			mList.addAll(apps);
			mList.add(new AppBean());
			init();
			notifyDataSetChanged();
		}
	}

	public ViewPager getViewPager() {
		return mViewPager;
	}

}
