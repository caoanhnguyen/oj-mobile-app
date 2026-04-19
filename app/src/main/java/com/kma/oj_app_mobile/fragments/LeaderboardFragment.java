package com.kma.oj_app_mobile.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.components.BarChartView;
import com.kma.oj_app_mobile.commons.PageData;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.adapters.RankingAdapter;
import com.kma.oj_app_mobile.dto.RankingUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView rvRanking;
    private ProgressBar progressBar;
    private BarChartView barChartView;
    private TextView tvChartHeader, tvHeaderValue, tvMemberCount;
    private Button btnLoadMore;
    
    private RankingAdapter adapter;
    private String currentRuleType = "ACM";

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = 0;
    private List<RankingUser> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        tabLayout = view.findViewById(R.id.tabLayout);
        rvRanking = view.findViewById(R.id.rvRanking);
        progressBar = view.findViewById(R.id.progressBar);
        barChartView = view.findViewById(R.id.barChartView);
        tvChartHeader = view.findViewById(R.id.tvChartHeader);
        tvHeaderValue = view.findViewById(R.id.tvHeaderValue);
        tvMemberCount = view.findViewById(R.id.tvMemberCount);
        btnLoadMore = view.findViewById(R.id.btnLoadMore);

        rvRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RankingAdapter();
        adapter.setOnItemClickListener(user -> showDetailModal(user));
        rvRanking.setAdapter(adapter);

        btnLoadMore.setOnClickListener(v -> fetchRankings(false));

        rvRanking.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // Pagination is handled by the explicit Load More button - no auto-scroll needed
            }
        });

        setupTabLayout();
        fetchRankings(true);

        return view;
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("ACM"));
        tabLayout.addTab(tabLayout.newTab().setText("OI"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentRuleType = tab.getPosition() == 0 ? "ACM" : "OI";
                if ("ACM".equals(currentRuleType)) {
                    tvChartHeader.setText("TOP 10 SỐ BÀI GIẢI");
                    if (tvHeaderValue != null) tvHeaderValue.setText("SỐ BÀI GIẢI");
                } else {
                    tvChartHeader.setText("TOP 10 TỔNG ĐIỂM");
                    if (tvHeaderValue != null) tvHeaderValue.setText("ĐIỂM");
                }
                fetchRankings(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchRankings(boolean isRefresh) {
        if (isLoading) return;
        
        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            allUsers.clear();
            adapter.setUsers(allUsers, currentRuleType);
            rvRanking.setVisibility(View.GONE);
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        apiService.getRankings(currentRuleType, currentPage, 20).enqueue(new Callback<ApiResponse<PageData<RankingUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageData<RankingUser>>> call, Response<ApiResponse<PageData<RankingUser>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PageData<RankingUser> pageData = response.body().getData();
                    List<RankingUser> users = pageData.getContent();
                    
                    if (users != null && !users.isEmpty()) {
                        allUsers.addAll(users);
                        adapter.setUsers(allUsers, currentRuleType);
                        rvRanking.setVisibility(View.VISIBLE);
                        
                        if (isRefresh) {
                            buildChart(allUsers);
                        }
                    }
                    
                    isLastPage = (currentPage + 1) >= pageData.getTotalPages();
                    if (!isLastPage) {
                        currentPage++;
                    }
                    // Update load more button visibility
                    int totalElements = pageData.getTotalElements();
                    if (tvMemberCount != null) {
                        tvMemberCount.setText(totalElements + " thành viên");
                    }
                    if (btnLoadMore != null) {
                        btnLoadMore.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
                        btnLoadMore.setText("Tải thêm (Trang " + (currentPage + 1) + "/" + pageData.getTotalPages() + ")");
                    }
                } else {
                    Toast.makeText(getContext(), "Không tải được dữ liệu xếp hạng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageData<RankingUser>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Chart ---
    private void buildChart(List<RankingUser> users) {
        if (users == null || users.isEmpty() || barChartView == null || getContext() == null) return;
        
        int n = Math.min(users.size(), 10);
        int barColor = Color.parseColor("ACM".equals(currentRuleType) ? "#ffa116" : "#2cbb5d");
        
        List<BarChartView.BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            RankingUser u = users.get(i);
            double val = "ACM".equals(currentRuleType)
                ? (u.getSolvedCount() != null ? (double)u.getSolvedCount() : 0.0)
                : (u.getTotalScore() != null ? u.getTotalScore() : 0.0);
            entries.add(new BarChartView.BarEntry(u.getUsername(), val));
        }
        
        barChartView.setData(entries, barColor);
    }

    private void showDetailModal(RankingUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Chi tiết User: " + (user.getUsername() != null ? user.getUsername() : "N/A"));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Xếp hạng: #").append(user.getRank() != null ? user.getRank() : "?").append("\n\n");
        sb.append("Số bài đã giải (Solved): ").append(user.getSolvedCount() != null ? user.getSolvedCount() : 0).append("\n");
        sb.append("Số lượt chấp nhận (AC): ").append(user.getAcCount() != null ? user.getAcCount() : 0).append("\n");
        sb.append("Tổng lượt nộp (Submissions): ").append(user.getSubmissionCount() != null ? user.getSubmissionCount() : 0).append("\n\n");
        
        double total = user.getTotalScore() != null ? user.getTotalScore() : 0.0;
        sb.append("Tổng điểm (Score): ").append(total == (long)total ? String.format("%d", (long)total) : String.format("%.2f", total)).append("\n");
        
        int sub = user.getSubmissionCount() != null ? user.getSubmissionCount() : 0;
        int ac = user.getAcCount() != null ? user.getAcCount() : 0;
        double rate = sub > 0 ? ((double) ac / sub) * 100 : 0.0;
        sb.append("Tỉ lệ chấp nhận (Rating): ").append(String.format("%.2f%%", rate));
        
        builder.setMessage(sb.toString());
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }
}
