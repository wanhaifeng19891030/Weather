package com.android.weather.utils;

import android.util.Log;

import com.android.weather.InfoBean;
import com.android.weather.QuciiInterface;
import com.qucii.sdk.CustomSDK;
import com.qucii.sdk.http.ICallback;
import com.qucii.sdk.transfer.Resp;

import retrofit2.Call;

/**
 * Created by admin on 2018/4/12.
 */
public class HttpUtils{

    private final String URL = "http://api-user.qucii.com";
    private static HttpUtils httpUtils;
    private QuciiInterface httpService;
    private Call<Resp<InfoBean>> respCall;

    public static HttpUtils getInstance(){
        if(httpUtils == null){
            synchronized (HttpUtils.class){
                if(httpUtils == null){
                    httpUtils = new HttpUtils();
                }
            }
        }
        return httpUtils;
    }

    /**
     * 带参数的get请求
     * @param areaId
     * @param iCallback
     */
    public void get(String areaId,ICallback iCallback){
        httpService = CustomSDK.instance().createHttpService(QuciiInterface.class, URL);
        Log.e("TAG","current city:"+areaId);
        respCall = httpService.getWearther(areaId);
        CustomSDK.instance().request(respCall,iCallback);
    }

}
