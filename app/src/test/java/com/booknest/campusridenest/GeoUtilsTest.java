package com.booknest.campusridenest;

import com.booknest.campusridenest.util.GeoUtils;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Unit tests for GeoUtils - Geographic Utility Functions.
 * Tests: TC-U05, TC-U12
 *
 * @author De Andre King
 * @version 1.0
 * @since October 2025
 */
@RunWith(JUnit4.class)
public class GeoUtilsTest {

    // Test coordinates
    private static final double STATE_COLLEGE_LAT = 40.7934;
    private static final double STATE_COLLEGE_LNG = -77.8600;
    private static final double PHILADELPHIA_LAT = 39.9526;
    private static final double PHILADELPHIA_LNG = -75.1652;
    private static final double PITTSBURGH_LAT = 40.4406;
    private static final double PITTSBURGH_LNG = -79.9959;
    private static final double HARRISBURG_LAT = 40.2732;
    private static final double HARRISBURG_LNG = -76.8867;

    // Known distances (approximate)
    private static final double STATE_COLLEGE_TO_PHILADELPHIA_KM = 234.0;
    private static final double STATE_COLLEGE_TO_PITTSBURGH_KM = 200.0;
    private static final double TOLERANCE_PERCENT = 0.05; // 5% tolerance

    // ============================================================
    // TC-U05: Haversine Distance Calculation
    // ============================================================

    @Test
    public void TC_U05_haversineDistance_stateCollegeToPhiladelphia() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint philadelphia = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        double distance = GeoUtils.calculateDistance(stateCollege, philadelphia);

        double expectedMin = STATE_COLLEGE_TO_PHILADELPHIA_KM * (1 - TOLERANCE_PERCENT);
        double expectedMax = STATE_COLLEGE_TO_PHILADELPHIA_KM * (1 + TOLERANCE_PERCENT);

