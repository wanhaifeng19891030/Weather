package com.android.weather;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.weather.adapter.CityManagerAdapter;
import com.android.weather.api.ApiManager;
import com.android.weather.utils.FileManager;
import com.android.weather.utils.MxxPreferenceUtil;
import com.android.weather.widget.CommomDialog;
import com.android.weather.widget.WeatherBaseWidget;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by admin on 2018/2/28.
 */
public class CityManagerActivity extends  BaseActivity{

    private ListView lv_city_view;
    private CityManagerAdapter adapter;
    private  List<ApiManager.Area> areas;
    private  CommomDialog commomDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_manager);
        initView();
        initData();
        initEvent();
    }

    private void initView(){
        lv_city_view = (ListView)findViewById(R.id.lv_city_view);
    }

    private void initEvent(){
        lv_city_view.setOnItemClickListener(cityItemClickListener);
        lv_city_view.setOnItemLongClickListener(cityItemLongClickListener);
    }

    private void initData(){

       areas = ApiManager.getInstace().loadSelectedArea(this);
        if(!areas.isEmpty()){
            adapter = new CityManagerAdapter(this,areas);
            lv_city_view.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private AdapterView.OnItemClickListener cityItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(CityManagerActivity.this,MainActivity.class);
            intent.putExtra("city",areas.get(i).city);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    private AdapterView.OnItemLongClickListener cityItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            String content = getResources().getString(R.string.content);
            String title = getResources().getString(R.string.title);
            content = String.format(content,areas.get(i).city);

            final int index = i;
            commomDialog = new CommomDialog(CityManagerActivity.this, R.style.dialog, content , new CommomDialog.OnCloseListener() {
                @Override
                public void onClick(Dialog dialog, boolean confirm) {
                    if(confirm){
                        boolean delResult = FileManager.deletefile(areas.get(index).id);
                        areas.remove(index);
                        boolean result = MxxPreferenceUtil.setPrefString(CityManagerActivity.this, MxxPreferenceUtil.KEY_SELECTED_AREA, new Gson().toJson(areas));
                        if(result&&delResult){
                            showLog("city area delete sucess!");
                            adapter.notifyDataSetChanged();
                            sendBroadcast(new Intent(MainActivity.ACTION_CITY_UPDATE));
                            sendBroadcast(new Intent(WeatherBaseWidget.UPDATE_WIDGET_WEATHER));
                            if(areas.size() <= 0){
                                Intent intent = new Intent(CityManagerActivity.this, CityPickerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivityForResult(intent, 1);
                            }
                        }else{
                            showLog("city area delete fail!");
                        }
                    }
                    commomDialog.dismiss();
                }
            });
            commomDialog.setTitle(title);
            commomDialog.show();
            return true;
        }
    };

    public void back(View v){
       finish();
    }

    public void add(View v){
        Intent intent = new Intent(this,CityPickerActivity.class);
        startActivityForResult(intent,1);
    }
}
