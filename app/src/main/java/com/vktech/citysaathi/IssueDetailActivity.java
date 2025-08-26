package com.vktech.citysaathi;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.vktech.citysaathi.models.Issue;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IssueDetailActivity extends AppCompatActivity {

    private TextView title, status, description, category, location, contact, submittedAt;
    private View userImageContainer;
    private ImageView userDetailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        // Initialize views
        title = findViewById(R.id.detailTitleTextView);
        status = findViewById(R.id.detailStatusTextView);
        description = findViewById(R.id.detailDescriptionTextView);
        category = findViewById(R.id.detailCategoryTextView);
        location = findViewById(R.id.detailLocationTextView);
        contact = findViewById(R.id.detailContactTextView);
        submittedAt = findViewById(R.id.detailSubmittedAtTextView);
        userImageContainer = findViewById(R.id.userImageContainer);
        userDetailImageView = findViewById(R.id.userDetailImageView);

        // Get the Issue object from the intent
        Intent intent = getIntent();
        Issue issue = (Issue) intent.getSerializableExtra("selected_issue");

        // Populate the UI if the issue is not null
        if (issue != null) {
            populateUI(issue);
        } else {
            Toast.makeText(this, "Error: Issue data not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateUI(Issue issue) {
        title.setText(issue.getTitle());
        status.setText(issue.getStatus());
        description.setText(issue.getDescription());
        category.setText(issue.getCategory());

        if (issue.getContactInfo() != null && !issue.getContactInfo().isEmpty()) {
            contact.setText(issue.getContactInfo());
        } else {
            contact.setText("Not Provided");
        }

        // UPDATED: Logic to display location
        // Prioritize showing the typed address
        if (issue.getLocationAddress() != null && !issue.getLocationAddress().isEmpty()) {
            location.setText(issue.getLocationAddress());
        }
        // Fallback to coordinates if address is not available but GPS was used
        else if (issue.getLatitude() != 0.0 || issue.getLongitude() != 0.0) {
            String locationString = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f",
                    issue.getLatitude(), issue.getLongitude());
            location.setText(locationString);
        }
        // If neither is available
        else {
            location.setText("Not Provided");
        }


        // Format timestamp
        if (issue.getSubmittedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault());
            submittedAt.setText("Submitted: " + sdf.format(issue.getSubmittedAt()));
        }

        // Logic to load and display the image if it exists
        if (issue.getImageUrl() != null && !issue.getImageUrl().isEmpty()) {
            userImageContainer.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(issue.getImageUrl())
                    .into(userDetailImageView);
        } else {
            userImageContainer.setVisibility(View.GONE);
        }

        // Set status color
        setStatusColor(issue.getStatus());
    }

    private void setStatusColor(String statusText) {
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
        Drawable background = status.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background.mutate()).setColor(color);
        }
    }
}