        assertTrue("Distance should be approximately 234 km, got: " + distance + " km",
                distance >= expectedMin && distance <= expectedMax);
    }

    @Test
    public void TC_U05_haversineDistance_stateCollegeToPittsburgh() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint pittsburgh = new GeoPoint(PITTSBURGH_LAT, PITTSBURGH_LNG);

        double distance = GeoUtils.calculateDistance(stateCollege, pittsburgh);

        double expectedMin = STATE_COLLEGE_TO_PITTSBURGH_KM * (1 - TOLERANCE_PERCENT);
        double expectedMax = STATE_COLLEGE_TO_PITTSBURGH_KM * (1 + TOLERANCE_PERCENT);

        assertTrue("Distance should be approximately 200 km, got: " + distance + " km",
                distance >= expectedMin && distance <= expectedMax);
    }

    @Test
    public void TC_U05_haversineDistance_samePointReturnsZero() {
        GeoPoint point = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);

        double distance = GeoUtils.calculateDistance(point, point);

        assertEquals("Same point should return 0 distance", 0.0, distance, 0.001);
    }

    @Test
    public void TC_U05_haversineDistance_symmetric() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint philadelphia = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        double distanceAB = GeoUtils.calculateDistance(stateCollege, philadelphia);
        double distanceBA = GeoUtils.calculateDistance(philadelphia, stateCollege);

        assertEquals("Distance should be symmetric", distanceAB, distanceBA, 0.001);
    }

    @Test
    public void TC_U05_haversineDistance_shortDistance() {
        GeoPoint point1 = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint point2 = new GeoPoint(STATE_COLLEGE_LAT + 0.009, STATE_COLLEGE_LNG);

        double distance = GeoUtils.calculateDistance(point1, point2);

        assertTrue("Short distance should be approximately 1 km, got: " + distance,
                distance >= 0.5 && distance <= 1.5);
    }

    @Test
    public void TC_U05_haversineDistance_returnsPositive() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint pittsburgh = new GeoPoint(PITTSBURGH_LAT, PITTSBURGH_LNG);

        double distance = GeoUtils.calculateDistance(stateCollege, pittsburgh);

        assertTrue("Distance should always be positive", distance >= 0);
    }

    @Test
    public void TC_U05_haversineDistance_nullPoint1ReturnsMaxValue() {
        GeoPoint philadelphia = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        double distance = GeoUtils.calculateDistance(null, philadelphia);

        assertEquals("Null point1 should return MAX_VALUE", Double.MAX_VALUE, distance, 0.001);
    }

    @Test
    public void TC_U05_haversineDistance_nullPoint2ReturnsMaxValue() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);

        double distance = GeoUtils.calculateDistance(stateCollege, null);

        assertEquals("Null point2 should return MAX_VALUE", Double.MAX_VALUE, distance, 0.001);
    }

    @Test
    public void TC_U05_haversineDistance_bothNullReturnsMaxValue() {
        double distance = GeoUtils.calculateDistance(null, null);

        assertEquals("Both null should return MAX_VALUE", Double.MAX_VALUE, distance, 0.001);
    }

    @Test
    public void TC_U05_calculateDistance_triangleInequality() {
        GeoPoint stateCollege = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint harrisburg = new GeoPoint(HARRISBURG_LAT, HARRISBURG_LNG);
        GeoPoint philadelphia = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        double distAC = GeoUtils.calculateDistance(stateCollege, philadelphia);
        double distAB = GeoUtils.calculateDistance(stateCollege, harrisburg);
        double distBC = GeoUtils.calculateDistance(harrisburg, philadelphia);

        assertTrue("Triangle inequality should hold: AC <= AB + BC",
                distAC <= distAB + distBC + 0.1);
    }

    // ============================================================
    // Route Compatibility Tests
    // ============================================================

    @Test
    public void areRoutesCompatible_nearbyRoutes_returnsTrue() {
        GeoPoint requestPickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint requestDropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Offer with very close pickup and dropoff
        GeoPoint offerStart = new GeoPoint(STATE_COLLEGE_LAT + 0.01, STATE_COLLEGE_LNG + 0.01);
        GeoPoint offerEnd = new GeoPoint(PHILADELPHIA_LAT + 0.01, PHILADELPHIA_LNG + 0.01);

        boolean compatible = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                5.0, 5.0  // 5km thresholds
        );

        assertTrue("Nearby routes should be compatible", compatible);
    }

    @Test
    public void areRoutesCompatible_farRoutes_returnsFalse() {
        GeoPoint requestPickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint requestDropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Offer going to Pittsburgh instead (different destination)
        GeoPoint offerStart = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint offerEnd = new GeoPoint(PITTSBURGH_LAT, PITTSBURGH_LNG);

        boolean compatible = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                5.0, 5.0  // 5km thresholds
        );

        assertFalse("Routes to different cities should not be compatible", compatible);
    }

    @Test
    public void areRoutesCompatible_pickupTooFar_returnsFalse() {
        GeoPoint requestPickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint requestDropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Offer starting from Harrisburg (too far for pickup)
        GeoPoint offerStart = new GeoPoint(HARRISBURG_LAT, HARRISBURG_LNG);
        GeoPoint offerEnd = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        boolean compatible = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                5.0, 5.0  // 5km thresholds - Harrisburg is ~80km away
        );

        assertFalse("Pickup too far should not be compatible", compatible);
    }

    @Test
    public void areRoutesCompatible_dropoffTooFar_returnsFalse() {
        GeoPoint requestPickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint requestDropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Offer ending at Harrisburg instead of Philadelphia
        GeoPoint offerStart = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint offerEnd = new GeoPoint(HARRISBURG_LAT, HARRISBURG_LNG);

        boolean compatible = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                5.0, 5.0  // 5km thresholds
        );

        assertFalse("Dropoff too far should not be compatible", compatible);
    }

    @Test
    public void areRoutesCompatible_exactSameRoute_returnsTrue() {
        GeoPoint pickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint dropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        boolean compatible = GeoUtils.areRoutesCompatible(
                pickup, dropoff,
                pickup, dropoff,
                5.0, 5.0
        );

        assertTrue("Exact same route should be compatible", compatible);
    }

    @Test
    public void areRoutesCompatible_largerThreshold_moreFlexible() {
        GeoPoint requestPickup = new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG);
        GeoPoint requestDropoff = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Offer with pickup ~2km away
        GeoPoint offerStart = new GeoPoint(STATE_COLLEGE_LAT + 0.02, STATE_COLLEGE_LNG);
        GeoPoint offerEnd = new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG);

        // Should fail with 1km threshold
        boolean compatibleSmall = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                1.0, 1.0
        );

        // Should pass with 5km threshold
        boolean compatibleLarge = GeoUtils.areRoutesCompatible(
                requestPickup, requestDropoff,
                offerStart, offerEnd,
                5.0, 5.0
        );

        assertFalse("Should fail with small threshold", compatibleSmall);
        assertTrue("Should pass with larger threshold", compatibleLarge);
    }
}