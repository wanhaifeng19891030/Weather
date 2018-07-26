package com.android.weather;

import com.qucii.sdk.transfer.Resp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface QuciiInterface {

	@GET("/weather/get")
	Call<Resp<InfoBean>> getWearther(@Query("city") String token);

}
