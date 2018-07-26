package com.android.weather.utils;

import android.content.Context;
import android.text.TextUtils;

import com.android.weather.R;

import java.util.regex.Pattern;

/**
 * author zaaach on 2016/1/28.
 */
public class PinyinUtils {
    /**
     * 获取拼音的首字母（大写）
     * @param pinyin
     * @return
     */
    public static String getFirstLetter(Context mContext,final String pinyin){
        if (TextUtils.isEmpty(pinyin)) return mContext.getResources().getString(R.string.location);
        String c = pinyin.substring(0, 1);
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c).matches()){
            return c.toUpperCase();
        } else if ("0".equals(c)){
            return mContext.getResources().getString(R.string.location);
        } else if ("1".equals(c)){
            return mContext.getResources().getString(R.string.hot);
        }
        return mContext.getResources().getString(R.string.location);
    }
}
