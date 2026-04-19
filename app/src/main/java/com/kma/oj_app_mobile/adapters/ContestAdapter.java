package com.kma.oj_app_mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.models.Contest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ContestAdapter extends RecyclerView.Adapter<ContestAdapter.ContestViewHolder> {

    private List<Contest> contests = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contest contest);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setContests(List<Contest> contests) {
        this.contests = contests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contest, parent, false);
        return new ContestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContestViewHolder holder, int position) {
        Contest contest = contests.get(position);
        holder.bind(contest);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contests != null ? contests.size() : 0;
    }

    static class ContestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvStatus;
        private final TextView tvTime;
        private final TextView tvDuration;
        private final TextView tvRuleType;

        public ContestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvRuleType = itemView.findViewById(R.id.tvRuleType);
        }

        public void bind(Contest contest) {
            tvTitle.setText(contest.getTitle() != null ? contest.getTitle() : "Untitled Contest");
            
            String startTime = formatTime(contest.getStartTime());
            String endTime = formatTime(contest.getEndTime());
            tvTime.setText(startTime + " - " + endTime);

            if (contest.getDurationMinutes() != null) {
                tvDuration.setText("Thời gian: " + contest.getDurationMinutes() + " phút");
            } else {
                tvDuration.setText("");
            }

            if (contest.getRuleType() != null) {
                String rule = contest.getRuleType();
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
            } else {
                tvRuleType.setText("CUSTOM");
                tvRuleType.setBackgroundResource(R.drawable.bg_badge_finished);
                tvRuleType.setTextColor(Color.parseColor("#9E9E9E"));
            }

            // Handle status badge styling based on contestStatus
            String status = contest.getContestStatus() != null ? contest.getContestStatus() : "UNKNOWN";
            tvStatus.setText(status);
            
            switch(status.toUpperCase()) {
                case "ACTIVE":
                case "ONGOING":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
                    tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                    tvStatus.setText("ONGOING");
                    break;
                case "UPCOMING":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_upcoming);
                    tvStatus.setTextColor(Color.parseColor("#F57C00"));
                    break;
                case "FINISHED":
                case "ENDED":
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_finished);
                    tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                    tvStatus.setText("ENDED");
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_finished);
                    tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
                    break;
            }
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
    }
}
