package com.booknest.campusridenest.model;

import com.google.firebase.Timestamp;

/**
 * UserProfile - Model for user profile data
 *
 * Firestore collection: users/{uid}
 *
 * Fields:
 * - uid: String (Firebase Auth UID)
 * - displayName: String
 * - email: String
 * - emailVerified: Boolean
 * - activePosts: Number (count of open posts)
 * - closedPosts: Number (count of closed posts)
 * - createdAt: Timestamp
 *
 * Sprint 3 US-09: User profile view
 */
public class UserProfile {

    private String uid;
    private String displayName;
    private String email;
    private boolean emailVerified;
    private int activePosts;
    private int closedPosts;
    private Timestamp createdAt;

    // Required empty constructor for Firestore deserialization
    public UserProfile() {
    }

    // Full constructor
    public UserProfile(String uid, String displayName, String email,
                       boolean emailVerified, int activePosts, int closedPosts,
                       Timestamp createdAt) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.emailVerified = emailVerified;
        this.activePosts = activePosts;
        this.closedPosts = closedPosts;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public int getActivePosts() {
        return activePosts;
    }

    public void setActivePosts(int activePosts) {
        this.activePosts = activePosts;
    }

    public int getClosedPosts() {
        return closedPosts;
    }

    public void setClosedPosts(int closedPosts) {
        this.closedPosts = closedPosts;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get total post count
     */
    public int getTotalPosts() {
        return activePosts + closedPosts;
    }

    /**
     * Format member since date
     * Returns "Member since [Month Year]"
     */
    public String getMemberSinceFormatted() {
        if (createdAt == null) {
            return "Member";
        }

        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MMMM yyyy",
                java.util.Locale.getDefault());
        return "Member since " + format.format(createdAt.toDate());
    }
}