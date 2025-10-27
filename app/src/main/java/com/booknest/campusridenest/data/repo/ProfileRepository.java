package com.booknest.campusridenest.data.repo;

import com.booknest.campusridenest.model.UserProfile;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * ProfileRepository - Manages user profile data in Firestore
 *
 * Collection: users/{uid}
 *
 * Features:
 * - Get user profile by UID
 * - Update user profile (display name)
 * - Real-time snapshot listeners for profile updates
 * - Post count synchronization
 *
 * Sprint 3 US-09: User profile view
 */
public class ProfileRepository {

    private static final String COLLECTION_USERS = "users";

    private final FirebaseFirestore db;

    // Singleton pattern
    private static ProfileRepository instance;

    private ProfileRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized ProfileRepository getInstance() {
        if (instance == null) {
            instance = new ProfileRepository();
        }
        return instance;
    }

    /**
     * Get user profile by UID
     * @return Task with DocumentSnapshot
     */
    public Task<DocumentSnapshot> getProfile(String uid) {
        return db.collection(COLLECTION_USERS)
                .document(uid)
                .get();
    }

    /**
     * Create new user profile
     * @return Task for tracking completion
     */
    public Task<Void> createProfile(UserProfile profile) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("uid", profile.getUid());
        profileData.put("displayName", profile.getDisplayName());
        profileData.put("email", profile.getEmail());
        profileData.put("emailVerified", profile.isEmailVerified());
        profileData.put("activePosts", 0);
        profileData.put("closedPosts", 0);
        profileData.put("createdAt", FieldValue.serverTimestamp());

        return db.collection(COLLECTION_USERS)
                .document(profile.getUid())
                .set(profileData);
    }

    /**
     * Update user profile
     * Allows updating display name only (email changes require re-authentication)
     * @return Task for tracking completion
     */
    public Task<Void> updateProfile(String uid, Map<String, Object> updates) {
        return db.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }

    /**
     * Update display name
     * Convenience method for most common profile update
     * @return Task for tracking completion
     */
    public Task<Void> updateDisplayName(String uid, String displayName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        return updateProfile(uid, updates);
    }

    /**
     * Increment active post count
     * Called when user creates a new post
     * @return Task for tracking completion
     */
    public Task<Void> incrementActivePosts(String uid) {
        return db.collection(COLLECTION_USERS)
                .document(uid)
                .update("activePosts", FieldValue.increment(1));
    }

    /**
     * Decrement active posts and increment closed posts
     * Called when user closes a post
     * @return Task for tracking completion
     */
    public Task<Void> closePost(String uid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("activePosts", FieldValue.increment(-1));
        updates.put("closedPosts", FieldValue.increment(1));

        return db.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }

    /**
     * Add snapshot listener for real-time profile updates
     * Use this in ProfileActivity to observe changes
     */
    public void addProfileListener(String uid,
                                   com.google.firebase.firestore.EventListener<DocumentSnapshot> listener) {
        db.collection(COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener(listener);
    }
}