package com.vktech.citysaathi;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vktech.citysaathi.models.Issue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminIssueDetailActivity extends AppCompatActivity {

    private Issue currentIssue;
    private FirebaseFirestore db;

    // UI Elements
    private TextView detailStatusTextView, detailTitleTextView, detailDescriptionTextView,
            detailCategoryTextView, detailLocationTextView, detailContactTextView,
            detailSubmittedByTextView, detailSubmittedAtTextView;
    private View detailStatusColorBar;
    private Button updateStatusButton;
    private View imageCardView;
    private ImageView detailIssueImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_detail);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get issue from intent
        currentIssue = (Issue) getIntent().getSerializableExtra("issue_object");

        // Initialize all views
        initializeViews();

        if (currentIssue != null) {
            populateUI();
        } else {
            Toast.makeText(this, "Error: Issue data not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        updateStatusButton.setOnClickListener(v -> showUpdateStatusDialog());
    }

    private void initializeViews() {
        detailStatusTextView = findViewById(R.id.detailStatusTextView);
        detailStatusColorBar = findViewById(R.id.detail_status_bar);
        updateStatusButton = findViewById(R.id.updateStatusButton);
        detailTitleTextView = findViewById(R.id.detailTitleTextView);
        detailDescriptionTextView = findViewById(R.id.detailDescriptionTextView);
        detailCategoryTextView = findViewById(R.id.detailCategoryTextView);
        detailLocationTextView = findViewById(R.id.detailLocationTextView);
        detailContactTextView = findViewById(R.id.detailContactTextView);
        detailSubmittedByTextView = findViewById(R.id.detailSubmittedByTextView);
        detailSubmittedAtTextView = findViewById(R.id.detailSubmittedAtTextView);
        imageCardView = findViewById(R.id.imageCardView);
        detailIssueImageView = findViewById(R.id.detailIssueImageView);
    }

    private void populateUI() {
        detailTitleTextView.setText(currentIssue.getTitle());
        detailDescriptionTextView.setText(currentIssue.getDescription());
        detailCategoryTextView.setText("Category: " + currentIssue.getCategory());
        detailSubmittedByTextView.setText(currentIssue.getUserName());

        if (currentIssue.getContactInfo() != null && !currentIssue.getContactInfo().isEmpty()) {
            detailContactTextView.setText(currentIssue.getContactInfo());
        } else {
            detailContactTextView.setText("Not Provided");
        }

        // UPDATED: Logic to display location
        // Prioritize showing the typed address
        if (currentIssue.getLocationAddress() != null && !currentIssue.getLocationAddress().isEmpty()) {
            detailLocationTextView.setText("Location: " + currentIssue.getLocationAddress());
        }
        // Fallback to coordinates
        else if (currentIssue.getLatitude() != 0.0 || currentIssue.getLongitude() != 0.0) {
            String locationString = String.format(Locale.getDefault(), "Location: Lat: %.4f, Lng: %.4f",
                    currentIssue.getLatitude(), currentIssue.getLongitude());
            detailLocationTextView.setText(locationString);
        }
        // If neither is available
        else {
            detailLocationTextView.setText("Location: Not Provided");
        }

        if (currentIssue.getSubmittedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault());
            detailSubmittedAtTextView.setText("Submitted: " + sdf.format(currentIssue.getSubmittedAt()));
        }

        // Logic to load and display the image if it exists
        if (currentIssue.getImageUrl() != null && !currentIssue.getImageUrl().isEmpty()) {
            imageCardView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentIssue.getImageUrl())
                    .into(detailIssueImageView);
        } else {
            imageCardView.setVisibility(View.GONE);
        }

        updateStatusUI(currentIssue.getStatus());
    }

    private void showUpdateStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_status, null);
        builder.setView(dialogView);

        AutoCompleteTextView statusAutoComplete = dialogView.findViewById(R.id.statusAutoComplete);
        String[] statuses = new String[]{"Pending", "In Queue", "Fixing", "Resolved"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        statusAutoComplete.setAdapter(adapter);
        statusAutoComplete.setText(currentIssue.getStatus(), false);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newStatus = statusAutoComplete.getText().toString();
            if (!newStatus.equals(currentIssue.getStatus())) {
                updateIssueStatus(newStatus);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateIssueStatus(String newStatus) {
        if (currentIssue.getDocumentId() == null) {
            Toast.makeText(this, "Error: Issue ID is missing. Cannot update.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", new Date());

        updateStatusButton.setEnabled(false); // Disable button during update

        db.collection("issues").document(currentIssue.getDocumentId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    currentIssue.setStatus(newStatus);
                    populateUI(); // Refresh the local UI
                    updateStatusButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating status. Please try again.", Toast.LENGTH_SHORT).show();
                    updateStatusButton.setEnabled(true);
                });
    }

    private void updateStatusUI(String statusText) {
        detailStatusTextView.setText(statusText);
        int color;
        if (statusText == null) statusText = "pending";
        switch (statusText.toLowerCase()) {
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
        detailStatusTextView.setTextColor(color);
        detailStatusColorBar.setBackgroundColor(color);
    }
}