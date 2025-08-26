package com.vktech.citysaathi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// 1. IMPORT THE CORRECT CLASS
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.vktech.citysaathi.adapters.IssueAdapter;
import com.vktech.citysaathi.models.Issue;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IssueAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private RecyclerView issuesRecyclerView;
    private IssueAdapter issueAdapter;
    private List<Issue> issueList;
    private FirebaseFirestore db;
    private View emptyView; // Changed from TextView to View to support the new LinearLayout
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ListenerRegistration issueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        if (getSupportActionBar() != null && currentUser.getDisplayName() != null) {
            getSupportActionBar().setTitle("Welcome, " + currentUser.getDisplayName());
        }

        db = FirebaseFirestore.getInstance();
        issuesRecyclerView = findViewById(R.id.issuesRecyclerView);
        emptyView = findViewById(R.id.emptyView); // The ID now points to the LinearLayout
        issuesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        issueList = new ArrayList<>();
        issueAdapter = new IssueAdapter(issueList, this);
        issuesRecyclerView.setAdapter(issueAdapter);

        // 2. DECLARE THE VARIABLE WITH THE CORRECT CLASS
        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddIssue);
        fab.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ReportIssueActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserIssues();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (issueListener != null) {
            issueListener.remove();
        }
    }

    private void loadUserIssues() {
        Query query = db.collection("issues")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("submittedAt", Query.Direction.DESCENDING);

        issueListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }
            issueList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Issue issue = doc.toObject(Issue.class);
                    issue.setDocumentId(doc.getId()); // Ensure document ID is set for deletion
                    issueList.add(issue);
                }
            }
            issueAdapter.notifyDataSetChanged();
            updateEmptyViewVisibility();
        });
    }

    @Override
    public void onItemClick(Issue issue) {
        Intent intent = new Intent(MainActivity.this, IssueDetailActivity.class);
        intent.putExtra("selected_issue", issue);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Issue issue) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Issue")
                .setMessage("Are you sure you want to delete this issue?")
                .setPositiveButton("Delete", (dialog, which) -> deleteIssueFromFirestore(issue))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteIssueFromFirestore(Issue issue) {
        if (issue.getDocumentId() == null || issue.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Error: Cannot delete issue without an ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("issues").document(issue.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Issue deleted successfully.", Toast.LENGTH_SHORT).show();
                    // The snapshot listener will automatically update the list.
                    // However, for immediate feedback, manual removal is fine too.
                    int position = issueList.indexOf(issue);
                    if (position != -1) {
                        issueList.remove(position);
                        issueAdapter.notifyItemRemoved(position);
                        updateEmptyViewVisibility();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting issue.", Toast.LENGTH_SHORT).show());
    }

    private void updateEmptyViewVisibility() {
        if (issueList.isEmpty()) {
            issuesRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            issuesRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}