package com.android.weather.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.android.weather.R;
import com.android.weather.api.ApiManager;

/**
 * Created by admin on 2018/3/22.
 */
public class WeatherWidget extends WeatherBaseWidget{

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
        boolean result = getWidgetExist(context,WeatherDateWidget.class.getSimpleName());
        if(!result){
            stopService(context);
        }
    }

    /**
     * 更新时间小部件
     * @param context
     */
    protected synchronized void update(Context context) {

        RemoteViews rViews = getRemoteViews(context,R.layout.weather_widget);

        //反转颜色
        int color = slashWallpaperColor(context);
        rViews.setTextColor(R.id.tc_time, color);
        rViews.setTextColor(R.id.tc_date,color);
        rViews.setTextColor(R.id.widget1_weather,color);
        rViews.setTextColor(R.id.tv_week,color);

        rViews.setTextViewText(R.id.tv_week,week(context));

        rViews.setOnClickPendingIntent(R.id.widget1_weather, startWeatherActivity(context,R.id.widget1_weather));
        rViews.setOnClickPendingIntent(R.id.ll_date, startDeskClockActivity(context,R.id.ll_date));
        rViews.setOnClickPendingIntent(R.id.tc_time, startDeskClockActivity(context,R.id.tc_time));

        //更新天气
        ApiManager.Area area = getCurrentArea(context);
        if(area != null) {
            rViews.setTextViewText(R.id.widget1_weather, area.weather+"  "+area.temp+"°");
        }else{
            rViews.setTextViewText(R.id.widget1_weather,context.getResources().getString(R.string.widget_no_data));
        }

        invalidate(context,rViews);
    }

    private void invalidate(Context context,RemoteViews rViews) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context.getApplicationContext());
        ComponentName cName = new ComponentName(context.getApplicationContext(), WeatherWidget.class);
        manager.updateAppWidget(cName, rViews);
    }
}
