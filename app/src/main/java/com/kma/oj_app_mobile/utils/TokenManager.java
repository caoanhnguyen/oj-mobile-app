package com.kma.oj_app_mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    private static final String PREF_NAME = "secure_auth_prefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USERNAME = "username";
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_JWT_TOKEN, token).apply();
        }
    }

    public void saveUsername(String username) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
        }
    }

    public void saveRefreshToken(String token) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply();
        }
    }

    public String getToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_JWT_TOKEN, null);
        }
        return null;
    }

    public String getUsername() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_USERNAME, "default_user");
        }
        return "default_user";
    }

    public String getRefreshToken() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
        }
        return null;
    }

    public void clearToken() {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .remove(KEY_JWT_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
                    .remove(KEY_USERNAME)
                    .apply();
        }
    }
}
