package com.booknest.campusridenest.util;

import com.google.firebase.firestore.GeoPoint;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate distance between two GeoPoints using Haversine formula
     * @return distance in kilometers
     */
    public static double calculateDistance(GeoPoint point1, GeoPoint point2) {
        if (point1 == null || point2 == null) {
            return Double.MAX_VALUE;
        }

        double lat1Rad = Math.toRadians(point1.getLatitude());
        double lat2Rad = Math.toRadians(point2.getLatitude());
        double deltaLat = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double deltaLon = Math.toRadians(point2.getLongitude() - point1.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Check if two routes are compatible based on distance thresholds
     */
    public static boolean areRoutesCompatible(
            GeoPoint requestPickup, GeoPoint requestDropoff,
            GeoPoint offerStart, GeoPoint offerEnd,
            double maxPickupDistanceKm, double maxDropoffDistanceKm) {

        double pickupDistance = calculateDistance(requestPickup, offerStart);
        double dropoffDistance = calculateDistance(requestDropoff, offerEnd);

        return pickupDistance <= maxPickupDistanceKm &&
                dropoffDistance <= maxDropoffDistanceKm;
    }
}