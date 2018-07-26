package com.android.weather.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.weather.FutureWeatherInfo;
import com.android.weather.InfoBean;
import com.android.weather.R;
import com.android.weather.api.entity.Weather;
import com.android.weather.dynamicweather.BaseDrawer.Type;
import com.android.weather.utils.HttpUtils;
import com.android.weather.utils.MxxPreferenceUtil;
import com.android.weather.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.qucii.sdk.http.ICallback;
import com.qucii.sdk.transfer.Resp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat") @SuppressWarnings("deprecation")
public class ApiManager implements ICallback<InfoBean> {

	private final String TAG = ApiManager.class.getSimpleName();
	private final String SUCESS = "ok";
	private Context mContext;
	private static ApiManager apiManager;
	private ApiListener apiListener;
	private ArrayList<Area> areas;

	public static ApiManager getInstace(){
		if(apiManager == null){
           apiManager = new ApiManager();
		}
		return apiManager;
	}

	public interface ApiListener {
		//public void onReceiveWeather(Weather weather, boolean updated);
		public void onReceiveWeather(InfoBean infoBean, boolean updated);
		public void onUpdateError();
	}

	@Override
	public void onSuccess(Resp<InfoBean> resp) {
		if(TextUtils.equals(resp.desc,SUCESS)){
			if (apiListener != null) {
				apiListener.onReceiveWeather(resp.data, true);
			}
		}
	}

	@Override
	public void onFailure(int i, String s) {
		if(apiListener != null){
			apiListener.onUpdateError();
		}
	}

