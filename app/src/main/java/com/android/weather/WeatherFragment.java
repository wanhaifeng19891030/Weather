package com.android.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.weather.api.ApiManager;
import com.android.weather.api.ApiManager.Area;
import com.android.weather.dynamicweather.BaseDrawer;
import com.android.weather.dynamicweather.BaseDrawer.Type;
import com.android.weather.utils.CitycacheUtils;
import com.android.weather.utils.FileManager;
import com.android.weather.utils.MxxNetworkUtil;
import com.android.weather.utils.UiUtil;
import com.android.weather.widget.AqiView;
import com.android.weather.widget.AstroView;
import com.android.weather.widget.DailyForecastView;
import com.android.weather.widget.HourlyForecastView;
import com.android.weather.widget.PullRefreshLayout;
import com.google.gson.Gson;

import java.util.List;

public class WeatherFragment extends BaseFragment implements  PullRefreshLayout.OnRefreshListener {

	private View mRootView;
	private InfoBean mInfoBean;
	private DailyForecastView mDailyForecastView;
	private PullRefreshLayout pullRefreshLayout;
	private HourlyForecastView mHourlyForecastView;
	private AqiView mAqiView;
	private AstroView mAstroView;
	private TextView tv_aqi;
	private Area mArea;
	private ScrollView mScrollView;
	private BaseDrawer.Type mDrawerType = Type.UNKNOWN_D;
	public BaseDrawer.Type getDrawerType() {
		return this.mDrawerType;
	}

	private static final String BUNDLE_EXTRA_AREA = "BUNDLE_EXTRA_AREA";
	private static final String BUNDLE_EXTRA_WEATHER = "BUNDLE_EXTRA_WEATHER";

	public static WeatherFragment makeInstance(@NonNull Area area) {
		WeatherFragment fragment = new WeatherFragment();
		Bundle bundle = new Bundle();
		if(area != null) {
			bundle.putSerializable(BUNDLE_EXTRA_AREA, area);
		}
		fragment.setArguments(bundle);
		return fragment;
	}

