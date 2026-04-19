package com.kma.oj_app_mobile.api;

import com.kma.oj_app_mobile.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Don't add token to auth endpoints to prevent Spring Security errors 
        String url = originalRequest.url().toString();
        if (url.contains("/auth/login") || url.contains("/auth/register")) {
            return chain.proceed(originalRequest);
        }

        String token = tokenManager.getToken();
        String refreshToken = tokenManager.getRefreshToken();
        
        if (token != null && !token.isEmpty()) {
            String cookieHeader = "accessToken=" + token;
            if (refreshToken != null && !refreshToken.isEmpty()) {
                cookieHeader += "; refreshToken=" + refreshToken;
            }
            
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Cookie", cookieHeader)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}
