package org.zywx.wbpalmstar.plugin.uexappmarket.activity;

import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AppsListAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AppPagerAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.NewMsg;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.ViewDataManager;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.DataCallBack;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.PackageInstalledReceiver;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AppMarketMainActivity extends Activity implements DataCallBack {

	public static AppMarketMainActivity sInstance;

	/**
	 * 监听安装和卸载的广播
	 */
	private PackageInstalledReceiver mPkgInstallReceiver;

	public static String IS_VISIBLE = "";
	private static View mainView;

	public static final String TAG = "AppMarketMainActivity";
	public static final String INTENT_KEY_URL = "url";
	private String requestUrl = null;
	private UpdateTaskList appsTaskList = new UpdateTaskList();
	List<AppBean> localApps;

	public static ViewDataManager mViewDataManager;

	public DragGridView gridView;

	private AppsListAdapter mAdapter;

	private ViewPager mAppViewPager;

	private AppPagerAdapter mAppPagerAdapter;

	private LinearLayout mAppIndicate;

	private int mAppPage = 0;

	// public static int itemHeight = 0;

	public static final String ACTION = "org.zywx.wbpalmstar.plugin.uexappmarket.BREAK";

	LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setContentView(EUExUtil
				.getResLayoutID("plugin_appmarket_ex_view_pagerpager"));

		sInstance = this;

		// DisplayMetrics dm = new DisplayMetrics();
		// getWindowManager().getDefaultDisplay().getMetrics(dm);

		// itemHeight = (EUExAppMarketEx.height/ Tools.ITEM_NUMBER);
		//
		// Log.e(EUExAppMarketEx.TAG, "屏幕的大小是 ：   width==" + dm.widthPixels
		// + "        height==" + dm.heightPixels);

		// 注册广播
		onRegister();
	}

	public void init() {
		String softToken = Tools.getSoftToken(this);

		if (softToken == null || softToken.length() == 0) {
			Toast.makeText(this, "softToken为空!", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}
		requestUrl = CommonUtility.URL_APP_LIST + softToken;

		Log.e(EUExAppMarketEx.TAG, "requestUrl   " + requestUrl);

		setupView();

		mViewDataManager = new ViewDataManager(this, requestUrl, this,
				appsTaskList);
		// setData(new AppBeanDao(this).getOrderInstallApp());
		// if (isFirstRun(this)) {
		// mViewDataManager.performAsyncLoadAppListAction();
		// return;
		// }

		localApps = new AppBeanDao(this).getOrderInstallApp();

		callBackRes(localApps);

		mViewDataManager.performAsyncLoadAppListAction();

	}

	public boolean isFirstRun(Context context) {
		SharedPreferences sp = context.getSharedPreferences("first_run", 0);
		return sp.getBoolean("firstRun", true);
	}

	private void setupView() {
		mAppViewPager = (ViewPager) findViewById(EUExUtil
				.getResIdID("plugin_ex_appmarker_viewpagerId"));

		mAppIndicate = (LinearLayout) findViewById(EUExUtil
				.getResIdID("plugin_appmarket_ex_app_indicate_view"));

		// gridView = (DragGridView) findViewById(EUExUtil.getResIdID("grid"));
		// gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		//
		// gridView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1,
		// int positon, long arg3) {
		// // TODO Auto-generated method stub
		// mAdapter.new ConvertClick(localApps, gridView, positon).init();
		// }
		// });

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setData(List<AppBean> list) {

		mAppPagerAdapter = (AppPagerAdapter) mAppViewPager.getAdapter();

		if (mAppPagerAdapter == null) {
			mAppPagerAdapter = new AppPagerAdapter(AppMarketMainActivity.this,
					(ArrayList<AppBean>) list, mAppViewPager);
			mAppViewPager.setAdapter(mAppPagerAdapter);
		} else {
			mAppPagerAdapter.refresh(list);
		}

		mAppViewPager.setOnPageChangeListener(new AppChangeListener());
		addIndicate(mAppIndicate, mAppPagerAdapter.getCount());
		showIndicate(mAppIndicate, mAppPage);

	}

	@SuppressWarnings("deprecation")
	public void addIndicate(LinearLayout parent, int count) {
		parent.removeAllViews();
		for (int i = 0; i < count; i++) {
			View child = inflater.inflate(EUExUtil
					.getResLayoutID("plugin_appmarket_ex_indicate_view"), null);
			ImageView iv = (ImageView) child.findViewById(EUExUtil
					.getResIdID("plugin_appmarket_ex_indicate_imageView"));
			
			iv.setBackgroundDrawable(getResources()
					.getDrawable(
							EUExUtil.getResDrawableID("plugin_appmarket_ex_indicate_ball_normal")));
			// }
			parent.addView(child);
		}
	}

	@SuppressWarnings("deprecation")
	private void showIndicate(ViewGroup parent, int position) {
		if (parent != null) {
			for (int i = 0, count = parent.getChildCount(); i < count; i++) {
				View view = parent.getChildAt(i);
				if (view != null) {
					ImageView iv = (ImageView) view
							.findViewById(EUExUtil
									.getResIdID("plugin_appmarket_ex_indicate_imageView"));
					if (i == position) {
						// iv.setImageResource(EUExUtil
						// .getResDrawableID("plugin_appmarket_ex_indicate_ball_current"));
						iv.setBackgroundDrawable(getResources()
								.getDrawable(
										EUExUtil.getResDrawableID("plugin_appmarket_ex_indicate_ball_current")));
					} else {
						// iv.setImageResource(EUExUtil
						// .getResDrawableID("plugin_appmarket_ex_indicate_ball_normal"));

						iv.setBackgroundDrawable(getResources()
								.getDrawable(
										EUExUtil.getResDrawableID("plugin_appmarket_ex_indicate_ball_normal")));
					}
				}
			}
		}
	}

	class AppChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			mAppViewPager.setCurrentItem(position, true);
			mAppPage = position;
			showIndicate(mAppIndicate, mAppPage);
		}

	}

	@Override
	public void callBackRes(List<AppBean> result) {
		setData(result);
	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences sp = getSharedPreferences("first_run", 0);
		Editor edit = sp.edit();
		edit.putBoolean("firstRun", false);

		edit.commit();
	}

	public void showAppsIndicate(int count) {
	}

	@Override
	protected void onDestroy() {

		if (mPkgInstallReceiver != null) {
			this.unregisterReceiver(mPkgInstallReceiver);
		}

		super.onDestroy();

	}

	public UpdateTaskList getAppsTaskList() {
		return appsTaskList;
	}

	public ViewDataManager getViewDataManager() {
		return mViewDataManager;
	}

	public AppsListAdapter getAppPagerAdapter() {
		return mAdapter;
	}

	public static AppMarketMainActivity getInstance() {
		return sInstance;
	}

	public void onRegister() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION);
		registerReceiver(myBroadcastReceiver, filter);
	}

	/**
	 * 该广播主要是在列表界面中点击返回按钮之后出现不能刷新这个界面而创建广播，
	 */
	public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			try {
				setData(new AppBeanDao(AppMarketMainActivity.this)
						.getOrderInstallApp());

				// mViewDataManager.performAsyncLoadAppListAction();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	public void setNewMsgNotify(List<NewMsg> mNew) {

		if (mNew == null) {
			return;
		}
		new AppBeanDao(this).setNewMsg(mNew);

		setData(new AppBeanDao(this).getOrderInstallApp());

	}

}