	private void fetchArguments() {
		if (this.mArea == null) {
			try {
				this.mArea = (Area) getArguments().getSerializable(BUNDLE_EXTRA_AREA);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(this.mInfoBean == null){
           try{
			  String str =  FileManager.getFile(this.mArea.id);
              this.mInfoBean = new Gson().fromJson(str, InfoBean.class);
		   }catch (Exception e){
			   e.printStackTrace();
		   }
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.fragment_weather, null);
			mDailyForecastView = (DailyForecastView) mRootView.findViewById(R.id.w_dailyForecastView);
			pullRefreshLayout = (PullRefreshLayout) mRootView.findViewById(R.id.w_PullRefreshLayout);
			mHourlyForecastView = (HourlyForecastView) mRootView.findViewById(R.id.w_hourlyForecastView);
			mAqiView = (AqiView) mRootView.findViewById(R.id.w_aqi_view);
			mAstroView = (AstroView) mRootView.findViewById(R.id.w_astroView);
			mScrollView = (ScrollView) mRootView.findViewById(R.id.w_WeatherScrollView);
			tv_aqi = (TextView) mRootView.findViewById(R.id.w_aqi_text);
			pullRefreshLayout.setOnRefreshListener(this);
		} else {
			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.scrollTo(0, 0);
				}
			});
		}
		return mRootView;
	}

	public void refreshCity(String city){
		ApiManager.getInstace().updateWeather(getActivity(), city, new ApiManager.ApiListener() {
			@Override
			public void onUpdateError() {
				if(pullRefreshLayout!=null){
					pullRefreshLayout.setRefreshing(false);
				}

				if(activityIsRunning()){
					UiUtil.toastDebug(getActivity(), getResources().getString(R.string.update_error));
				}
			}

			@Override
			public void onReceiveWeather(InfoBean infoBean, boolean updated) {

				if(pullRefreshLayout!=null){
					pullRefreshLayout.setRefreshing(false);
				}
				if (updated &&  activityIsRunning()) {
					UiUtil.toastDebug(getActivity(), getResources().getString(R.string.update_sucess));
					WeatherFragment.this.mInfoBean = infoBean;
					saveCityToJson();
					updateWeatherView();
				}
			}
		});
	}

    /**
	 * 判断Activity是否正在运行
	 * @return
     */
	private boolean activityIsRunning(){
		if (getActivity() == null || getActivity().isDestroyed() || getActivity().isFinishing()) {
			return false;
		}
		return true;
	}

	/***
	 * 保存当前城市
	 */
	private void saveCityToJson(){
		if(mInfoBean != null){
			CitycacheUtils.getInstace().cacheCurrentCity(getActivity(),mInfoBean);
		}
	}

	@Override
	public void onRefresh() {
		//mArea.id
        refreshCity(getCityName());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		super.onCreate(savedInstanceState);
		fetchArguments();
		if(this.mInfoBean != null){
           updateWeatherView();
		}else{
			postRefresh();
		}

        getActivity().getIntent().getBooleanExtra("transact_from_mms",false);
	}

	public String getCityName() {
		fetchArguments();
		if (this.mArea != null) {
			return mArea.city;
		} else {
			return "Error";
		}
	}

    /**
	 * 更新天气背景
     */
	private void updateDrawerTypeAndNotify() {
		ApiManager.getInstace().setInfoBean(mInfoBean);
		final BaseDrawer.Type curType = ApiManager.getInstace().convertWeatherType();
		this.mDrawerType = curType;
		notifyActivityUpdate();
	}

	private void postRefresh() {
		if (pullRefreshLayout != null) {
			Activity activity = getActivity();
			if (activity != null) {
				if (MxxNetworkUtil.isNetworkAvailable(activity)) {
					pullRefreshLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							pullRefreshLayout.setRefreshing(true, true);
						}
					}, 100);
				}else {
					if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
						return;
					}
				}
			}
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}

	public void updateWeatherView(){

		if(mInfoBean==null) return;
		try {
			//绘制背景
			updateDrawerTypeAndNotify();
			//更新时间
			if (ApiManager.getInstace().isToday(mInfoBean.getUpdatetime())) {
				showLog(mInfoBean.getUpdatetime());
				setTextViewString(R.id.w_basic_update_loc, mInfoBean.getUpdatetime().substring(11, 16) + " " + getResources().getString(R.string.release));
			} else {
				setTextViewString(R.id.w_basic_update_loc, mInfoBean.getUpdatetime().substring(5, 16) + " " + getResources().getString(R.string.release));
			}

			//最高和最低温
			String maxTemp = mInfoBean.getTemphigh();
			String minTemp = mInfoBean.getTemplow();
			if (!TextUtils.isEmpty(maxTemp) && !TextUtils.isEmpty(minTemp)) {
				setTextViewString(R.id.w_now_max_min_temp, maxTemp + "°/" + minTemp + "°");
			}
			//24小时天气
			List<InfoBean.Hourly> hourlys = mInfoBean.getHourly();
			if(hourlys!=null&&hourlys.size()>0){
				mHourlyForecastView.setData(hourlys);
				//温度
				String temp = hourlys.get(0).getTemp();
				if (!TextUtils.isEmpty(temp)) {
					setTextViewString(R.id.w_now_tmp, temp);
					setTextViewString(R.id.w_todaydetail_temp, temp + "°");
				}

				//天气
				String weather = hourlys.get(0).getWeather();
				if (!TextUtils.isEmpty(weather)) {
					setTextViewString(R.id.w_now_cond_text, weather + "  ");
				}
			}
			//一星期天气实况
			List<InfoBean.Daily> dailys = mInfoBean.getDaily();
			if(dailys!=null&&dailys.size()>0){
				mDailyForecastView.setData(dailys);
			}
			//空气质量
			InfoBean.Aqi aqi = mInfoBean.getAqi();
			if (aqi != null) {
				//空气质量指数类别，有“优、良、轻度污染、中度污染、重度污染、严重污染”6类
				int mAqi = Integer.parseInt(aqi.getAqi());
				String quality = aqi.getQuality();
				if (quality.length() >= 4) {
					quality = quality.substring(0, 2);
				}
				setTextViewString(R.id.w_aqi_text, quality);
				setTextViewString(R.id.tv_aqi_title, getResources().getString(R.string.aqi1));
				if (0 <= mAqi && mAqi <= 50) {//优 #25B185
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_0);
				} else if (51 <= mAqi && mAqi <= 100) {//良 #EDC444
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_1);
				} else if (101 <= mAqi && mAqi <= 150) {//轻度污染 #F29E2F
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_2);
				} else if (151 <= mAqi && mAqi <= 200) {//中度污染 #E95E5E
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_3);
				} else if (201 <= mAqi && mAqi <= 300) {//重度污染 #9959BA
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_4);
				} else {//严重污染 #E12424
					tv_aqi.setBackgroundResource(R.drawable.aqi_bg_5);
				}
				//空气质量刻度图
				mAqiView.setData(Integer.parseInt(aqi.getAqi()), aqi.getQuality());
				//相对湿度
				setTextViewString(R.id.w_humidity_text, getResources().getString(R.string.humidity) + " " + mInfoBean.getHumidity() + "%");
				//PM2.5
				setTextViewString(R.id.w_aqi_pm25, aqi.getIpm2_5() + "μg/m³");
				//PM10
				setTextViewString(R.id.w_aqi_pm10, aqi.getIpm10() + "μg/m³");
				//二氧化硫
				setTextViewString(R.id.w_aqi_so2, aqi.getIso2() + "μg/m³");
				//二氧化氮
				setTextViewString(R.id.w_aqi_no2, aqi.getIno2() + "μg/m³");
			}

			//太阳和风
			mAstroView.setData(mInfoBean.getDaily().get(0).getSunrise(), mInfoBean.getDaily().get(0).getSunset(),
					mInfoBean.getPressure(), mInfoBean.getWindspeed(), mInfoBean.getWinddirect(), mInfoBean.getWindpower());

			//生活指数
			List<InfoBean.Index> indexs = mInfoBean.getIndex();
			if (!indexs.isEmpty()) {
				for (InfoBean.Index index : indexs) {
					if (index.getIname().equals(getResources().getString(R.string.suggestion_drsg))) {
						setTextViewString(R.id.w_suggestion_drsg_title, index.getIname());
						setTextViewString(R.id.w_suggestion_drsg_brf, index.getIvalue());
						setTextViewString(R.id.w_suggestion_drsg, index.getDetail());
					} else if (index.getIname().equals(getResources().getString(R.string.suggestion_uv))) {
						setTextViewString(R.id.w_suggestion_uv_title, index.getIname());
						setTextViewString(R.id.w_suggestion_uv_brf, index.getIvalue());
						setTextViewString(R.id.w_suggestion_uv, index.getDetail());
					} else if (index.getIname().equals(getResources().getString(R.string.suggestion_flu))) {
						setTextViewString(R.id.w_suggestion_flu_title, index.getIname());
						setTextViewString(R.id.w_suggestion_flu_brf, index.getIvalue());
						setTextViewString(R.id.w_suggestion_flu, index.getDetail());
					} else if (index.getIname().equals(getResources().getString(R.string.suggestion_cw))) {
						setTextViewString(R.id.w_suggestion_cw_title, index.getIname());
						setTextViewString(R.id.w_suggestion_cw_brf, index.getIvalue());
						setTextViewString(R.id.w_suggestion_cw, index.getDetail());
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setTextViewString(int textViewId, String str) {
		TextView tv = (TextView) mRootView.findViewById(textViewId);
		if (tv != null) {
			tv.setText(str);
		} else {
			toast("Error NOT found textView id->" + Integer.toHexString(textViewId));
		}
	}

	@Override
	public String getTitle() {
		return getCityName();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onSelected() {
		// checkRefresh();
	}
}
