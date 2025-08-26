package com.vktech.citysaathi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vktech.citysaathi.models.Issue;
import java.util.Locale;

public class ReportIssueActivity extends AppCompatActivity {

    private static final String TAG = "ReportIssueActivity";

    private TextInputEditText titleEditText, descriptionEditText, locationEditText, contactEditText, imageUrlEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private Button submitButton, gpsButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        setupLaunchers();
        setupListeners();
    }

    private void initializeViews() {
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        contactEditText = findViewById(R.id.contactEditText);
        categoryAutoComplete = findViewById(R.id.categoryAutoComplete);
        submitButton = findViewById(R.id.submitButton);
        gpsButton = findViewById(R.id.gpsButton);
        imageUrlEditText = findViewById(R.id.imageUrlEditText);
    }

    private void setupLaunchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) { getCurrentLocation(); }
            else { Toast.makeText(this, "Location permission denied.", Toast.LENGTH_LONG).show(); }
        });
    }

    private void setupListeners() {
        String[] categories = getResources().getStringArray(R.array.issue_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);

        gpsButton.setOnClickListener(v -> requestLocationPermission());
        submitButton.setOnClickListener(v -> submitIssue());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                String coordinates = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", currentLatitude, currentLongitude);
                locationEditText.setText(coordinates);
                Toast.makeText(this, "Location fetched successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Could not get location. Please ensure GPS is enabled.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void submitIssue() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        String locationText = locationEditText.getText().toString().trim();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(category) || TextUtils.isEmpty(locationText)) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        Toast.makeText(this, "Submitting...", Toast.LENGTH_LONG).show();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in.", Toast.LENGTH_SHORT).show();
            submitButton.setEnabled(true);
            return;
        }

        String contact = contactEditText.getText().toString().trim();
        String imageUrl = imageUrlEditText.getText().toString().trim();

        // UPDATED: Get the manually typed location text
        String locationAddress = locationEditText.getText().toString().trim();

        // UPDATED: Pass the locationAddress to the Issue constructor
        Issue newIssue = new Issue(title, description, category, contact, currentLatitude, currentLongitude, locationAddress, currentUser.getUid(), currentUser.getDisplayName(), imageUrl);

        db.collection("issues").add(newIssue)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Issue reported successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReportIssueActivity.this, "Error reporting issue: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(true);
                });
    }
}