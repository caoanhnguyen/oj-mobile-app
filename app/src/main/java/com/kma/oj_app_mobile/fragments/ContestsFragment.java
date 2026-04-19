package com.kma.oj_app_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.tabs.TabLayout;
import com.kma.oj_app_mobile.api.ApiClient;
import com.kma.oj_app_mobile.commons.ApiResponse;
import com.kma.oj_app_mobile.api.ApiService;
import com.kma.oj_app_mobile.models.Contest;
import com.kma.oj_app_mobile.adapters.ContestAdapter;
import com.kma.oj_app_mobile.commons.PageData;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.activities.ContestDetailActivity;

import android.content.Intent;

public class ContestsFragment extends Fragment {

    private RecyclerView rvContests;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private ContestAdapter adapter;
    private TabLayout tabLayout;
    
    private String currentStatusFilter = "ONGOING"; // default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contests, container, false);
        
        rvContests = view.findViewById(R.id.rvContests);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tabLayout = view.findViewById(R.id.tabLayout);

        rvContests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContestAdapter();
        
        adapter.setOnItemClickListener(contest -> {
            Intent intent = new Intent(getActivity(), ContestDetailActivity.class);
            intent.putExtra("CONTEST_KEY", contest.getContestKey());
            startActivity(intent);
        });
        
        rvContests.setAdapter(adapter);

        setupTabLayout();

        fetchContests();

        return view;
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Đang diễn ra"));
        tabLayout.addTab(tabLayout.newTab().setText("Sắp diễn ra"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã kết thúc"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatusFilter = "ONGOING";
                        break;
                    case 1:
                        currentStatusFilter = "UPCOMING";
                        break;
                    case 2:
                        currentStatusFilter = "ENDED";
                        break;
                }
                fetchContests();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                fetchContests();
            }
        });
    }

    private void fetchContests() {
        progressBar.setVisibility(View.VISIBLE);
        rvContests.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getClient(getContext()).create(ApiService.class);
        apiService.getContests(currentStatusFilter).enqueue(new Callback<ApiResponse<PageData<Contest>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageData<Contest>>> call, Response<ApiResponse<PageData<Contest>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Contest> contestList = response.body().getData().getContent();
                    adapter.setContests(contestList);
                    
                    if (contestList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvContests.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvContests.setVisibility(View.VISIBLE);
                    }
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageData<Contest>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
