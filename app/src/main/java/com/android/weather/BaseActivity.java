package com.android.weather;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;



public  class BaseActivity extends Activity {

	private final String TAG = BaseActivity.this.getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	public void showLog(String msg) {
		if(WeatherApplication.DEBUG){
			Log.e(TAG, msg);
		}
	}

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
