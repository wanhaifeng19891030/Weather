package com.android.weather;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.weather.api.ApiManager;
import com.android.weather.api.ApiManager.Area;
import com.android.weather.dynamicweather.BaseDrawer;
import com.android.weather.dynamicweather.DynamicWeatherView;
import com.android.weather.utils.UiUtil;
import com.android.weather.utils.WeatherUtils;
import com.android.weather.widget.CommomDialog;
import com.android.weather.widget.MxxFragmentPagerAdapter;
import com.android.weather.widget.MxxViewPager;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    public static Typeface typeface;
    public static Typeface getTypeface(Context context) {
        return typeface;
    }

    public static final String ACTION_CITY_UPDATE = "android.intent.action.CITY_UPDATE";
    private DynamicWeatherView weatherView;
    private MxxViewPager viewPager;
    private ArrayList<WeatherFragment> fragments;
    private ArrayList<Area> selectedAreas;
    private CityBoradcastReceiver cityBoradcastReceiver;
    private final int SDK_PERMISSION_REQUEST = 127;
    private CommomDialog commomDialog;
    private int item;

    @Override
    protected void onNewIntent(Intent data) {
        super.onNewIntent(data);
        String city = data.getStringExtra("city");
        if(!TextUtils.isEmpty(city)) {
            int index = isSaveCity(city);
            if (index == -1) {
                Area area = new Area();
                area.city = city;
                if(selectedAreas != null) {
                    if(WeatherUtils.isLocationCity(this,city)){
                        selectedAreas.add(item = 0,area);
                        initView();
                    }else{
                        selectedAreas.add(area);
                        item = selectedAreas.size();
                        initView();
                    }
                }
            } else {
                viewPager.setCurrentItem(index, false);
            }
        }else{
            if(selectedAreas != null&&selectedAreas.size() <= 0){
                citySelect();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean result = requestPersimmions();
        if(result){
            startInitView();
        }
    }

    /***
     * 启动初始化
     */
    private void startInitView(){
        selectedAreas = ApiManager.getInstace().loadSelectedArea(this);
        fragments = new ArrayList<>();
        if (selectedAreas.size() <= 0) {
            citySelect();
        } else {
            item = 0;
            initView();
        }
        cityRegisterReceiver();
    }

    private boolean requestPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            // 读取电话状态权限
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(isRequestPermissionsSucess(grantResults)){
            startInitView();
        }else{
            showPermissionsDialog();
        }
    }

    /**
     * 是否全部授权成功
     * @param grantResults
     * @return
     */
    private boolean isRequestPermissionsSucess(int[] grantResults){
        for(int i : grantResults){
            if(i == -1){
                return false;
            }
        }
        return true;
    }

    /**
     * 权限被禁止后用户去设置中设置权限
     */
    private void showPermissionsDialog(){
        String content = getResources().getString(R.string.percontent);
        String title = getResources().getString(R.string.title);
        commomDialog = new CommomDialog(this, R.style.dialog, content , new CommomDialog.OnCloseListener() {
            @Override
            public void onClick(Dialog dialog, boolean confirm) {
                if(confirm){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                }
                commomDialog.dismiss();
                AppManager.getAppManager().finishAllActivity();
            }
        });
        commomDialog.setTitle(title);
        commomDialog.show();
    }

    private void initView() {
        try {
            destroySetting();
            setContentView(R.layout.activity_main);
            viewPager = (MxxViewPager) findViewById(R.id.main_viewpager);
            weatherView = (DynamicWeatherView) findViewById(R.id.main_dynamicweatherview);

            if (Build.VERSION.SDK_INT >= 19) {
                viewPager.setPadding(0, UiUtil.getStatusBarHeight(), 0, 0);
            }
            weatherView.setDrawerType(BaseDrawer.Type.UNKNOWN_D);
            for (Area area : selectedAreas) {
                fragments.add(WeatherFragment.makeInstance(area));
            }
            viewPager.setOffscreenPageLimit(fragments.size());
            viewPager.setAdapter(new SimpleFragmentPagerAdapter(getFragmentManager(), fragments));
            viewPager.setOnPageChangeListener(onPageChangeListener);
            viewPager.setCurrentItem(item, false);
        } catch (Exception e) {
            showLog("Exception" + e.getMessage());
        }
    }

    private MxxViewPager.OnPageChangeListener onPageChangeListener = new MxxViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            SimpleFragmentPagerAdapter adapter = ((SimpleFragmentPagerAdapter) viewPager.getAdapter());
            weatherView.setDrawerType(adapter.getItem(position).getDrawerType());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };

    private void destroySetting(){
        viewPager = null;
        weatherView = null;
        fragments.clear();
    }

    private void cityRegisterReceiver() {
        cityBoradcastReceiver = new CityBoradcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CITY_UPDATE);
        registerReceiver(cityBoradcastReceiver, intentFilter);
    }

    private void unCityRegisterReceiver() {
        if (cityBoradcastReceiver != null) {
            unregisterReceiver(cityBoradcastReceiver);
        }
    }

    public class CityBoradcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            selectedAreas = ApiManager.getInstace().loadSelectedArea(MainActivity.this);
            item = 0;
            initView();
        }
    }

    /**
     * 城市管理
     * @param v
     */
    public void cityManager(View v) {
        Intent intent = new Intent(this, CityManagerActivity.class);
        startActivityForResult(intent, 1);
    }

    /**
     * 城市选择
     */
    private void citySelect(){
        Intent intent = new Intent(this, CityPickerActivity.class);
        startActivityForResult(intent, 1);
    }

    /**
     * 是否保存了城市
     * @param city
     * @return
     */
    private int isSaveCity(String city) {
        if (selectedAreas.size() <= 0) {
            return -1;
        }
        int index = 0;
        for(Area area : selectedAreas){
            if (area.city.contains(city)||city.contains(area.city)) {
                return index;
            }
            index ++ ;
        }
        return -1;
    }

    /**
     * 实现更新背景
     */
    public void updateCurDrawerType() {
        weatherView.setDrawerType(((SimpleFragmentPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem()).getDrawerType());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(weatherView != null){
           weatherView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(weatherView != null){
            weatherView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unCityRegisterReceiver();
    }

    public static class SimpleFragmentPagerAdapter extends MxxFragmentPagerAdapter {

        private ArrayList<WeatherFragment> fragments;

        public SimpleFragmentPagerAdapter(FragmentManager fragmentManager, ArrayList<WeatherFragment> fragments) {
            super(fragmentManager);
            this.fragments = fragments;

        }

        @Override
        public BaseFragment getItem(int position) {
            BaseFragment fragment = fragments.get(position);
            fragment.setRetainInstance(true);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((Fragment) object).getView());
            super.destroyItem(container, position, object);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
