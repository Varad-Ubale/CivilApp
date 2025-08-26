package com.vktech.citysaathi.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vktech.citysaathi.R;
import com.vktech.citysaathi.models.Issue;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    private final List<Issue> issueList;
    private final OnItemClickListener listener;

    // Updated interface with long click handler
    public interface OnItemClickListener {
        void onItemClick(Issue issue);
        void onItemLongClick(Issue issue); // New method for long press
    }

    public IssueAdapter(List<Issue> issueList, OnItemClickListener listener) {
        this.issueList = issueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.bind(issue, listener);
    }

    @Override
    public int getItemCount() { return issueList.size(); }

    static class IssueViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, categoryTextView, statusTextView, dateTextView;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(final Issue issue, final OnItemClickListener listener) {
            titleTextView.setText(issue.getTitle());
            descriptionTextView.setText(issue.getDescription());
            categoryTextView.setText(issue.getCategory());
            statusTextView.setText(issue.getStatus());

            if (issue.getSubmittedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                dateTextView.setText(sdf.format(issue.getSubmittedAt()));
            }

            setStatusColor(issue.getStatus());

            // Set listeners
            itemView.setOnClickListener(v -> listener.onItemClick(issue));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(issue);
                return true; // Consume the long click event
            });
        }

        // UPDATED: New color scheme
        private void setStatusColor(String status) {
            int color;
            if (status == null) status = "pending";
            switch (status.toLowerCase()) {
                case "submitted":
                    color = Color.parseColor("#FF9800"); // Orange
                    break;
                case "pending":
                    color = Color.parseColor("#F44336"); // Red
                    break;
                case "in queue":
                case "in progress":
                case "fixing":
                    color = Color.parseColor("#2196F3"); // Blue
                    break;
                case "resolved":
                    color = Color.parseColor("#4CAF50"); // Green
                    break;
                default:
                    color = Color.parseColor("#F44336"); // Default to Red
                    break;
            }
            Drawable background = statusTextView.getBackground();
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background.mutate()).setColor(color);
            }
        }
    }
}