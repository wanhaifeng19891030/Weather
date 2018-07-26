package com.android.weather.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.weather.MainActivity;

public class FontTextView extends TextView{

	public FontTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(isInEditMode()){
			return ;
		}
//		setIncludeFontPadding(false);
		setTypeface(MainActivity.getTypeface(context));
	}

}
