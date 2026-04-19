package com.kma.oj_app_mobile.api;

import android.content.Context;

import com.kma.oj_app_mobile.BuildConfig;
import com.kma.oj_app_mobile.utils.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Configured centrally via local.properties instead of hardcoding
    private static final String BASE_URL = BuildConfig.BASE_URL;
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = new TokenManager(context);
            AuthInterceptor authInterceptor = new AuthInterceptor(tokenManager);
            AuthAuthenticator authAuthenticator = new AuthAuthenticator(tokenManager);

            // Log ALL network traffic in Logcat under tag "OkHttp"
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .authenticator(authAuthenticator)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
