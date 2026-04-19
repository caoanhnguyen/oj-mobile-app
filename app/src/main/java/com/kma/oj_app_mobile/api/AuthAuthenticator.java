package com.kma.oj_app_mobile.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kma.oj_app_mobile.BuildConfig;
import com.kma.oj_app_mobile.utils.TokenManager;

import java.io.IOException;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class AuthAuthenticator implements Authenticator {

    private TokenManager tokenManager;

    public AuthAuthenticator(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        String refreshToken = tokenManager.getRefreshToken();
        
        Log.d("AuthAuthenticator", "Received 401. Attempting to refresh token...");

        // If no refresh token is present, we can't refresh
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null; 
        }

        // Avoid infinite loop if refresh API itself returns 401
        if (responseCount(response) >= 3 || isRefreshRequest(response)) {
            Log.d("AuthAuthenticator", "Failed to refresh or already tried. Giving up.");
            tokenManager.clearToken();
            return null;
        }

        synchronized (this) {
            // Check if token was already refreshed by another concurrent request
            String currentToken = tokenManager.getToken();
            String originalToken = response.request().header("Authorization");
            
            if (originalToken != null && currentToken != null) {
                // If the token in the failed request is different from what TokenManager currently has,
                // it means another thread has already refreshed it. Retry with the new token.
                String originalBearer = originalToken.replace("Bearer ", "").trim();
                if (!originalBearer.equals(currentToken)) {
                    Log.d("AuthAuthenticator", "Token already refreshed. Retrying...");
                    return buildNewRequest(response.request(), currentToken, tokenManager.getRefreshToken());
                }
            }

            // Otherwise, let's call the refresh API synchronously
            OkHttpClient client = new OkHttpClient();
            
            RequestBody emptyBody = RequestBody.create("{}", MediaType.parse("application/json"));
            Request refreshRequest = new Request.Builder()
                    .url(BuildConfig.BASE_URL + "auth/refresh")
                    .post(emptyBody)
                    .header("Cookie", "refreshToken=" + refreshToken)
                    .build();

            try (Response refreshResponse = client.newCall(refreshRequest).execute()) {
                if (refreshResponse.isSuccessful()) {
                    List<String> setCookies = refreshResponse.headers().values("Set-Cookie");
                    String newAccessToken = null;
                    String newRefreshToken = null;

                    for (String cookie : setCookies) {
                        String[] parts = cookie.split(";")[0].split("=");
                        if (parts.length > 1) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            if ("accessToken".equals(key)) {
                                newAccessToken = value;
                            } else if ("refreshToken".equals(key)) {
                                newRefreshToken = value;
                            }
                        }
                    }

                    if (newAccessToken != null) {
                        Log.d("AuthAuthenticator", "Token refresh successful!");
                        tokenManager.saveToken(newAccessToken);
                        if (newRefreshToken != null) {
                            tokenManager.saveRefreshToken(newRefreshToken);
                        } else {
                            newRefreshToken = refreshToken; 
                        }
                        
                        return buildNewRequest(response.request(), newAccessToken, newRefreshToken);
                    } else {
                        // Response success but no cookie?
                        Log.e("AuthAuthenticator", "Refresh successful but no accessToken in Set-Cookie!");
                        tokenManager.clearToken();
                    }
                } else {
                    Log.d("AuthAuthenticator", "Refresh API returned " + refreshResponse.code());
                    tokenManager.clearToken();
                }
            }
        }
        
        return null;
    }
    
    private boolean isRefreshRequest(Response response) {
        return response.request().url().toString().contains("/auth/refresh");
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private Request buildNewRequest(Request originalRequest, String newAccessToken, String newRefreshToken) {
        String cookieHeader = "accessToken=" + newAccessToken;
        if (newRefreshToken != null) {
            cookieHeader += "; refreshToken=" + newRefreshToken;
        }
        
        return originalRequest.newBuilder()
                .header("Authorization", "Bearer " + newAccessToken)
                .header("Cookie", cookieHeader)
                .build();
    }
}
