package com.kma.oj_app_mobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.dto.LoginRequest;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.utils.TokenManager;
import com.kma.oj_app_mobile.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegister;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvLogo), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top + 40, 0, 0); // Add top padding for notch
            return insets;
        });

        tokenManager = new TokenManager(this);

        // If user already logged in, navigate straight to MainActivity
        if (tokenManager.getToken() != null) {
            goToMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> performLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Không được để trống Tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Không được để trống Mật khẩu");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(username, password);
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.login(request).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveUsername(username);
                    // Extract set-cookie securely
                    extractAndSaveToken(response.headers().values("Set-Cookie"));
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Kiểm tra lại thông tin.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void extractAndSaveToken(List<String> cookies) {
        for (String cookie : cookies) {
            String[] parts = cookie.split(";")[0].split("=");
            if (parts.length > 1) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                if ("accessToken".equals(key)) {
                    tokenManager.saveToken(value);
                } else if ("refreshToken".equals(key)) {
                    tokenManager.saveRefreshToken(value);
                }
            }
        }
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        etUsername.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
