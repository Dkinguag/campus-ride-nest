package com.booknest.campusridenest.services;

import android.util.Log;

import com.booknest.campusridenest.model.RideMatch;
import com.booknest.campusridenest.model.RideOffer;
import com.booknest.campusridenest.model.RideRequest;
import com.booknest.campusridenest.util.RideMatchScorer;  // UPDATED IMPORT
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RideMatchingService {

    private static final String TAG = "RideMatchingService";
    private final FirebaseFirestore db;
    private final RideMatchScorer scorer;

    public RideMatchingService() {
        this.db = FirebaseFirestore.getInstance();
        this.scorer = new RideMatchScorer();
    }
    public interface MatchCallback {
        void onMatchesFound(List<RideMatch> matches);
        void onError(String error);
    }

    public void findMatchesForRequest(RideRequest request, MatchCallback callback) {
        Log.d(TAG, "Finding matches for request: " + request.id);

        // Query all OPEN offers from Firestore
        db.collection("offers")
                .whereEqualTo("status", "open")  // CHANGED: "active" → "open"
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<RideMatch> matches = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            RideOffer offer = doc.toObject(RideOffer.class);
                            offer.id = doc.getId();

                            // Skip offers from the same user
                            if (offer.ownerUid != null && offer.ownerUid.equals(request.ownerUid)) {
                                continue;
                            }

                            // Score the match
                            RideMatch match = scorer.scoreMatch(request, offer);

                            // Only include matches with score > 0
                            if (match.matchScore > 0) {
                                matches.add(match);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing offer: " + doc.getId(), e);
                        }
                    }

                    // Sort by match score (highest first)
                    Collections.sort(matches, new Comparator<RideMatch>() {
                        @Override
                        public int compare(RideMatch m1, RideMatch m2) {
                            return Double.compare(m2.matchScore, m1.matchScore);
                        }
                    });

                    // Return top 10 matches
                    List<RideMatch> topMatches = matches.subList(
                            0, Math.min(10, matches.size()));

                    Log.d(TAG, "Returning " + topMatches.size() + " top matches");
                    callback.onMatchesFound(topMatches);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching rides", e);
                    callback.onError("Failed to fetch rides: " + e.getMessage());
                });
    }
}