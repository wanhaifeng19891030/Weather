package com.android.weather.widget;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.weather.InfoBean;
import com.android.weather.api.ApiManager;
import com.android.weather.utils.CitycacheUtils;
import com.android.weather.utils.HttpUtils;
import com.qucii.sdk.http.ICallback;
import com.qucii.sdk.transfer.Resp;

import java.util.List;

/**
 * Created by admin on 2018/5/17.
 */
public class RefreshWeatherService extends Service {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        registerTimeReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterTimeReceiver();
    }

    private  void registerTimeReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }

    private void unregisterTimeReceiver(){
        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
           if(TextUtils.equals(action,Intent.ACTION_SCREEN_ON)){
               updateThreeHour(context);
           }
        }
    };

    private void updateThreeHour(Context context){
        this.context = context;
        List<ApiManager.Area> areas =  ApiManager.getInstace().loadSelectedArea(context);
        for(ApiManager.Area area : areas){
            HttpUtils.getInstance().get(area.city,callback);
        }
    }

    private ICallback<InfoBean> callback = new ICallback<InfoBean>() {
        @Override
        public void onSuccess(Resp<InfoBean> resp) {
            if(TextUtils.equals(resp.desc,"ok")){
                CitycacheUtils.getInstace().cacheCurrentCity(context,resp.data);
            }
        }

        @Override
        public void onFailure(int i, String s) {
            showLog("onFailure:"+s);
        }
    };

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
    }
}
