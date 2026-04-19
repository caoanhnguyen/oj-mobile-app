package com.kma.oj_app_mobile.api;

import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.dto.ContestDetail;
import com.kma.oj_app_mobile.dto.LoginRequest;
import com.kma.oj_app_mobile.commons.PageData;
import com.kma.oj_app_mobile.dto.RankingUser;
import com.kma.oj_app_mobile.dto.RegisterRequest;
import com.kma.oj_app_mobile.dto.UserHeatMap;
import com.kma.oj_app_mobile.dto.UserProfile;
import com.kma.oj_app_mobile.models.Contest;
import com.kma.oj_app_mobile.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface ApiService {
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest request);

    @GET("contests")
    Call<ApiResponse<PageData<Contest>>> getContests(@retrofit2.http.Query("contestStatus") String contestStatus);
    
    @GET("contests/{contestKey}")
    Call<ApiResponse<ContestDetail>> getContestDetails(@retrofit2.http.Path("contestKey") String contestKey);
    
    @POST("contests/{contestKey}/register")
    Call<ApiResponse<Object>> registerContest(@retrofit2.http.Path("contestKey") String contestKey);
    
    @GET("users/me")
    Call<ApiResponse<UserProfile>> getMe();
    
    @GET("users/{id}/heatmap")
    Call<ApiResponse<UserHeatMap>> getHeatmap(@retrofit2.http.Path("id") String id);

    @GET("rankings")
    Call<ApiResponse<PageData<RankingUser>>> getRankings(
        @retrofit2.http.Query("ruleType") String ruleType,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("size") Integer size
    );

    @GET("contests/{contestKey}/leaderboard")
    Call<ApiResponse<PageData<RankingUser>>> getContestLeaderboard(
        @retrofit2.http.Path("contestKey") String contestKey,
        @retrofit2.http.Query("page") Integer page,
        @retrofit2.http.Query("size") Integer size
    );
}
