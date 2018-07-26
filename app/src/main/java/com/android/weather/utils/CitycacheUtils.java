package com.android.weather.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.weather.FutureWeatherInfo;
import com.android.weather.InfoBean;
import com.android.weather.WeatherApplication;
import com.android.weather.api.ApiManager;
import com.android.weather.widget.WeatherBaseWidget;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2018/4/19.
 */
public class CitycacheUtils {

    private static CitycacheUtils citycacheUtils;

    public static CitycacheUtils getInstace(){
        if(citycacheUtils == null){
           citycacheUtils = new CitycacheUtils();
        }
        return  citycacheUtils;
    }

    /**
     * sharedpref保存城市
     * @param mInfoBean
     */
    public synchronized void cacheCurrentCity(Context context,final InfoBean mInfoBean){

        ApiManager.Area area = new ApiManager.Area();
        area.city =  mInfoBean.getCity();
        List<InfoBean.Hourly> hourlies = mInfoBean.getHourly();
        if(hourlies!=null&&hourlies.size()>0){
            String temp = hourlies.get(0).getTemp();
            String weather = hourlies.get(0).getWeather();
            if(!TextUtils.isEmpty(temp)&&!TextUtils.isEmpty(weather)){
                area.temp =  temp;
                area.weather = weather;
            }
        }
        area.lowHighTemp = mInfoBean.getTemplow()+"/"+ mInfoBean.getTemphigh();
        area.id = mInfoBean.getCitycode();
        area.imgCode = mInfoBean.getImg();
        area.aqi = mInfoBean.getAqi().getQuality();
        List<FutureWeatherInfo> futureWeatherInfos = new ArrayList<>();
        for(InfoBean.Daily daily : mInfoBean.getDaily()){
            FutureWeatherInfo futureWeatherInfo = new FutureWeatherInfo();
            futureWeatherInfo.setDate(daily.getDate());
            futureWeatherInfo.setImgCode(daily.getDay().getImg());
            futureWeatherInfo.setTempHigh(daily.getDay().getTemphigh());
            futureWeatherInfo.setTempLow(daily.getNight().getTemplow());
            futureWeatherInfos.add(futureWeatherInfo);
        }
        area.futureWeatherInfos =  futureWeatherInfos;
        List<ApiManager.Area> areas = ApiManager.getInstace().loadSelectedArea(WeatherApplication.getInstance());
        int position = isExistArea(areas,area);
        if( position != -1){
            areas.set(position,area);
        }else{
            if(WeatherUtils.isLocationCity(context,area.city)){
                areas.add(0,area);
            }else{
                areas.add(area);
            }
        }
        Gson gson = new Gson();
        String str = gson.toJson(areas);
        showLog(str);
        boolean result = MxxPreferenceUtil.setPrefString(context,MxxPreferenceUtil.KEY_SELECTED_AREA,new Gson().toJson(areas));
        if(result){
            showLog("city area save sucess!");
        }else{
            showLog("city area save fail!");
        }
        cacheCurrentCityFile(mInfoBean);
        //通知小部件更新
        if(context != null){
           Intent intent = new Intent(WeatherBaseWidget.UPDATE_WIDGET_WEATHER);
           context.sendBroadcast(intent);
        }
    }

    /**
     * 文件保存
     * @param mInfoBean
     */
    private synchronized void cacheCurrentCityFile(final InfoBean mInfoBean){

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileManager.saveFile(new Gson().toJson(mInfoBean),mInfoBean.getCitycode());
            }
        }).start();
    }

    /**
     * 根据城市code判断是否已经保存了当前城市的简要信息
     * @param newArea
     * @return
     */
    private synchronized int isExistArea(List<ApiManager.Area> areas, ApiManager.Area newArea){
        for(int position = 0;position < areas.size() ; position++){
            ApiManager.Area curArea = areas.get(position);
            if(TextUtils.equals(curArea.id,newArea.id)){
                return position;
            }
        }
        return -1;
    }

    private void showLog(String msg){
        if(WeatherApplication.DEBUG){
            Log.e("TAG",msg);
        }
    }

}
