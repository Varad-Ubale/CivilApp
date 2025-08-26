package com.vktech.citysaathi.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vktech.citysaathi.R;
import com.vktech.citysaathi.models.Issue;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminIssueAdapter extends RecyclerView.Adapter<AdminIssueAdapter.AdminIssueViewHolder> {

    private final List<Issue> issueList;
    private final OnAdminIssueClickListener listener;

    public interface OnAdminIssueClickListener {
        void onIssueClicked(Issue issue);
    }

    public AdminIssueAdapter(List<Issue> issueList, OnAdminIssueClickListener listener) {
        this.issueList = issueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminIssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue_admin, parent, false);
        return new AdminIssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminIssueViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.bind(issue, listener);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    static class AdminIssueViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, categoryTextView, statusTextView, dateTextView, submittedByTextView;
        Button updateButton;

        public AdminIssueViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            submittedByTextView = itemView.findViewById(R.id.submittedByTextView);
            updateButton = itemView.findViewById(R.id.updateButton);
        }

        void bind(final Issue issue, final OnAdminIssueClickListener listener) {
            titleTextView.setText(issue.getTitle());
            categoryTextView.setText(issue.getCategory());
            statusTextView.setText(issue.getStatus());

            if (issue.getUserName() != null && !issue.getUserName().isEmpty()) {
                submittedByTextView.setText("By: " + issue.getUserName());
            } else {
                submittedByTextView.setText("By: Not available");
            }

            if (issue.getSubmittedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                dateTextView.setText(sdf.format(issue.getSubmittedAt()));
            }

            setStatusColor(issue.getStatus());
            itemView.setOnClickListener(v -> listener.onIssueClicked(issue));
            updateButton.setVisibility(View.GONE);
        }

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