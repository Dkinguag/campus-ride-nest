package com.booknest.campusridenest.services;

import com.booknest.campusridenest.model.RideMatch;
import com.booknest.campusridenest.model.RideOffer;
import com.booknest.campusridenest.model.RideRequest;
import com.booknest.campusridenest.util.GeoUtils;

import java.util.ArrayList;
import java.util.List;

public class RideMatchScorer {

    // Scoring weights (total = 100)
    private static final double WEIGHT_DISTANCE = 35.0;
    private static final double WEIGHT_TIME = 30.0;
    private static final double WEIGHT_PRICE = 20.0;
    private static final double WEIGHT_PREFERENCES = 15.0;

    // Distance thresholds
    private static final double MAX_PICKUP_DISTANCE_KM = 5.0;    // 5km
    private static final double MAX_DROPOFF_DISTANCE_KM = 5.0;   // 5km
    private static final double IDEAL_DISTANCE_KM = 1.0;          // 1km is ideal

    // Time thresholds
    private static final long MAX_TIME_DIFF_MINUTES = 120;       // 2 hours
    private static final long IDEAL_TIME_DIFF_MINUTES = 30;      // 30 minutes is ideal

    public RideMatch scoreMatch(RideRequest request, RideOffer offer) {
        // Calculate distances
        double pickupDistance = GeoUtils.calculateDistance(
                request.pickupLocation, offer.startLocation);
        double dropoffDistance = GeoUtils.calculateDistance(
                request.dropoffLocation, offer.endLocation);

        // Quick filters - incompatible rides get score of 0
        if (pickupDistance > MAX_PICKUP_DISTANCE_KM ||
                dropoffDistance > MAX_DROPOFF_DISTANCE_KM) {
            return new RideMatch(offer, 0.0, pickupDistance, dropoffDistance,
                    Long.MAX_VALUE, "Route too far apart");
        }

        long timeDiff = Math.abs(request.timeMillis - offer.timeMillis) / 60000; // minutes
        if (timeDiff > MAX_TIME_DIFF_MINUTES) {
            return new RideMatch(offer, 0.0, pickupDistance, dropoffDistance,
                    timeDiff, "Time difference too large");
        }

        // Price compatibility
        if (request.maxBudget > 0 && offer.pricePerSeat > request.maxBudget) {
            return new RideMatch(offer, 0.0, pickupDistance, dropoffDistance,
                    timeDiff, "Price exceeds budget");
        }

        // Calculate component scores
        double distanceScore = calculateDistanceScore(pickupDistance, dropoffDistance);
        double timeScore = calculateTimeScore(timeDiff);
        double priceScore = calculatePriceScore(request.maxBudget, offer.pricePerSeat);
        double preferenceScore = calculatePreferenceScore(request, offer);

        // Weighted total score
        double totalScore =
                (distanceScore * WEIGHT_DISTANCE +
                        timeScore * WEIGHT_TIME +
                        priceScore * WEIGHT_PRICE +
                        preferenceScore * WEIGHT_PREFERENCES) / 100.0;

        // Generate compatibility reason
        String reason = generateCompatibilityReason(
                distanceScore, timeScore, priceScore, preferenceScore);

        return new RideMatch(offer, totalScore, pickupDistance, dropoffDistance,
                timeDiff, reason);
    }

    private double calculateDistanceScore(double pickupDist, double dropoffDist) {
        double avgDistance = (pickupDist + dropoffDist) / 2.0;

        if (avgDistance <= IDEAL_DISTANCE_KM) {
            return 100.0;
        } else if (avgDistance >= MAX_PICKUP_DISTANCE_KM) {
            return 0.0;
        } else {
            // Linear decay from 100 to 0
            return 100.0 * (1.0 - (avgDistance - IDEAL_DISTANCE_KM) /
                    (MAX_PICKUP_DISTANCE_KM - IDEAL_DISTANCE_KM));
        }
    }

    private double calculateTimeScore(long timeDiffMinutes) {
        if (timeDiffMinutes <= IDEAL_TIME_DIFF_MINUTES) {
            return 100.0;
        } else if (timeDiffMinutes >= MAX_TIME_DIFF_MINUTES) {
            return 0.0;
        } else {
            // Linear decay from 100 to 0
            return 100.0 * (1.0 - (double)(timeDiffMinutes - IDEAL_TIME_DIFF_MINUTES) /
                    (MAX_TIME_DIFF_MINUTES - IDEAL_TIME_DIFF_MINUTES));
        }
    }

    private double calculatePriceScore(double maxBudget, double pricePerSeat) {
        if (maxBudget <= 0) {
            return 100.0; // No budget constraint
        }

        if (pricePerSeat <= maxBudget * 0.7) {
            return 100.0; // Great deal
        } else if (pricePerSeat <= maxBudget) {
            return 70.0;  // Within budget
        } else {
            return 0.0;   // Over budget
        }
    }

    private double calculatePreferenceScore(RideRequest request, RideOffer offer) {
        int matches = 0;
        int total = 0;

        // Smoking preference
        total++;
        if (!request.needsNonSmoking || !offer.allowsSmoking) {
            matches++;
        }

        // Pets preference
        total++;
        if (!request.needsNoPets || !offer.allowsPets) {
            matches++;
        }

        // Music preference
        if (request.musicPreference != null && offer.musicPreference != null &&
                !request.musicPreference.equals("no-preference") &&
                !offer.musicPreference.equals("no-preference")) {
            total++;
            if (request.musicPreference.equals(offer.musicPreference)) {
                matches++;
            }
        }

        // Conversation preference
        if (request.conversationLevel != null && offer.conversationLevel != null &&
                !request.conversationLevel.equals("no-preference") &&
                !offer.conversationLevel.equals("no-preference")) {
            total++;
            if (request.conversationLevel.equals(offer.conversationLevel)) {
                matches++;
            }
        }

        return total > 0 ? (100.0 * matches / total) : 100.0;
    }

    private String generateCompatibilityReason(double distScore, double timeScore,
                                               double priceScore, double prefScore) {
        List<String> reasons = new ArrayList<>();

        if (distScore >= 80) reasons.add("Very close route");
        else if (distScore >= 60) reasons.add("Nearby route");

        if (timeScore >= 80) reasons.add("Perfect timing");
        else if (timeScore >= 60) reasons.add("Good timing");

        if (priceScore >= 90) reasons.add("Great price");
        else if (priceScore >= 70) reasons.add("Fair price");

        if (prefScore >= 75) reasons.add("Matching preferences");

        if (reasons.isEmpty()) {
            return "Compatible match";
        }

        return String.join(" • ", reasons);
    }
}