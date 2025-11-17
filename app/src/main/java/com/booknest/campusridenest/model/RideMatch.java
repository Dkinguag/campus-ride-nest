package com.booknest.campusridenest.model;

import java.io.Serializable;

public class RideMatch implements Serializable {
    public RideOffer offer;
    public double matchScore;           // 0-100
    public double pickupDistanceKm;
    public double dropoffDistanceKm;
    public long timeDifferenceMinutes;
    public String compatibilityReason;  // Why it's a good match

    public RideMatch(RideOffer offer, double matchScore, double pickupDistanceKm,
                     double dropoffDistanceKm, long timeDifferenceMinutes,
                     String compatibilityReason) {
        this.offer = offer;
        this.matchScore = matchScore;
        this.pickupDistanceKm = pickupDistanceKm;
        this.dropoffDistanceKm = dropoffDistanceKm;
        this.timeDifferenceMinutes = timeDifferenceMinutes;
        this.compatibilityReason = compatibilityReason;
    }
}
