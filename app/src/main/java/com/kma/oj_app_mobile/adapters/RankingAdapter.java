package com.kma.oj_app_mobile.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kma.oj_app_mobile.R;
import com.kma.oj_app_mobile.dto.RankingUser;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<RankingUser> users = new ArrayList<>();
    private String ruleType = "ACM";
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RankingUser user);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<RankingUser> users, String ruleType) {
        this.users = users;
        this.ruleType = ruleType;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankingUser user = users.get(position);
        
        holder.tvRank.setText(String.valueOf(user.getRank() != null ? user.getRank() : position + 1));
        holder.tvUsername.setText(user.getUsername());
        
        // Rank highlight
        int rank = position + 1;
        if (rank == 1) {
            holder.tvRank.setTextColor(Color.parseColor("#ffa116"));
            // Add a subtle background color using social chip
        } else if (rank == 2) {
            holder.tvRank.setTextColor(Color.parseColor("#eff2f6"));
        } else if (rank == 3) {
            holder.tvRank.setTextColor(Color.parseColor("#ad8a56"));
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#8a8a8a"));
        }

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            // Replace localhost with actual server IP from ApiClient (192.168.1.5) for physical devices
            String url = user.getAvatarUrl().replace("localhost", "192.168.1.5");
            Glide.with(holder.itemView.getContext())
                 .load(url)
                 .circleCrop()
                 .error(R.drawable.ic_profile)
                 .into(holder.imgAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                 .load(R.drawable.ic_profile)
                 .circleCrop()
                 .into(holder.imgAvatar);
        }

        if ("ACM".equals(ruleType)) {
            int val = user.getSolvedCount() != null ? user.getSolvedCount() : 0;
            holder.tvValue.setText(String.valueOf(val));
            holder.tvValue.setTextColor(Color.parseColor("#ffa116"));
        } else {
            double val = user.getTotalScore() != null ? user.getTotalScore() : 0.0;
            if (val == (long) val) {
                holder.tvValue.setText(String.format("%d", (long)val));
            } else {
                holder.tvValue.setText(String.format("%.1f", val));
            }
            holder.tvValue.setTextColor(Color.parseColor("#2cbb5d"));
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvValue;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvValue = itemView.findViewById(R.id.tvValue);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
