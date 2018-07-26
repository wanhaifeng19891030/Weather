package com.android.weather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.weather.R;
import com.android.weather.api.ApiManager;

import java.util.List;

/**
 * Created by admin on 2018/3/1.
 */
public class CityManagerAdapter extends BaseAdapter {

    private List<ApiManager.Area> areas;
    private Context context;
    private LayoutInflater inflater;

    public CityManagerAdapter(Context context,List<ApiManager.Area> areas){
        this.context = context;
        this.areas = areas;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return areas.size();
    }

    @Override
    public Object getItem(int i) {
        return areas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.city_manager_item,null);
            viewHolder.tv_city = (TextView) view.findViewById(R.id.tv_city);
            viewHolder.tv_temp = (TextView) view.findViewById(R.id.tv_temp);
            viewHolder.tv_weather = (TextView) view.findViewById(R.id.tv_weather);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        String city = areas.get(i).city;
        String temp = areas.get(i).temp;
        String weather = areas.get(i).weather;

        viewHolder.tv_city.setText(city);
        viewHolder.tv_temp.setText(temp+"℃");
        viewHolder.tv_weather.setText(weather);

        return view;
    }

    class ViewHolder{
        TextView tv_city;
        TextView tv_temp;
        TextView tv_weather;
    }
}
