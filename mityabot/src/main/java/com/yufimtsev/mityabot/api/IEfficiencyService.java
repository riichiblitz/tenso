package com.yufimtsev.mityabot.api;

import com.yufimtsev.mityabot.model.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IEfficiencyService {

    @GET("eff")
    Call<ApiResponse> getEfficiency(@Query("hand") String hand, @Query("used") String used);


}
