package com.android.weather.dynamicweather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AnimationUtils;

import com.android.weather.dynamicweather.BaseDrawer.Type;

public class DynamicWeatherView extends SurfaceView implements SurfaceHolder.Callback {

    static final String TAG = DynamicWeatherView.class.getSimpleName();
    private DrawThread mDrawThread;
    private String str = getString(android.R.string.cancel);

    public DynamicWeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private BaseDrawer preDrawer, curDrawer;
    private float curDrawerAlpha = 0f;
    private Type curType = Type.DEFAULT;
    private int mWidth, mHeight;

    private void init(Context context) {
        curDrawerAlpha = 0f;
        mDrawThread = new DrawThread();
        final SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    private void setDrawer(BaseDrawer baseDrawer) {
        if (baseDrawer == null) {
            return;
        }
        curDrawerAlpha = 0f;
        if (this.curDrawer != null) {
            this.preDrawer = curDrawer;
        }
        this.curDrawer = baseDrawer;
        // updateDrawerSize(getWidth(), getHeight());
        // invalidate();
    }

    public void setDrawerType(Type type) {
        if (type == null) {
            return;
        }
        // UiUtil.toastDebug(getContext(), "setDrawerType->" + type.name());
        if (type != curType) {
            curType = type;
            setDrawer(BaseDrawer.makeDrawerByType(getContext(), curType));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // updateDrawerSize(w, h);
        mWidth = w;
        mHeight = h;
    }

    public void onResume(){
        synchronized (mDrawThread){
            if(!mDrawThread.getRunning()){
                mDrawThread.setRunning(true);
                mDrawThread.start();
            }
        }
    }

    public void onPause(){
        synchronized (mDrawThread) {
            mDrawThread.setRunning(false);
        }
    }

    private boolean drawSurface(Canvas canvas) {
        final int w = mWidth;
        final int h = mHeight;
        if (w == 0 || h == 0) {
            return true;
        }
        boolean needDrawNextFrame = false;
        if (curDrawer != null) {
            curDrawer.setSize(w, h);
            needDrawNextFrame = curDrawer.draw(canvas, curDrawerAlpha);
        }
        if (preDrawer != null && curDrawerAlpha < 1f) {
            needDrawNextFrame = true;
            preDrawer.setSize(w, h);
            preDrawer.draw(canvas, 1f - curDrawerAlpha);
        }
        if (curDrawerAlpha < 1f) {
            curDrawerAlpha += 0.04f;
            if (curDrawerAlpha > 1) {
                curDrawerAlpha = 1f;
                preDrawer = null;
            }
        }
        return needDrawNextFrame;
    }

    private String getString(int id) {
        return "";
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Tell the drawing thread that a surface is available.
        synchronized (mDrawThread) {
            mDrawThread.mSurface = holder;
            mDrawThread.setRunning(true);
            mDrawThread.start();
        }
        showLog("surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // We need to tell the drawing thread to stop, and block until
        // it has done so.
        synchronized (mDrawThread) {
            mDrawThread.mSurface = holder;
            mDrawThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    mDrawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }
        holder.removeCallback(this);
        showLog("surfaceDestroyed");
    }

    private class DrawThread extends Thread {

        public SurfaceHolder mSurface;
        private boolean mRunning = true;

        public void setRunning(boolean mRunning){
            this.mRunning = mRunning;
        }

        public boolean  getRunning(){
            return mRunning;
        }

        @Override
        public void run() {

            while (mRunning) {
                if (mSurface != null) {
                    synchronized (mSurface) {
                        final long startTime = AnimationUtils.currentAnimationTimeMillis();
                        Canvas canvas = mSurface.lockCanvas();
                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            drawSurface(canvas);
                            mSurface.unlockCanvasAndPost(canvas);
                        } else {
                            showLog("Failure locking canvas");
                        }
                        final long drawTime = AnimationUtils.currentAnimationTimeMillis() - startTime;
                        final long needSleepTime = 16 - drawTime;
                        //showLog("drawSurface drawTime->" + drawTime + " needSleepTime->" + Math.max(0, needSleepTime));// needSleepTime);
                        if (needSleepTime > 0) {
                            try {
                                Thread.sleep(needSleepTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }
    }

    private void showLog(String msg){
        Log.e(TAG,msg);
    }
}
