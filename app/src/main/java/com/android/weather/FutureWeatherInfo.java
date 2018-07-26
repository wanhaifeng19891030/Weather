package com.android.weather;

import java.io.Serializable;

/**
 * Created by admin on 2018/3/31.
 */
public class FutureWeatherInfo implements Serializable{
    private String date;
    private String imgCode;
    private String tempHigh;
    private String tempLow;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgCode() {
        return imgCode;
    }

    public void setImgCode(String imgCode) {
        this.imgCode = imgCode;
    }

    public String getTempHigh() {
        return tempHigh;
    }

    public void setTempHigh(String tempHigh) {
        this.tempHigh = tempHigh;
    }

    public String getTempLow() {
        return tempLow;
    }

    public void setTempLow(String tempLow) {
        this.tempLow = tempLow;
    }
}
