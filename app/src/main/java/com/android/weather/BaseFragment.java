package com.android.weather;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.android.weather.dynamicweather.BaseDrawer;

public abstract class BaseFragment extends Fragment {

	private final String TAG = BaseFragment.class.getSimpleName();

	public abstract String getTitle();

	public abstract void onSelected();

	public abstract BaseDrawer.Type getDrawerType();

	protected void notifyActivityUpdate() {
		if (getUserVisibleHint()) {
			Activity activity = getActivity();
			if (activity != null) {
				((MainActivity) activity).updateCurDrawerType();
			} else {
			}
		}
	}

	protected void toast(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}

	protected void showLog(String msg){
		if(WeatherApplication.DEBUG){
			Log.e(TAG, msg);
		}
	}
}
