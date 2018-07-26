package com.android.weather;

import android.app.Application;

import com.qucii.sdk.CustomSDK;
import com.qucii.sdk.UserSDK;

/**
 * Created by admin on 2018/1/31.
 */
public class WeatherApplication extends Application{

    public static final boolean DEBUG = true;
    public static WeatherApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CustomSDK.initialize(this);
        UserSDK.initialize(this);
    }

    public static WeatherApplication getInstance(){
        return instance;
    }
}
