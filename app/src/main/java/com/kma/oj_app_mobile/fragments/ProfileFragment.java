package com.kma.oj_app_mobile.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.utils.TokenManager;
import com.kma.oj_app_mobile.dto.UserHeatMap;
import com.kma.oj_app_mobile.dto.UserProfile;
import com.kma.oj_app_mobile.activities.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    // ── Header ───────────────────────────────────────────────────────────────
    private ImageView btnLogout, ivAvatar;
    private TextView tvFullName, tvEmail;

    // ── Bio ───────────────────────────────────────────────────────────────────
    private LinearLayout layoutBio;
    private WebView wvBio;

    // ── Info tab fields ───────────────────────────────────────────────────────
    private TextView tvGender, tvPhone, tvSchool, tvMajor, tvLocation, tvJoinedDate;

    // ── Social chips (LinearLayouts acting as clickable rows) ─────────────────
    private LinearLayout btnGithub, btnLinkedIn, btnWebsite;
    private TextView tvNoSocialLinks;
    private String githubUrl, linkedInUrl, websiteUrl;

    // ── Stats tab ─────────────────────────────────────────────────────────────
    private TextView tvSolved, tvSubmissions, tvScore, tvAcRate;
    private TextView tvHeatmapSubtitle;

    // ── Heatmap ───────────────────────────────────────────────────────────────
    private HorizontalScrollView heatmapScroll;
    private LinearLayout heatmapMonthRow;
    private GridLayout heatmapGrid;
    private ProgressBar progressBar;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private TabLayout tabLayout;
    private LinearLayout layoutInfo, layoutStats;

    private ApiService apiService;

    private static final String[] MONTH_LABELS = {"Th1","Th2","Th3","Th4","Th5","Th6","Th7","Th8","Th9","Th10","Th11","Th12"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        fetchProfile();
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initViews(View v) {
        btnLogout    = v.findViewById(R.id.btnLogout);
        ivAvatar     = v.findViewById(R.id.ivAvatar);
        tvFullName   = v.findViewById(R.id.tvFullName);
        tvEmail      = v.findViewById(R.id.tvEmail);

        layoutBio    = v.findViewById(R.id.layoutBio);
        wvBio        = v.findViewById(R.id.wvBio);

        tvGender     = v.findViewById(R.id.tvGender);
        tvPhone      = v.findViewById(R.id.tvPhone);
        tvSchool     = v.findViewById(R.id.tvSchool);
        tvMajor      = v.findViewById(R.id.tvMajor);
        tvLocation   = v.findViewById(R.id.tvLocation);
        tvJoinedDate = v.findViewById(R.id.tvJoinedDate);

        btnGithub       = v.findViewById(R.id.btnGithub);
        btnLinkedIn     = v.findViewById(R.id.btnLinkedIn);
        btnWebsite      = v.findViewById(R.id.btnWebsite);
        tvNoSocialLinks = v.findViewById(R.id.tvNoSocialLinks);

        tvSolved        = v.findViewById(R.id.tvSolved);
        tvSubmissions   = v.findViewById(R.id.tvSubmissions);
        tvScore         = v.findViewById(R.id.tvScore);
        tvAcRate        = v.findViewById(R.id.tvAcRate);
        tvHeatmapSubtitle = v.findViewById(R.id.tvHeatmapSubtitle);

        heatmapScroll   = v.findViewById(R.id.heatmapScroll);
        heatmapMonthRow = v.findViewById(R.id.heatmapMonthRow);
        heatmapGrid     = v.findViewById(R.id.heatmapGrid);
        progressBar     = v.findViewById(R.id.progressBar);

        tabLayout    = v.findViewById(R.id.tabLayout);
        layoutInfo   = v.findViewById(R.id.layoutInfo);
        layoutStats  = v.findViewById(R.id.layoutStats);

        // ── WebView setup for bio HTML ──
        if (wvBio != null) {
            WebSettings ws = wvBio.getSettings();
            ws.setJavaScriptEnabled(false);
            ws.setLoadWithOverviewMode(true);
            ws.setUseWideViewPort(false);
            wvBio.setBackgroundColor(Color.TRANSPARENT);
            wvBio.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            wvBio.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            wvBio.setWebViewClient(new WebViewClient());
        }

        // ── Click handlers ──
        btnLogout.setOnClickListener(x -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        btnGithub.setOnClickListener(x -> openUrl(githubUrl));
        btnLinkedIn.setOnClickListener(x -> openUrl(linkedInUrl));
        btnWebsite.setOnClickListener(x -> openUrl(websiteUrl));

        // ── Tab switching ──
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean showInfo = (tab.getPosition() == 0);
                layoutInfo.setVisibility(showInfo ? View.VISIBLE : View.GONE);
                layoutStats.setVisibility(showInfo ? View.GONE : View.VISIBLE);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void openUrl(String url) {
        if (url == null || url.isEmpty()) return;
        try {
            String full = url.startsWith("http") ? url : "https://" + url;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(full)));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
        }
    }

    private void performLogout() {
        new TokenManager(getContext()).clearToken();
        Intent i = new Intent(getActivity(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        if (getActivity() != null) getActivity().finish();
    }

    // ── API ───────────────────────────────────────────────────────────────────

    private void fetchProfile() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getMe().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call,
                                   Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    UserProfile p = response.body().getData();
                    bindProfileData(p);
                    if (p.getId() != null) {
                        fetchHeatmap(p.getId());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        drawHeatmap(null);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Lỗi tải hồ sơ (" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHeatmap(String userId) {
        apiService.getHeatmap(userId).enqueue(new Callback<ApiResponse<UserHeatMap>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserHeatMap>> call,
                                   Response<ApiResponse<UserHeatMap>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    UserHeatMap data = response.body().getData();
                    List<UserHeatMap.HeatMapItem> items = data.getHeatmapItems();
                    int total = data.getTotalSubmissions();
                    Log.d("Profile", "Heatmap ok. total=" + total
                            + " items=" + (items != null ? items.size() : 0));
                    if (tvHeatmapSubtitle != null)
                        tvHeatmapSubtitle.setText(total + " submissions trong năm qua");
                    drawHeatmap(items);
                } else {
                    Log.e("Profile", "Heatmap err: " + response.code());
                    if (tvHeatmapSubtitle != null)
                        tvHeatmapSubtitle.setText("Lỗi tải lịch sử (" + response.code() + ")");
                    drawHeatmap(null);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<UserHeatMap>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("Profile", "Heatmap fail: " + t.getMessage(), t);
                if (tvHeatmapSubtitle != null)
                    tvHeatmapSubtitle.setText("Không thể tải lịch sử");
                drawHeatmap(null);
            }
        });
    }

    // ── Data Binding ──────────────────────────────────────────────────────────

    private void bindProfileData(UserProfile p) {
        // Common header
        tvFullName.setText(notBlank(p.getFullName()) ? p.getFullName() : p.getUsername());
        tvEmail.setText(notBlank(p.getEmail()) ? p.getEmail() : "");
        // Avatar
        Glide.with(this)
             .load(notBlank(p.getAvatarUrl()) ? p.getAvatarUrl() : R.drawable.ic_profile)
             .placeholder(R.drawable.ic_profile)
             .error(R.drawable.ic_profile)
             .circleCrop()
             .into(ivAvatar);

        // Bio (HTML from Quill)
        if (notBlank(p.getBio()) && wvBio != null) {
            layoutBio.setVisibility(View.VISIBLE);
            String html = buildBioHtml(p.getBio());
            wvBio.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }

        // Info
        tvGender.setText(genderText(p.getGender()));
        tvPhone.setText(notBlank(p.getPhoneNumber()) ? p.getPhoneNumber() : "Chưa cập nhật");
        tvSchool.setText(notBlank(p.getSchool()) ? p.getSchool() : "Chưa cập nhật");
        tvMajor.setText(notBlank(p.getMajor()) ? p.getMajor() : "Chưa cập nhật");

        String loc = "";
        if (notBlank(p.getCity())) loc = p.getCity();
        if (notBlank(p.getCountry())) loc += (loc.isEmpty() ? "" : ", ") + p.getCountry();
        tvLocation.setText(loc.isEmpty() ? "Chưa cập nhật" : loc);
        tvJoinedDate.setText(formatDate(p.getCreatedDate()));

        // Social link chips
        boolean hasSocial = false;
        if (notBlank(p.getGithubUrl())) {
            githubUrl = p.getGithubUrl();
            btnGithub.setVisibility(View.VISIBLE);
            hasSocial = true;
        }
        if (notBlank(p.getLinkedInUrl())) {
            linkedInUrl = p.getLinkedInUrl();
            btnLinkedIn.setVisibility(View.VISIBLE);
            hasSocial = true;
        }
        if (notBlank(p.getWebsite())) {
            websiteUrl = p.getWebsite();
            btnWebsite.setVisibility(View.VISIBLE);
            hasSocial = true;
        }
        tvNoSocialLinks.setVisibility(hasSocial ? View.GONE : View.VISIBLE);

        // Stats
        int solved = p.getSolvedCount() != null ? p.getSolvedCount() : 0;
        int sub    = p.getSubmissionCount() != null ? p.getSubmissionCount() : 0;
        int ac     = p.getAcCount() != null ? p.getAcCount() : 0;
        double score = p.getTotalScore() != null ? p.getTotalScore() : 0;

        tvSolved.setText(String.valueOf(solved));
        tvSubmissions.setText(String.valueOf(sub));
        tvScore.setText(String.valueOf(Math.round(score)));
        tvAcRate.setText(sub > 0
                ? String.format(Locale.getDefault(), "%.2f%%", (double) ac / sub * 100.0)
                : "0.00%");
    }

    /**
     * Wraps Quill HTML in a minimal dark-themed HTML page so the WebView renders it properly.
     */
    private String buildBioHtml(String quillHtml) {
        return "<!DOCTYPE html><html><head>"
                + "<meta charset='UTF-8'/>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'/>"
                + "<style>"
                + "  body { margin:0; padding:0; background:transparent;"
                + "         color:#CCCCCC; font-family:sans-serif; font-size:14px; line-height:1.6; }"
                + "  a    { color:#7C9EFF; }"
                + "  p    { margin:0 0 8px 0; }"
                + "  ul,ol{ margin:0 0 8px 0; padding-left:20px; }"
                + "  h1,h2,h3 { color:#FFFFFF; }"
                + "  strong, b { color:#FFFFFF; }"
                + "</style></head><body>"
                + quillHtml
                + "</body></html>";
    }

    // ── Heatmap ───────────────────────────────────────────────────────────────

    private void drawHeatmap(List<UserHeatMap.HeatMapItem> items) {
        if (heatmapGrid == null || getContext() == null) return;
        heatmapGrid.removeAllViews();
        heatmapMonthRow.removeAllViews();

        // Build date → count lookup
        Map<String, Integer> counts = new HashMap<>();
        if (items != null) {
            for (UserHeatMap.HeatMapItem item : items) {
                String key = item.getDateKey();
                if (key != null && item.getCount() != null)
                    counts.put(key, item.getCount());
            }
        }
        Log.d("Heatmap", "drawHeatmap: " + counts.size() + " dates with activity");

        float dp       = getContext().getResources().getDisplayMetrics().density;
        int cellSize   = Math.round(14 * dp);   // slightly larger cells
        int cellMargin = Math.round(2 * dp);
        int colWidth   = cellSize + cellMargin * 2;

        // ── Calendar: start 363 days ago, aligned to Monday of that week ──
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -363);

        // Column-major layout: col = week index (0..51), row = day-of-week Mon=0..Sun=6
        int COLS = 52;
        int ROWS = 7;
        heatmapGrid.setColumnCount(COLS);
        heatmapGrid.setRowCount(ROWS);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Track current month for labels
        int lastMonth = -1;

        for (int col = 0; col < COLS; col++) {
            // On first day of this week column, check if month changes
            Calendar weekStart = (Calendar) cal.clone();
            int monthOfWeekStart = weekStart.get(Calendar.MONTH); // 0-based

            if (monthOfWeekStart != lastMonth) {
                // Add a month label view aligned over this column
                TextView label = new TextView(getContext());
                label.setText(MONTH_LABELS[monthOfWeekStart]);
                label.setTextColor(Color.parseColor("#888888"));
                label.setTextSize(9f);  // sp

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        colWidth * 1, // ideally we'd span till next month, but simple approach
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                // If not first label, add invisible spacers for skipped columns since last label
                label.setLayoutParams(lp);
                label.setMaxLines(1);
                heatmapMonthRow.addView(label);

                lastMonth = monthOfWeekStart;
            } else {
                // Filler spacer so labels align with their column
                View spacer = new View(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(colWidth, 1);
                spacer.setLayoutParams(lp);
                heatmapMonthRow.addView(spacer);
            }

            // Draw 7 cells in this column
            for (int row = 0; row < ROWS; row++) {
                String dateKey = fmt.format(cal.getTime());
                int count = counts.containsKey(dateKey) ? counts.get(dateKey) : 0;

                View cell = new View(getContext());
                GridLayout.LayoutParams glp = new GridLayout.LayoutParams(
                        GridLayout.spec(row),
                        GridLayout.spec(col)
                );
                glp.width  = cellSize;
                glp.height = cellSize;
                glp.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);
                cell.setLayoutParams(glp);
                cell.setBackgroundColor(heatColor(count));

                heatmapGrid.addView(cell);
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        // ── Auto-scroll to the rightmost end (most recent = today) ──
        heatmapScroll.post(() -> heatmapScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
    }

    private int heatColor(int count) {
        if (count == 0)  return Color.parseColor("#2A2A3A");
        if (count <= 2)  return Color.parseColor("#0e4429");
        if (count <= 5)  return Color.parseColor("#006d32");
        if (count <= 10) return Color.parseColor("#26a641");
        return               Color.parseColor("#39d353");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean notBlank(String s) { return s != null && !s.isEmpty(); }

    private String genderText(String g) {
        if (g == null) return "Chưa cập nhật";
        switch (g) {
            case "MALE":   return "Nam";
            case "FEMALE": return "Nữ";
            default:       return g;
        }
    }

    private String formatDate(String iso) {
        if (!notBlank(iso) || iso.length() < 10) return "Chưa cập nhật";
        try {
            String[] p = iso.substring(0, 10).split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception e) { return iso; }
    }

    private String trimUrl(String url) {
        if (url == null) return "";
        return url.replaceFirst("https?://(www\\.)?", "").replaceAll("/$", "");
    }
}
