package com.android.weather.utils;

import android.content.Context;
import android.text.TextUtils;

import com.android.weather.LocationInfo;
import com.android.weather.R;
import com.android.weather.api.ApiManager;
import com.google.gson.Gson;

/**
 * Created by admin on 2018/3/31.
 */
public class WeatherUtils {

    private  static String PACKAGENAME = "com.android.weather";
    private  static String CLASSNAME = "drawable";
    /**
     * widget未来三天的天气小图标
     * @param code
     * @return
     */
    public static int getWidgetIconDrawableId(String code){
        if(TextUtils.equals(code,"301")){
            code = "7";
        }
        if(TextUtils.equals(code,"302")){
            code ="14";
        }
        return getCompentID(CLASSNAME,"widget_"+code);
    }

    /**
     * widget顶部大图标
     * @param code
     * @return
     */
    public static int getWidgetBigIconDrawableId(int code){
        boolean isNight = isNight = ApiManager.getInstace().isNight();
        int resid = 0;
        String resStr = "widget_big_";
        if(code == 301){
           code = 7;
        }
        if(code == 302){
            code =14;
        }
        if((code == 0 || code ==1) && isNight){
           resStr = "widget_big_night_";
        }
        return getCompentID(CLASSNAME,resStr+code);
    }

    /**
     * 反射得到组件的id号
     * @param className layout,string,drawable,style,id,color,array
     * @param idName    唯一文件名
     * @return  资源id
     */
    public static int getCompentID(String className, String idName) {
        int id = 0;
        try {
            Class<?> cls = Class.forName(PACKAGENAME + ".R$" + className);
            id = cls.getField(idName).getInt(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * app内部使用的图片
     * @param w
     * @param time
     * @return
     */
    public static int getCondIconDrawableId(int w,String time) {

        boolean isNight;

        if(TextUtils.isEmpty(time)){
            isNight = ApiManager.getInstace().isNight();
        }else{
            isNight = ApiManager.getInstace().isNight(time);
        }
        try {
            //final int w = Integer.valueOf(weather.get().now.cond.code);
            switch (w) {
                case 0://晴
                    return isNight ? R.drawable.ic_weather_home_sunny_night : R.drawable.ic_weather_home_sunny;
                case 1:// 多云
                    return isNight ? R.drawable.ic_weather_home_mostlycloudy_night : R.drawable.ic_weather_home_mostlycloudy;
                case 2:// 阴
                    return isNight ? R.drawable.ic_weather_home_cloudy_night : R.drawable.ic_weather_home_cloudy;
                case 3:// 阵雨
                    return R.drawable.ic_weather_home_shower;
                case 4:// 雷阵雨 Thundershower
                case 5:// 强雷阵雨 Heavy Thunderstorm
                    return R.drawable.ic_weather_home_thunderstorms;
                case 7:// 小雨
                case 301:
                case 21:
                    return R.drawable.ic_weather_home_lightrain;
                case 8:// 中雨 Moderate Rain
                case 22:
                    return R.drawable.ic_weather_home_moderaterain;
                case 9:// 大雨 Heavy Rain
                case 10://暴雨
                case 11://大暴雨
                case 12://特大暴雨
                case 23:// 大雨-暴雨
                case 24://暴雨-大暴雨
                case 25://大暴雨-特大暴雨
                    return R.drawable.ic_weather_home_heavyrain;
                case 13:// 阵雪
                case 302:
                    return R.drawable.ic_weather_home_lightsnow;
                case 14:// 小雪
                case 26:
                    return R.drawable.ic_weather_home_lightsnow;
                case 15:// 中雪
                case 27:
                    return R.drawable.ic_weather_home_heavysnow;
                case 16:// 大雪
                case 28:
                    return R.drawable.ic_weather_home_moderatesnow;
                case 17:
                    return R.drawable.ic_weather_home_snowstorm;
                case 18:// 雾
                case 57:
                case 58:
                case 32:
                case 49:
                    return R.drawable.ic_weather_home_fog;
                case 19:// 冻雨 Freezing Rain
                    return R.drawable.ic_weather_home_rain;
                case 20:// 沙尘暴
                    return R.drawable.ic_weather_home_severestorm;
                case 29://浮尘
                case 30:
                    return R.drawable.ic_weather_home_sand_devil;
                case 53:
                case 54:
                case 55:
                case 56:
                    return R.drawable.ic_weather_home_haze;
                default:
                    return R.drawable.ic_weather_home_unknown;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.drawable.ic_weather_home_unknown;
    }

    /**
     * 判断是否是定位城市
     * @param context
     * @param currentCity
     * @return
     */
    public static boolean isLocationCity(Context context,String currentCity){

        String jsonStr = MxxPreferenceUtil.getPrefString(context,MxxPreferenceUtil.LOCATION_CITY,null);
        LocationInfo locationInfo = new Gson().fromJson(jsonStr,LocationInfo.class);
        if(locationInfo != null){
            String city = locationInfo.getCity();
            String district = locationInfo.getDistrict();
            if(currentCity.contains(city)||currentCity.contains(district)
                    ||city.contains(currentCity)||district.contains(currentCity)){
                return true;
            }
        }
        return false;
    }
}
