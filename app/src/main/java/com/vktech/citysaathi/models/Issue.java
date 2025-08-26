package com.vktech.citysaathi.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
import java.util.Date;

public class Issue implements Serializable {

    @DocumentId
    private String documentId;
    private String title;
    private String description;
    private String category;
    private String status;
    private String contactInfo;
    private double latitude;
    private double longitude;
    private String locationAddress; // Added field for typed address
    private String userId;
    private String userName;
    private Date submittedAt;
    private Date updatedAt;
    private String imageUrl;

    public Issue() {}

    // Updated constructor
    public Issue(String title, String description, String category, String contactInfo, double latitude, double longitude, String locationAddress, String userId, String userName, String imageUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.contactInfo = contactInfo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress; // Set the address
        this.userId = userId;
        this.userName = userName;
        this.status = "Submitted";
        this.updatedAt = null;
        this.submittedAt = new Date();
        this.imageUrl = imageUrl;
    }

    // --- Getters and Setters ---

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}