package com.android.weather.utils;

/**
 * author zaaach on 2016/1/26.
 */
public class StringUtils {
    /**
     * 提取出城市或者县
     * @param city
     * @param district
     * @return
     */
//    public static String extractLocation(final String city, final String district){
//        return district.contains("县") ? district.substring(0, district.length() - 1) : city.substring(0, city.length() - 1);
//    }

    /**
     * 获取时间 小时:分 HH:mm
     * @return
     */
    public  static String getTimeShort(String currentTime) {
        String dates[] = currentTime.split(" ")[1].split(":");
        String dateString = dates[0]+":"+dates[1];
        return dateString;
    }
}
