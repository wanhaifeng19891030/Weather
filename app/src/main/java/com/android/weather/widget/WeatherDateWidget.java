package com.android.weather.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.android.weather.FutureWeatherInfo;
import com.android.weather.R;
import com.android.weather.api.ApiManager;
import com.android.weather.utils.WeatherUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Created by admin on 2018/3/22.
 */
public class WeatherDateWidget extends WeatherBaseWidget{

    //未来三天天气
    private final int NEXT_THREE_DAYS_WEATHER = 4;

    // 当第1个 widget 的实例被创建时触发。也就是说，如果用户对同一个 widget 增加了两次（两个实例），那么onEnabled()只会在第一次增加widget时触发。
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        showLog("action:onEnabled");
        update(context);
        setWidgetExist(context,getClass().getSimpleName(),true);
        startService(context);
    }

    // 当最后1个 widget 的实例被删除时触发。
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        showLog("action:onDisabled");
        setWidgetExist(context,getClass().getSimpleName(),false);
        boolean result = getWidgetExist(context,WeatherWidget.class.getSimpleName());
        if(!result){
           stopService(context);
        }
    }

    /**
     * 更新时间小部件
     * @param context
     */
    protected  synchronized  void update(Context context){

        RemoteViews rViews = getRemoteViews(context,R.layout.weather_date_widget);

        rViews.setOnClickPendingIntent(R.id.rl_widget_weather_layout, startWeatherActivity(context,R.id.rl_widget_weather_layout));
        rViews.setOnClickPendingIntent( R.id.textclock, startDeskClockActivity(context, R.id.textclock));
        rViews.setOnClickPendingIntent( R.id.widget_week, startDeskClockActivity(context, R.id.widget_week));

        rViews.setTextViewText(R.id.widget_week,week(context));

        //天气
        ApiManager.Area area = getCurrentArea(context);
        if(area != null) {
            //天气图标
            int imgCode = Integer.parseInt(area.imgCode);
            rViews.setImageViewResource(R.id.widget_weather_icon, WeatherUtils.getWidgetBigIconDrawableId(imgCode));
            //天气情况和空气质量
            rViews.setTextViewText(R.id.widget_weather_aqi, area.weather + " | " + context.getResources().getString(R.string.aqi1) + area.aqi);
            //日期追加字符
            rViews.setTextViewText(R.id.widget_city, area.city);
            //温度
            rViews.setTextViewText(R.id.widget_temp, area.temp + "°");
            //未来三天的天气
            List<FutureWeatherInfo> futureWeatherInfos = area.futureWeatherInfos;
            if (futureWeatherInfos != null) {
                for (int i = 0; i < NEXT_THREE_DAYS_WEATHER; i++) {
                    FutureWeatherInfo futureWeatherInfo = futureWeatherInfos.get(i);
                    String date = formatDate(context, futureWeatherInfo.getDate());
                    rViews.setTextViewText(getViewId("widget_future_day" + i), date);
                    String code = futureWeatherInfo.getImgCode();
                    rViews.setImageViewResource(getViewId("widget_future_icon" + i), WeatherUtils.getWidgetIconDrawableId(code));
                    String lowTemp = futureWeatherInfo.getTempLow();
                    String highTemp = futureWeatherInfo.getTempHigh();
                    rViews.setTextViewText(getViewId("widget_future_lowHighTemp" + i), lowTemp + "°/" + highTemp + "°");
                }
            }
        }else{
                rViews.setTextViewText(R.id.widget_city,context.getResources().getString(R.string.placeholder));
                rViews.setImageViewResource(R.id.widget_weather_icon, R.drawable.widget_big_1);
                rViews.setTextViewText(R.id.widget_weather_aqi, context.getResources().getString(R.string.N_A));
                rViews.setTextViewText(R.id.widget_temp, context.getResources().getString(R.string.placeholder4));
                for(int i = 0 ;i < NEXT_THREE_DAYS_WEATHER ; i++){
                    rViews.setTextViewText(getViewId("widget_future_day"+i),context.getResources().getString(R.string.N_A));
                    rViews.setImageViewResource(getViewId("widget_future_icon"+i),R.drawable.widget_1);
                    String lowTemp = context.getResources().getString(R.string.placeholder2);
                    String highTemp = context.getResources().getString(R.string.placeholder2);
                    rViews.setTextViewText(getViewId("widget_future_lowHighTemp"+i),lowTemp+"°/"+highTemp+"°");
                }
        }
        invalidate(context,rViews);
    }

    /**
     *更新界面
     */
    private void invalidate(Context context,RemoteViews rViews) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName cName = new ComponentName(context.getApplicationContext(), WeatherDateWidget.class);
        manager.updateAppWidget(cName, rViews);
    }

    /**
     * 格式化时间
     * @param date
     * @return
     */
    private String formatDate(Context context,String date){
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
                return context.getResources().getString(R.string.today);
            } else if ((curDay + 1) == day) {
                return context.getResources().getString(R.string.tomorrow);
            }
        }
        return month+"/"+day;
    }
}
