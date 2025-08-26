package com.vktech.citysaathi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vktech.citysaathi.adapters.AdminIssueAdapter;
import com.vktech.citysaathi.models.Issue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardActivity extends AppCompatActivity implements AdminIssueAdapter.OnAdminIssueClickListener {

    private static final String TAG = "AdminDashboardActivity";
    private RecyclerView adminIssuesRecyclerView;
    private AdminIssueAdapter adminIssueAdapter;
    private List<Issue> issueList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UPDATED: Add TextView for the new "Submitted" card
    private TextView totalCountTv, submittedCountTv, pendingCountTv, inProgressCountTv, resolvedCountTv;
    private ListenerRegistration issueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // UPDATED: Initialize all TextViews for the cards
        totalCountTv = findViewById(R.id.totalIssuesCount);
        submittedCountTv = findViewById(R.id.submitted);
        pendingCountTv = findViewById(R.id.pendingIssuesCount);
        inProgressCountTv = findViewById(R.id.inProgressIssuesCount);
        resolvedCountTv = findViewById(R.id.resolvedIssuesCount);

        adminIssuesRecyclerView = findViewById(R.id.adminIssuesRecyclerView);
        adminIssuesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        issueList = new ArrayList<>();
        adminIssueAdapter = new AdminIssueAdapter(issueList, this);
        adminIssuesRecyclerView.setAdapter(adminIssueAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAllIssues();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (issueListener != null) {
            issueListener.remove();
        }
    }

    private void loadAllIssues() {
        Query query = db.collection("issues").orderBy("submittedAt", Query.Direction.DESCENDING);
        issueListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            issueList.clear();
            // UPDATED: Add a counter for "submitted" issues
            int submittedCount = 0;
            int pendingCount = 0;
            int inProgressCount = 0;
            int resolvedCount = 0;

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Issue issue = doc.toObject(Issue.class);
                    issueList.add(issue);

                    // UPDATED: Expanded logic to categorize all statuses
                    if (issue.getStatus() != null) {
                        String status = issue.getStatus().toLowerCase();
                        switch (status) {
                            case "submitted":
                                submittedCount++;
                                break;
                            case "pending":
                                pendingCount++;
                                break;
                            case "in queue":
                            case "fixing":
                            case "in progress":
                                inProgressCount++;
                                break;
                            case "resolved":
                                resolvedCount++;
                                break;
                        }
                    } else {
                        // If status is null, consider it as submitted
                        submittedCount++;
                    }
                }
            }

            adminIssueAdapter.notifyDataSetChanged();
            // UPDATED: Pass the new count to the update method
            updateCounts(issueList.size(), submittedCount, pendingCount, inProgressCount, resolvedCount);
        });
    }

    // UPDATED: Method signature to accept the new "submitted" count
    private void updateCounts(int total, int submitted, int pending, int inProgress, int resolved) {
        totalCountTv.setText(String.format(Locale.getDefault(), "%d", total));
        submittedCountTv.setText(String.format(Locale.getDefault(), "%d", submitted)); // Set text for new card
        pendingCountTv.setText(String.format(Locale.getDefault(), "%d", pending));
        inProgressCountTv.setText(String.format(Locale.getDefault(), "%d", inProgress));
        resolvedCountTv.setText(String.format(Locale.getDefault(), "%d", resolved));
    }

    @Override
    public void onIssueClicked(Issue issue) {
        Intent intent = new Intent(this, AdminIssueDetailActivity.class);
        intent.putExtra("issue_object", issue);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}