	public ArrayList<Area> loadSelectedArea(Context context) {
		mContext = context;
		if(areas == null){
			areas = new ArrayList<Area>();
		}else{
			areas.clear();
		}
		String json = MxxPreferenceUtil.getPrefString(context, MxxPreferenceUtil.KEY_SELECTED_AREA, "");
		if (TextUtils.isEmpty(json)) {
			return areas;
		}
		try {
			Area[] aa = new Gson().fromJson(json, Area[].class);
			if (aa != null) {
				Collections.addAll(areas, aa);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return areas;
	}

	public void updateWeather(@NonNull final Context context, @NonNull String areaId, @NonNull final ApiListener apiListener) {
		if (TextUtils.isEmpty(areaId)) {
			return;
		}
		this.mContext = context;
		this.apiListener = apiListener;
		HttpUtils.getInstance().get(areaId,this);
	}

	/**
	 * 是否需要更新Weather数据 1小时15分钟之内的return false;
	 * 传入null或者有问题的weather也会返回true
	 * @param weather
	 * @return
	 */
	public boolean isNeedUpdate(Weather weather) {
		if (!acceptWeather(weather)) {
			return true;
		}
		try {
			final String updateTime = weather.get().basic.update.loc;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date updateDate = format.parse(updateTime);
			Date curDate = new Date();
			long interval = curDate.getTime() - updateDate.getTime();// 时间间隔 ms
			if ((interval >= 0) && (interval < 75 * 60 * 1000)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 是否是今天2015-11-05 04:00 合法data格式： 2015-11-05 04:00 或者2015-11-05
	 * @param date
	 * @return
	 */
	public boolean isToday(String date) {
		if (TextUtils.isEmpty(date) || date.length() < 10) {// 2015-11-05
			return false;
		}
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String today = format.format(new Date());
			if (TextUtils.equals(today, date.substring(0, 10))) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 是否是合法的Weather数据
	 * @param weather
	 * @return
	 */
	public boolean acceptWeather(Weather weather) {
		if (weather == null || !weather.isOK()) {
			return false;
		}
		return true;
	}

	/**
	 * 把Weather转换为对应的BaseDrawer.Type
	 * @return
	 */
	public Type convertWeatherType() {
		if (infoBean == null) {
			return Type.DEFAULT;
		}
		final boolean isNight = isNight();
		try {
			final int w = Integer.valueOf(infoBean.getHourly().get(0).getImg());
			switch (w) {
			case 0:
				return isNight ? Type.CLEAR_N : Type.CLEAR_D;
			case 1:// 多云
			case 102:// 少云
			case 103:// 晴间多云
				return isNight ? Type.CLOUDY_N : Type.CLOUDY_D;
			case 2:// 阴
				return isNight ? Type.OVERCAST_N : Type.OVERCAST_D;
				// 200 - 213是风
			case 200:
			case 201:
			case 202:
			case 203:
			case 204:
			case 205:
			case 206:
			case 207:
			case 208:
			case 209:
			case 210:
			case 211:
			case 212:
			case 213:
				return isNight ? Type.WIND_N : Type.WIND_D;
			case 3:// 阵雨Shower Rain
			case 4:// 强阵雨 Heavy Shower Rain
			case 5:// 雷阵雨 Thundershower
			case 6:// 强雷阵雨 Heavy Thunderstorm
			case 7:// 雷阵雨伴有冰雹 Hail
			case 8:// 小雨 Light Rain
			case 9:// 中雨 Moderate Rain
			case 10:// 大雨 Heavy Rain
			case 11:// 极端降雨 Extreme Rain
			case 12:// 毛毛雨/细雨 Drizzle Rain
			case 19:// 暴雨 Storm
			case 21:// 大暴雨 Heavy Storm
			case 22:// 特大暴雨 Severe Storm
			case 23:// 冻雨 Freezing Rain
			case 24:
			case 25:
			case 301:
				return isNight ? Type.RAIN_N : Type.RAIN_D;
			case 13:// 小雪 Light Snow
			case 14:// 中雪 Moderate Snow
			case 15:// 大雪 Heavy Snow
			case 16:// 暴雪 Snowstorm
			case 17:// 阵雪 Snow Flurry
			case 26:
			case 27:
			case 28:
			case 302:
				return isNight ? Type.SNOW_N : Type.SNOW_D;
			case 404:// 雨夹雪 Sleet
			case 405:// 雨雪天气 Rain And Snow
			case 406:// 阵雨夹雪 Shower Snow
				return isNight ? Type.RAIN_SNOW_N : Type.RAIN_SNOW_D;
			case 32:// 薄雾
			case 18:// 雾
			case 49:
			case 57:
			case 58:
				return isNight ? Type.FOG_N : Type.FOG_D;
			case 53:// 霾
			case 54:
			case 55:
			case 56:
			case 29:// 浮尘
				return isNight ? Type.HAZE_N : Type.HAZE_D;
			case 30:// 扬沙
			case 506:// 火山灰
			case 20:// 沙尘暴
			case 31:// 强沙尘暴
				return isNight ? Type.SAND_N : Type.SAND_D;
			default:
				return isNight ? Type.UNKNOWN_N : Type.UNKNOWN_D;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isNight ? Type.UNKNOWN_N : Type.UNKNOWN_D;
	}

	/**
	 * 转换日期2015-11-05为今天、明天、昨天，或者是星期几
	 * 
	 * @param date
	 * @return
	 */
	public String prettyDate(String date) {
		try {
			final String[] strs = date.split("-");
			final int year = Integer.valueOf(strs[0]);
			final int month = Integer.valueOf(strs[1]);
			final int day = Integer.valueOf(strs[2]);
			Calendar c = Calendar.getInstance();
			int curYear = c.get(Calendar.YEAR);
			int curMonth = c.get(Calendar.MONTH) + 1;// Java月份从0月开始
			int curDay = c.get(Calendar.DAY_OF_MONTH);
			if (curYear == year && curMonth == month) {
				if (curDay == day) {
					return mContext.getResources().getString(R.string.today);
				} else if ((curDay + 1) == day) {
					return mContext.getResources().getString(R.string.tomorrow);
				} else if ((curDay - 1) == day) {
					return mContext.getResources().getString(R.string.yesterday);
				}
			}
			c.set(year, month - 1, day);
			// http://www.tuicool.com/articles/Avqauq
			// 一周第一天是否为星期天
			boolean isFirstSunday = (c.getFirstDayOfWeek() == Calendar.SUNDAY);
			// 获取周几
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			// 若一周第一天为星期天，则-1
			if (isFirstSunday) {
				dayOfWeek = dayOfWeek - 1;
				if (dayOfWeek == 0) {
					dayOfWeek = 7;
				}
			}
			if(dayOfWeek == 7){
				dayOfWeek = 0;
			}
			// 若当天为2014年10月13日（星期一），则打印输出：1
			// 若当天为2014年10月17日（星期五），则打印输出：5
			// 若当天为2014年10月19日（星期日），则打印输出：7
			return mContext.getResources().getStringArray(R.array.week_array)[dayOfWeek];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	private InfoBean infoBean;
	public void setInfoBean(InfoBean infoBean){
       this.infoBean = infoBean;
	}

	public boolean isNight() {
		if(infoBean == null) {
			return false;
		}
		//系统时间
		String currenttime = StringUtils.getTimeShort(infoBean.getCurrenttime());
		return isNight(currenttime);
	}

	public boolean isNight(String currenttime) {
		try {
			if(infoBean == null) {
				return false;
			}
			List<InfoBean.Daily> dailys = infoBean.getDaily();
			//系统时间
			if (dailys != null && dailys.size() > 0) {
				InfoBean.Daily daily = dailys.get(0);//格式2018-03-23
				final int curTime = Integer.valueOf(currenttime.replace(":",""));
				final int srTime = Integer.valueOf(daily.getSunrise().replaceAll(":", ""));// 日出时间
				final int ssTime = Integer.valueOf(daily.getSunset().replaceAll(":", ""));// 日落时间
				if (curTime > srTime && curTime <= ssTime) {// 是白天
					return false;
				} else {
					return true;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			showLog(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public static class Area implements Serializable {
		private static final long serialVersionUID = 7646903512215148839L;

		public Area() {
			super();
		}

		@SerializedName("id")
		@Expose
		public String id;
		@SerializedName("weather")
		@Expose
		public String weather;
		@SerializedName("temp")
		@Expose
		public String temp;
		@SerializedName("city")
		@Expose
		public String city;
		@SerializedName("lowHighTemp")
		@Expose
		public String lowHighTemp;
		@SerializedName("imgCode")
		@Expose
		public String imgCode;
		@SerializedName("aqi")
		@Expose
		public String aqi;
		@SerializedName("futureWeatherInfos")
		@Expose
		public List<FutureWeatherInfo> futureWeatherInfos;

		@Override
		public String toString() {
			return temp + " [" + city + "," + lowHighTemp + "]";// + "," + id
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((city == null) ? 0 : city.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((temp == null) ? 0 : temp.hashCode());
			result = prime * result + ((weather == null) ? 0 : weather.hashCode());
			result = prime * result + ((lowHighTemp == null) ? 0 : lowHighTemp.hashCode());
			result = prime * result + ((imgCode == null) ? 0 : imgCode.hashCode());
			result = prime * result + ((aqi == null) ? 0 : aqi.hashCode());
			result = prime * result + ((futureWeatherInfos == null) ? 0 : futureWeatherInfos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Area other = (Area) obj;
			if (city == null) {
				if (other.city != null)
					return false;
			} else if (!city.equals(other.city))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (temp == null) {
				if (other.temp != null)
					return false;
			} else if (!temp.equals(other.temp))
				return false;
			if (weather == null) {
				if (other.weather != null)
					return false;
			} else if (!weather.equals(other.weather))
				return false;
			if (lowHighTemp == null) {
				if (other.lowHighTemp != null)
					return false;
			} else if (!lowHighTemp.equals(other.lowHighTemp))
				return false;
			if (imgCode == null) {
				if (other.imgCode != null)
					return false;
			} else if (!imgCode.equals(other.imgCode))
				return false;
			if (aqi == null) {
				if (other.aqi != null)
					return false;
			} else if (!aqi.equals(other.aqi))
				return false;
			if (futureWeatherInfos == null) {
				if (other.futureWeatherInfos != null)
					return false;
			} else if (!futureWeatherInfos.equals(other.futureWeatherInfos))
				return false;
			return true;
		}
	}

	private void showLog(String msg){
		Log.e(this.getClass().getSimpleName(),msg);
	}
}
