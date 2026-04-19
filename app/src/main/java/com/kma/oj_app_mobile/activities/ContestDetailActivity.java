package com.kma.oj_app_mobile.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.CalendarContract;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import android.widget.LinearLayout;

import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.dto.ContestDetail;
import com.kma.oj_app_mobile.commons.PageData;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.adapters.RankingAdapter;
import com.kma.oj_app_mobile.dto.RankingUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContestDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvAuthor, tvParticipants, tvRuleType, tvStatus, tvDuration, tvTime;
    private WebView wvDescription;
    private Button btnRegister, btnCalendar;
    private ProgressBar progressBar;
    
    private TabLayout tabLayout;
    private LinearLayout layDescription, layLeaderboard;
    private RecyclerView rvLeaderboard;
    private TextView tvLeaderboardEmpty, tvHeaderValue;
    private RankingAdapter rankingAdapter;
    
    private String contestKey;
    private ContestDetail currentContest;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contest_detail);

        contestKey = getIntent().getStringExtra("CONTEST_KEY");
        if (contestKey == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Contest", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient(this).create(ApiService.class);
        initViews();
        fetchContestDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When user returns from Calendar app, re-check and hide the button if flag was set
        if (contestKey != null) {
            SharedPreferences prefs = getSharedPreferences("KMA_OJ_PREFS", MODE_PRIVATE);
            boolean addedToCalendar = prefs.getBoolean("CALENDAR_" + contestKey, false);
            if (addedToCalendar && btnCalendar != null) {
                btnCalendar.setVisibility(View.GONE);
            }
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvParticipants = findViewById(R.id.tvParticipants);
        tvRuleType = findViewById(R.id.tvRuleType);
        tvStatus = findViewById(R.id.tvStatus);
        tvDuration = findViewById(R.id.tvDuration);
        tvTime = findViewById(R.id.tvTime);
        wvDescription = findViewById(R.id.wvDescription);
        btnRegister = findViewById(R.id.btnRegister);
        btnCalendar = findViewById(R.id.btnCalendar);
        progressBar = findViewById(R.id.progressBar);

        if (wvDescription != null) {
            WebSettings ws = wvDescription.getSettings();
            ws.setJavaScriptEnabled(false);
            wvDescription.setBackgroundColor(0x00000000); // 0 or Color.TRANSPARENT
            wvDescription.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            wvDescription.setWebViewClient(new WebViewClient());
        }

        tabLayout = findViewById(R.id.tabLayout);
        layDescription = findViewById(R.id.layDescription);
        layLeaderboard = findViewById(R.id.layLeaderboard);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        tvLeaderboardEmpty = findViewById(R.id.tvLeaderboardEmpty);
        tvHeaderValue = findViewById(R.id.tvHeaderValue);

        if (tabLayout != null) {
            tabLayout.addTab(tabLayout.newTab().setText("Mô tả"));
            tabLayout.addTab(tabLayout.newTab().setText("Bảng xếp hạng"));
            
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getPosition() == 1) {
                        layDescription.setVisibility(View.GONE);
                        layLeaderboard.setVisibility(View.VISIBLE);
                        if (rankingAdapter == null && currentContest != null) {
                            loadLeaderboard();
                        }
                    } else {
                        layDescription.setVisibility(View.VISIBLE);
                        layLeaderboard.setVisibility(View.GONE);
                    }
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
        
        if (rvLeaderboard != null) {
            rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
            rvLeaderboard.setNestedScrollingEnabled(false);
        }

        btnBack.setOnClickListener(v -> finish());
        
        btnRegister.setOnClickListener(v -> handleRegistration());
        
        btnCalendar.setOnClickListener(v -> handleAddToCalendar());
    }

    private void fetchContestDetails() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getContestDetails(contestKey).enqueue(new Callback<ApiResponse<ContestDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<ContestDetail>> call, Response<ApiResponse<ContestDetail>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentContest = response.body().getData();
                    populateData(currentContest);
                } else {
                    Toast.makeText(ContestDetailActivity.this, "Lỗi tải dữ liệu chi tiết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ContestDetail>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ContestDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData(ContestDetail contest) {
        tvTitle.setText(contest.getTitle() != null ? contest.getTitle() : "Untitled");
        tvAuthor.setText(contest.getAuthorUsername() != null ? "Bởi " + contest.getAuthorUsername() : "Bởi Admin");
        tvParticipants.setText((contest.getParticipantCount() != null ? contest.getParticipantCount() : 0) + " tham gia");
        
        String rule = contest.getRuleType() != null ? contest.getRuleType() : "Unknown";
        tvRuleType.setText(rule);
        if ("ACM".equalsIgnoreCase(rule)) {
            tvRuleType.setBackgroundResource(R.drawable.bg_badge_acm);
            tvRuleType.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("OI".equalsIgnoreCase(rule)) {
            tvRuleType.setBackgroundResource(R.drawable.bg_badge_oi);
            tvRuleType.setTextColor(Color.parseColor("#F57C00"));
        } else {
            tvRuleType.setBackgroundResource(R.drawable.bg_badge_finished);
            tvRuleType.setTextColor(Color.parseColor("#9E9E9E"));
        }

        if (contest.getDurationMinutes() != null) {
            tvDuration.setVisibility(View.VISIBLE);
            tvDuration.setText(contest.getDurationMinutes() + " Phút");
        } else {
            tvDuration.setVisibility(View.GONE);
        }
        
        String startTime = formatTime(contest.getStartTime());
        String endTime = formatTime(contest.getEndTime());
        tvTime.setText(startTime + " - " + endTime);
        
        if (contest.getDescription() != null && !contest.getDescription().isEmpty()) {
            String htmlContent = buildHtml(contest.getDescription());
            wvDescription.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        } else {
            String htmlContent = buildHtml("Không có mô tả.");
            if (wvDescription != null) wvDescription.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        }

        // Setup Header Value for Leaderboard
        if (tvHeaderValue != null) {
            if ("ACM".equalsIgnoreCase(rule)) {
                tvHeaderValue.setText("SỐ BÀI GIẢI");
            } else {
                tvHeaderValue.setText("ĐIỂM");
            }
        }
        
        // Update tab title for disabled status visually
        if (tabLayout != null && tabLayout.getTabCount() > 1) {
            String sbv = contest.getScoreboardVisibility();
            String status = contest.getContestStatus() != null ? contest.getContestStatus().toUpperCase() : "UNKNOWN";
            boolean canView = "ALWAYS_VISIBLE".equals(sbv) || "PUBLIC".equals(sbv) || 
                              ("HIDDEN_DURING_CONTEST".equals(sbv) && ("ENDED".equals(status) || "FINISHED".equals(status)));
            
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) {
                if (!canView) {
                    tab.setText("Xếp hạng (Ẩn)");
                    if (tab.view != null) {
                        tab.view.setEnabled(false);
                        tab.view.setClickable(false);
                    }
                } else {
                    tab.setText("Bảng xếp hạng");
                    if (tab.view != null) {
                        tab.view.setEnabled(true);
                        tab.view.setClickable(true);
                    }
                }
            }
        }

        // Deal with status colors
        String status = contest.getContestStatus() != null ? contest.getContestStatus().toUpperCase() : "UNKNOWN";
        tvStatus.setText(status);
        switch (status) {
            case "ONGOING":
                tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
                tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "UPCOMING":
                tvStatus.setBackgroundResource(R.drawable.bg_badge_upcoming);
                tvStatus.setTextColor(Color.parseColor("#F57C00"));
                break;
            default: // ENDED
                tvStatus.setBackgroundResource(R.drawable.bg_badge_finished);
                tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                tvStatus.setText("ENDED");
                break;
        }

        // Deal with registration state mapping EXACT visibility rules
        // Use simple contestKey as SharedPreferences key (cleared on logout via clearToken)
        SharedPreferences prefs = getSharedPreferences("KMA_OJ_PREFS", MODE_PRIVATE);
        String calendarPrefKey = "CALENDAR_" + this.contestKey;
        android.util.Log.d("ContestDetail", "calendarPrefKey=" + calendarPrefKey + " value=" + prefs.getBoolean(calendarPrefKey, false));
        boolean addedToCalendar = prefs.getBoolean(calendarPrefKey, false);

        btnCalendar.setVisibility(View.GONE);
        if ("ENDED".equals(status) || "FINISHED".equals(status)) {
            btnRegister.setText("ĐÃ KẾT THÚC");
            btnRegister.setBackgroundColor(Color.parseColor("#9E9E9E"));
            btnRegister.setEnabled(false);
        } else if ("ONGOING".equals(status)) {
            if (contest.isRegistered()) {
                btnRegister.setText("ĐÃ ĐĂNG KÝ");
                btnRegister.setBackgroundColor(Color.parseColor("#9E9E9E"));
                btnRegister.setEnabled(false);
            } else {
                Boolean allowLate = contest.getAllowLateRegistration();
                if (allowLate != null && allowLate) {
                    btnRegister.setText("ĐĂNG KÝ VÀO THI NGAY");
                    btnRegister.setBackgroundResource(R.drawable.bg_button);
                    btnRegister.setEnabled(true);
                } else {
                    btnRegister.setText("HẾT HẠN ĐĂNG KÝ");
                    btnRegister.setBackgroundColor(Color.parseColor("#9E9E9E"));
                    btnRegister.setEnabled(false);
                }
            }
        } else {
            // UPCOMING STATUS
            if (contest.isRegistered()) {
                btnRegister.setText("ĐÃ ĐĂNG KÝ");
                btnRegister.setBackgroundColor(Color.parseColor("#9E9E9E"));
                btnRegister.setEnabled(false);
                if (!addedToCalendar) {
                    btnCalendar.setVisibility(View.VISIBLE); // Let them add to calendar instead!
                }
            } else {
                btnRegister.setText("ĐĂNG KÝ THAM GIA");
                btnRegister.setBackgroundResource(R.drawable.bg_button);
                btnRegister.setEnabled(true);
            }
        }
    }

    private String buildHtml(String body) {
        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>"
                + "<style>"
                + "body { font-family: sans-serif; font-size: 15px; color: #B0BEC5; line-height: 1.6; padding: 0; margin: 0; background-color: transparent; word-wrap: break-word; }"
                + "blockquote { border-left: 4px solid #F57C00; margin: 0 0 16px; padding-left: 12px; font-style: italic; color: #9E9E9E; }"
                + "pre { background-color: #1E1E1E; padding: 12px; border-radius: 8px; overflow-x: auto; color: #E0E0E0; font-family: monospace; font-size: 14px; }"
                + "code { background-color: #1E1E1E; padding: 2px 4px; border-radius: 4px; font-family: monospace; font-size: 14px; }"
                + "img { max-width: 100%; height: auto; display: block; margin: 8px auto; border-radius: 8px; }"
                + "a { color: #F57C00; text-decoration: none; }"
                + "ul, ol { padding-left: 20px; }"
                + "h1, h2, h3, h4, h5, h6 { color: #E0E0E0; margin-top: 24px; margin-bottom: 12px; }"
                + "</style></head><body>"
                + body
                + "</body></html>";
    }

    private void handleAddToCalendar() {
        if (currentContest == null) return;
        
        // 1. Immediately hide and save flag to guarantee UX requirement
        SharedPreferences prefs = getSharedPreferences("KMA_OJ_PREFS", MODE_PRIVATE);
        String calendarPrefKey = "CALENDAR_" + ContestDetailActivity.this.contestKey;
        prefs.edit().putBoolean(calendarPrefKey, true).apply();
        btnCalendar.setVisibility(View.GONE);

        // 2. Parse date and trigger Calendar intent safely
        try {
            long beginTime = System.currentTimeMillis();
            if (currentContest.getStartTime() != null) {
                String safeTime = currentContest.getStartTime();
                // Strip milliseconds and timezone Z for reliable simple parsing
                if (safeTime.contains(".")) safeTime = safeTime.substring(0, safeTime.indexOf("."));
                if (safeTime.endsWith("Z")) safeTime = safeTime.replace("Z", "");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date startDate = sdf.parse(safeTime);
                if (startDate != null) {
                    beginTime = startDate.getTime();
                }
            }
            
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                    .putExtra(CalendarContract.Events.TITLE, "OJ Contest: " + (currentContest.getTitle() != null ? currentContest.getTitle() : "N/A"))
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Đừng quên tham gia bài thi này nhé!")
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không có ứng dụng Lịch nào trên máy!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Đã lưu vào danh sách, nhưng không thể mở Lịch!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRegistration() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        
        apiService.registerContest(contestKey).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(ContestDetailActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    if (currentContest != null) {
                       currentContest.setRegistered(true);
                       populateData(currentContest); // Automatically reload UI to match calendar injection
                    }
                } else {
                    btnRegister.setEnabled(true);
                    Toast.makeText(ContestDetailActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(ContestDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTime(String rawIsoTime) {
        if (rawIsoTime == null || rawIsoTime.isEmpty()) return "?";
        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(rawIsoTime);
            
            SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            localFormat.setTimeZone(TimeZone.getDefault());
            if (date != null) {
                return localFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawIsoTime;
    }

    private void loadLeaderboard() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (tvLeaderboardEmpty != null) tvLeaderboardEmpty.setVisibility(View.GONE);
        
        apiService.getContestLeaderboard(contestKey, 0, 100).enqueue(new Callback<ApiResponse<PageData<RankingUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageData<RankingUser>>> call, Response<ApiResponse<PageData<RankingUser>>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PageData<RankingUser> pageData = response.body().getData();
                    if (pageData.getContent() != null && !pageData.getContent().isEmpty()) {
                        rankingAdapter = new RankingAdapter();
                        rankingAdapter.setUsers(pageData.getContent(), currentContest.getRuleType());
                        if (rvLeaderboard != null) {
                            rvLeaderboard.setAdapter(rankingAdapter);
                            rvLeaderboard.setVisibility(View.VISIBLE);
                        }
                        if (tvLeaderboardEmpty != null) tvLeaderboardEmpty.setVisibility(View.GONE);
                    } else {
                        if (tvLeaderboardEmpty != null) tvLeaderboardEmpty.setVisibility(View.VISIBLE);
                        if (rvLeaderboard != null) rvLeaderboard.setVisibility(View.GONE);
                    }
                } else {
                    if (tvLeaderboardEmpty != null) {
                        tvLeaderboardEmpty.setVisibility(View.VISIBLE);
                        tvLeaderboardEmpty.setText("Không thể tải bảng xếp hạng");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageData<RankingUser>>> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (tvLeaderboardEmpty != null) {
                    tvLeaderboardEmpty.setVisibility(View.VISIBLE);
                    tvLeaderboardEmpty.setText("Lỗi mạng: " + t.getMessage());
                }
            }
        });
    }
}
