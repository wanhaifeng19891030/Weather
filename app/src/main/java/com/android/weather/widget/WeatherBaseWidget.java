package com.android.weather.widget;

import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.weather.MainActivity;
import com.android.weather.R;
import com.android.weather.WeatherApplication;
import com.android.weather.api.ApiManager;
import com.android.weather.utils.MxxPreferenceUtil;
import com.android.weather.utils.WeatherUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by admin on 2018/4/2.
 */
public class WeatherBaseWidget extends AppWidgetProvider{
    ////监听壁纸广播action
    public static final String UPDATE_WIDGET_WEATHER = "android.intent.action.WEATHER_CHANGED";
    private static final String WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED";
    private static final String FIRST_INIT = "android.intent.action.qucii.firstInit";
    public static final int THUMBNAIL_WIDTH = 2;//壁纸缩略图宽度
    //时钟包名和类名用于应用间跳转
    protected final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    protected final String DESKCLOCK_CLASSNAME = ".DeskClock";
    protected final String SELECT_TAB_INTENT_EXTRA = "deskclock.select.tab";
    protected final int size = 14;

    //当 widget 更新时被执行。
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        showLog("action:onUpdate");
        update(context);
    }

    //当 widget 被删除时被触发。
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    //当widget尺寸发生改变
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        showLog("action:onAppWidgetOptionsChanged");
        update(context);
    }

    // 接收到任意广播时触发，并且会在上述的方法之前被调用。
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        showLog("action:"+action);
        update(context);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        showLog("action:onRestored");
    }

    /**
     * 更新时间小部件
     * @param context
     */
    protected void update(Context context){ }

    protected void startService(Context context){
        context.startService(new Intent(context,RefreshWeatherService.class));
    }

    protected void stopService(Context context){
        context.stopService(new Intent(context,RefreshWeatherService.class));
    }

    protected PendingIntent startWeatherActivity(Context context,int requestCode){
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pi;
    }

    protected PendingIntent startDeskClockActivity(Context context,int requestCode) {
        Intent intent = new Intent();
        intent.setClassName(DESKCLOCK_PACKAGENAME, DESKCLOCK_PACKAGENAME+DESKCLOCK_CLASSNAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SELECT_TAB_INTENT_EXTRA,1);
        PendingIntent pi = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pi;
    }

    protected boolean setWidgetExist(Context context,String type,boolean value){
        return  MxxPreferenceUtil.setPrefBoolean(context,type,value);
    }

    protected boolean getWidgetExist(Context context,String type){
        boolean isExist = MxxPreferenceUtil.getPrefBoolean(context,type,false);
        return isExist;
    }

    protected int getViewId(String idName){
        return WeatherUtils.getCompentID("id",idName);
    }

    /**
     * 判断是否存在定位的地址，存在返回对象
     * @param context
     * @return
     */
    protected ApiManager.Area getCurrentArea(Context context){
        ArrayList<ApiManager.Area> areas = ApiManager.getInstace().loadSelectedArea(context);
        if(areas.isEmpty()){
            return null;
        }else{
            return areas.get(0);
        }
    }

    /**
     * 获取小组件对象
     * @param context
     * @return
     */
    protected RemoteViews getRemoteViews(Context context,int layoutId){
        return new RemoteViews(context.getPackageName(), layoutId);
    }

    /**
     *
     * @Title: slashWallpaperColor
     * @Description: 获取wallpaper中的颜色
     * 	取色步骤：
     * 	1、将壁纸按比例缩放成2*3左右的大小
     *  2、将缩略图作高斯模糊处理
     * 	2、取模糊后的图中所有像素点的颜色值，如果有一半以上的像素点的颜色值满足指定条件，则文字反转成黑色;
     * 	  并且如果至少有一个颜色值为纯白，压黑桌面

     * @author: y.wan
     * @date:  2018年4月19日
     * @param: @param context
     * @return: void
     * @throws
     */
    protected int slashWallpaperColor(final Context context){
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        BitmapDrawable drawable = (BitmapDrawable) wallpaperManager.getDrawable();
        if(drawable == null){
            return Color.WHITE;
        }
        Bitmap src = drawable.getBitmap();
        if(src == null || src.getWidth() <= 0 || src.getHeight() <= 0){
            return Color.WHITE;
        }
        int targetWidth = THUMBNAIL_WIDTH;
        int targetHight = targetWidth * src.getHeight()/src.getWidth();
        //压缩
        if(src.isRecycled()){
            return Color.WHITE;
        }
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(src, targetWidth, targetHight, true);
        //高斯模糊处理
        Bitmap bluredBitmap = blurBitmap(context,  scaleBitmap);

        int picw = bluredBitmap.getWidth();
        int pich = bluredBitmap.getHeight();
        int[] pix = new int[picw * pich];
        bluredBitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);
        int matchCount = 0;//满足条件的像素点数量
        for(int i = 0; i < pix.length; i ++){
            if(matches(pix[i])){
                matchCount ++;
            }
        }
        if(matchCount >= pix.length/2){
           return Color.parseColor("#FF464C56");
        }
        if(!scaleBitmap.isRecycled()){
            scaleBitmap.recycle();
        }
        if(!bluredBitmap.isRecycled()){
            bluredBitmap.recycle();
        }
        return Color.WHITE;
    }

    /**
     *
     * @Title: blurBitmap
     * @Description: 高斯模糊

     * @author: y.wan
     * @date:  2018年4月19日
     * @param: @param context
     * @param: @param bitmap
     * @param: @return
     * @return: Bitmap
     * @throws
     */
    public Bitmap blurBitmap(Context context, Bitmap bitmap){

        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //https://issuetracker.google.com/issues/37117262
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            try {
                Class<?> javaRSDir = Class.forName("android.renderscript.RenderScriptCacheDir");
                Method setCacheDir = javaRSDir.getMethod("setupDiskCache", File.class);
                setCacheDir.invoke(null, context.getCodeCacheDir());
            } catch (Exception e) {
            }
        }
        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(context);

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur: 0 < radius <= 25
        blurScript.setRadius(5.0f);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();
        rs.destroy();
        blurScript.destroy();
        allIn.destroy();
        allOut.destroy();

        return outBitmap;

    }

    /**
     *
     * @Title: matches
     * @Description: 判断颜色值是否满足下列条件之一
     * 1：S：0-30   B：70-100
     * 2：H:70-60   S:0-100   B:80-100

     * @author: y.wan
     * @date:  2018年4月19日
     * @param: @param color
     * @param: @return
     * @return: Boolean
     * @throws
     */
    private boolean matches(int color){
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color),
                hsv);

        return (hsv[1] <= 0.2f && hsv[2] >= 0.8f) ||
                (hsv[0] >= 60 && hsv[0] <= 70 && hsv[2] >= 0.8f);
    }

    /**
     * 获取星期
     * @return
     */
    protected String week(Context context){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int mWay = c.get(Calendar.DAY_OF_WEEK);
        return context.getResources().getStringArray(R.array.week_array)[mWay-1];
    }

    protected void showLog(String msg){
        if(WeatherApplication.DEBUG){
            Log.e(getClass().getSimpleName(),msg);
        }
    }
}
