package com.android.weather;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.weather.adapter.CityListAdapter;
import com.android.weather.adapter.ResultListAdapter;
import com.android.weather.api.ApiManager;
import com.android.weather.api.entity.CityInfo;
import com.android.weather.api.entity.LocateState;
import com.android.weather.utils.DBManager;
import com.android.weather.utils.MxxNetworkUtil;
import com.android.weather.utils.MxxPreferenceUtil;
import com.android.weather.widget.CommomDialog;
import com.android.weather.widget.PinnedSectionListView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * .
 */
public class CityPickerActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_PICK_CITY = 2333;
    public static final String KEY_PICKED_CITY = "picked_city";

    private PinnedSectionListView mListView;
    private ListView mResultListView;
    //private SideLetterBar mLetterBar;
    private EditText searchBox;
    private ImageView clearBtn;
    private TextView backBtn;
    private ViewGroup emptyView;

    private CityListAdapter mCityAdapter;
    private ResultListAdapter mResultAdapter;
    private List<CityInfo> mAllCities;
    private DBManager dbManager;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener=new MyLocationListener();
    private final int maxCityNum = 8;
    private LocationInfo locationInfo;
    private  CommomDialog commomDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        initData();
        initView();
        initLocation();
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());

        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            String cityCode = location.getCityCode();

            if(TextUtils.isEmpty(district)&&TextUtils.isEmpty(city)){
                mCityAdapter.updateLocateState(LocateState.FAILED, null);
            }else {
                String locationStr = city;
                if(!TextUtils.isEmpty(district)){
                    locationStr = district;
                }
                locationInfo = new LocationInfo();
                locationInfo.setCity(city);
                locationInfo.setDistrict(district);
                String jsonStr = new Gson().toJson(locationInfo);
                boolean resutlt = MxxPreferenceUtil.setPrefString(CityPickerActivity.this,MxxPreferenceUtil.LOCATION_CITY, jsonStr);
                if(resutlt){
                    mCityAdapter.updateLocateState(LocateState.SUCCESS, locationStr);
                }
            }
        }
    }

    private void initData() {
        dbManager = new DBManager(this);
        dbManager.copyDBFile();
        mAllCities = dbManager.getAllCities();
        mAllCities = new ArrayList<>();
        mCityAdapter = new CityListAdapter(this, mAllCities);
        mCityAdapter.setOnCityClickListener(new CityListAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String city) {
                back(city,isLocationCity(city));
            }

            @Override
            public void onLocateClick() {
                mCityAdapter.updateLocateState(LocateState.LOCATING, null);
                mLocationClient.start();
            }
        });
        mResultAdapter = new ResultListAdapter(this, null);
    }

    private void initView() {
        mListView = (PinnedSectionListView) findViewById(R.id.listview_all_city);
        mListView.setAdapter(mCityAdapter);

        searchBox = (EditText) findViewById(R.id.et_search);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                if (TextUtils.isEmpty(keyword)) {
                    clearBtn.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    mResultListView.setVisibility(View.GONE);
                } else {
                    clearBtn.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.VISIBLE);
                    List<CityInfo> result = dbManager.searchCity(keyword);
                    if (result == null || result.size() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        mResultAdapter.changeData(result);
                    }
                }
            }
        });
        emptyView = (ViewGroup) findViewById(R.id.empty_view);
        mResultListView = (ListView) findViewById(R.id.listview_search_result);
        mResultListView.setAdapter(mResultAdapter);
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String city = mResultAdapter.getItem(position).getName();
                back(city,isLocationCity(city));
            }
        });
        clearBtn = (ImageView) findViewById(R.id.iv_search_clear);
        backBtn = (TextView) findViewById(R.id.back);
        clearBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    /**
     * 判断当前点击的是否是定位的城市
     * @param currentCity
     */
    private boolean isLocationCity(String currentCity){
        if(locationInfo != null){
            String city = locationInfo.getCity();
            String district = locationInfo.getDistrict();
            if(currentCity.contains(city)||currentCity.contains(district)
                    ||city.contains(currentCity)||district.contains(currentCity)){
                return true;
            }
        }
        return false;
    }

    private void back(String city,boolean isLocation){
        if(!MxxNetworkUtil.isNetworkAvailable(this)){
            showToast(getResources().getString(R.string.no_network));
            return;
        }
        if( ApiManager.getInstace().loadSelectedArea(this).size() >= maxCityNum) {
            showToast(getResources().getString(R.string.max_city));
            return;
        }
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("city",city);
        intent.putExtra("isLocation",isLocation);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_search_clear:
                searchBox.setText("");
                clearBtn.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
                mResultListView.setVisibility(View.GONE);
                break;
            case R.id.back:
                finishAllActivity();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAllActivity();
    }

    /**
     * 特殊情况退出所有的activity
     */
    private void finishAllActivity(){
        if( ApiManager.getInstace().loadSelectedArea(this).size()<=0){
            AppManager.getAppManager().finishAllActivity();
        }else{
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }
}