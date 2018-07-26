package com.android.weather.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.android.weather.InfoBean;
import com.android.weather.MainActivity;
import com.android.weather.api.ApiManager;
import com.android.weather.utils.WeatherUtils;

import java.util.List;

/**
 * 一周天气预报
 * 按文字算高度18行
 * 文字设置为12，高度是216dp
 * @author Mixiaoxiao
 *	
 */
public class DailyForecastView extends View {

	private int width, height;
	private float percent = 0f;
	private final float density;
	//private ArrayList<DailyForecast> forecastList;
	private Path tmpMaxPath = new Path();
	private Path tmpMinPath = new Path();
	private Path cirPath = new Path();
//	private PathMeasure tmpMaxPathMeasure = new PathMeasure(tmpMaxPath, false);
	// private PointF[] points ;
	private Data[] datas;
//	private final float textSize;

	private final TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
	private final TextPaint mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
	private final TextPaint maxPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
	private int strokeWidth = 6;

	public class Data {
		public float minOffsetPercent, maxOffsetPercent;// 差值%
		public int tmp_max, tmp_min;
		public String date;
		public String wind_sc;
		public String cond_txt_d;
	}

	public DailyForecastView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		if(isInEditMode()){
			return ;
		}
		init(context);
		initCircle();
		initMaxLine();
	}
	
	public void resetAnimation(){
		percent = 0f;
		invalidate();
	}

	private void init(Context context) {
		paint.setStrokeWidth(1.2f*density );
		paint.setTextSize(12f * density);
		paint.setStyle(Style.FILL);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(MainActivity.getTypeface(context));
	}

	private void initCircle(){
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStrokeWidth(strokeWidth);
		mPaint.setColor(Color.WHITE);
	}

	private void initMaxLine(){
		maxPaint.setColor(Color.parseColor("#EECB2A"));
		maxPaint.setStrokeWidth(1.2f*density );
		maxPaint.setStyle(Style.FILL);
		maxPaint.setAlpha(255);
	}

	//220dp 18hang
	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		if(isInEditMode()){
			return ;
		}

		paint.setStyle(Style.FILL);
		maxPaint.setStyle(Style.FILL);
		//一共需要 顶部文字2(+图占8行)+底部文字2 + 【间距1 + 日期1 + 间距0.5 +　晴1 + 间距0.5f + 微风1 + 底部边距1f 】 = 18行
		// 12     13       14      14.5    15.5      16      17       18
		final float textSize = this.height / 18f;
		final float textOffset = getTextPaintOffset(paint);
		final float dH = textSize * 8f;
		final float dCenterY = textSize * 6f ;
		if (datas == null || datas.length <= 1) {
			//canvas.drawLine(0, dCenterY, this.width, dCenterY, paint);//没有数据的情况下只画一条线
			return;
		}
		final float dW = this.width * 1f / datas.length;

		tmpMaxPath.reset();
		tmpMinPath.reset();
		cirPath.reset();
		final int length = datas.length;
		float[] x = new float[length];
		float[] yMax = new float[length];
		float[] yMin = new float[length];
		final float textPercent = (percent >= 0.6f) ? ((percent - 0.6f) / 0.4f) : 0f;
		final float pathPercent = (percent >= 0.6f) ? 1f : (percent / 0.6f);
		
		//画底部的三行文字和标注最高最低温度
		paint.setAlpha((int) (255 * textPercent));
		paint.setColor(Color.WHITE);

		for (int i = 0; i < length; i++) {
			final Data d = datas[i];
			x[i] = i * dW + dW / 2f;
			yMax[i] = dCenterY - d.maxOffsetPercent * dH;
			yMin[i] = dCenterY - d.minOffsetPercent * dH;

			canvas.drawText(d.tmp_max + "°", x[i], yMax[i] - textSize + textOffset, paint);// - textSize
			canvas.drawText(d.tmp_min + "°", x[i], yMin[i] + textSize  + textOffset, paint);
			canvas.drawText(ApiManager.getInstace().prettyDate(d.date), x[i], textSize * 13.5f + textOffset, paint);//日期d.date.substring(5)
            int drawableId = WeatherUtils.getCondIconDrawableId(Integer.valueOf(d.cond_txt_d),null);
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
			canvas.drawBitmap(bitmap, x[i]-40, textSize * 14.5f + textOffset,paint);
			bitmap.recycle();
		}

		paint.setAlpha(255);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.parseColor("#87CDDC"));

		maxPaint.setAlpha(255);
		maxPaint.setStyle(Style.STROKE);

		for (int i = 0; i < length ; i++) {
			if (i == 0) {
				tmpMaxPath.moveTo(x[i] - 1, yMax[i]);
				tmpMinPath.moveTo(x[i]-1, yMin[i]);
			}
			tmpMaxPath.lineTo(x[i], yMax[i]);
			tmpMinPath.lineTo(x[i], yMin[i]);
			cirPath.addCircle(x[i], yMax[i],strokeWidth, Path.Direction.CCW);
			cirPath.addCircle(x[i], yMin[i],strokeWidth, Path.Direction.CCW);
		}

		final boolean needClip = pathPercent < 1f;
		if(needClip){
			canvas.save();
			canvas.clipRect( 0 , 0, this.width * pathPercent, this.height);
		}
		canvas.drawPath(tmpMaxPath, maxPaint);
		canvas.drawPath(tmpMinPath, paint);
		canvas.drawPath(cirPath,mPaint);

		if(percent < 1){
			percent += 1f;// 0.025f;
			percent = Math.min(percent, 1f);
			ViewCompat.postInvalidateOnAnimation(this);
		}

		if(needClip){
			canvas.restore();
		}
	}

	public void setData(List<InfoBean.Daily> dailies) {

		datas = new Data[7];
		try {
			int all_max = Integer.MIN_VALUE;
			int all_min = Integer.MAX_VALUE;
			for (int i = 0; i < 7; i++) {
				InfoBean.Daily daily = dailies.get(i);
				int max = Integer.valueOf(daily.getDay().getTemphigh());
				int min = Integer.valueOf(daily.getNight().getTemplow());
				if (all_max < max) {
					all_max = max;
				}
				if (all_min > min) {
					all_min = min;
				}
				final Data data = new Data();
				data.tmp_max = max;
				data.tmp_min = min;
				data.date = daily.getDate();
				if(ApiManager.getInstace().isNight()){
					data.wind_sc = daily.getNight().getWinddirect();
					data.cond_txt_d = daily.getNight().getImg();
				}else{
					data.wind_sc = daily.getDay().getWinddirect();
					data.cond_txt_d = daily.getDay().getImg();
				}
				datas[i] = data;
			}
			float all_distance = Math.abs(all_max - all_min);
			float average_distance = (all_max + all_min) / 2f;
			for (Data d : datas) {
				d.maxOffsetPercent = (d.tmp_max - average_distance) / all_distance;
				d.minOffsetPercent = (d.tmp_min - average_distance) / all_distance;
			}
			resetAnimation();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width = w;
		this.height = h;
	}

	public static float getTextPaintOffset(Paint paint) {
		FontMetrics fontMetrics = paint.getFontMetrics();
		return -(fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.top;
	}

}
