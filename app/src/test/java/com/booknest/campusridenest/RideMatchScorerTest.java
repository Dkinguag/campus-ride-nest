package com.booknest.campusridenest;

import com.booknest.campusridenest.model.RideMatch;
import com.booknest.campusridenest.model.RideOffer;
import com.booknest.campusridenest.model.RideRequest;
import com.booknest.campusridenest.util.RideMatchScorer;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Unit tests for RideMatchScorer - Smart Matching Algorithm.
 * Tests: TC-U03, TC-U04, TC-U06, TC-U07, TC-U09, TC-U10
 *
 * @author De Andre King
 * @version 1.0
 * @since November 2025
 */
@RunWith(JUnit4.class)
public class RideMatchScorerTest {

    private RideMatchScorer scorer;

    // Test coordinates
    private static final double STATE_COLLEGE_LAT = 40.7934;
    private static final double STATE_COLLEGE_LNG = -77.8600;
    private static final double PHILADELPHIA_LAT = 39.9526;
    private static final double PHILADELPHIA_LNG = -75.1652;
    private static final double PITTSBURGH_LAT = 40.4406;
    private static final double PITTSBURGH_LNG = -79.9959;

    // Test times (in milliseconds)
    private static final long BASE_TIME = System.currentTimeMillis() + 86400000;
    private static final long FIFTEEN_MINUTES = 15 * 60 * 1000;
    private static final long THIRTY_MINUTES = 30 * 60 * 1000;
    private static final long FORTY_FIVE_MINUTES = 45 * 60 * 1000;
    private static final long THREE_HOURS = 3 * 60 * 60 * 1000;

    @Before
    public void setup() {
        scorer = new RideMatchScorer();
    }

    // ============================================================
    // TC-U03: Perfect Match Calculation
    // ============================================================

    @Test
    public void TC_U03_perfectMatch_returnsHighScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);

        assertTrue("Perfect match should return score >= 0.85, got: " + match.matchScore,
                match.matchScore >= 0.85);
    }

    @Test
    public void TC_U03_perfectMatch_allPreferencesMatch() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 40.0, false, false, "user1"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 20.0, false, false, "driver1"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("Score should be >= 0.85 for perfect match, got: " + match.matchScore,
                match.matchScore >= 0.85);
    }

    // ============================================================
    // TC-U04: Time Out of Range
    // ============================================================

    @Test
    public void TC_U04_timeOutOfRange_returnsZeroScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME + THREE_HOURS, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);

        assertEquals("Time difference > 120 min should return 0 score", 0.0, match.matchScore, 0.001);
    }

    @Test
    public void TC_U04_timeWithinRange_returnsGoodScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME + FIFTEEN_MINUTES, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("Time within 30 min should have good score, got: " + match.matchScore,
                match.matchScore > 0.0);
    }

    @Test
    public void TC_U04_exactTimeMatch_maximizesTimeScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("Exact time match should yield high score, got: " + match.matchScore,
                match.matchScore >= 0.80);
    }

    // ============================================================
    // TC-U06: Route Too Far
    // ============================================================

    @Test
    public void TC_U06_routeTooFar_returnsZeroScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PITTSBURGH_LAT, PITTSBURGH_LNG),
                BASE_TIME, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertEquals("Route to different city should return 0", 0.0, match.matchScore, 0.001);
    }

    @Test
    public void TC_U06_nearbyRoute_returnsGoodScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT + 0.005, STATE_COLLEGE_LNG + 0.005),
                new GeoPoint(PHILADELPHIA_LAT + 0.005, PHILADELPHIA_LNG + 0.005),
                BASE_TIME, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("Nearby route should score well, got: " + match.matchScore,
                match.matchScore >= 0.70);
    }

    // ============================================================
    // TC-U07: Price Over Budget
    // ============================================================

    @Test
    public void TC_U07_priceOverBudget_returnsZeroScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 30.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertEquals("Price over budget should return 0", 0.0, match.matchScore, 0.001);
    }

    @Test
    public void TC_U07_priceWithinBudget_acceptable() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("Price within budget should be acceptable", match.matchScore > 0.0);
    }

    @Test
    public void TC_U07_noBudgetConstraint_acceptable() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 0.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 100.0, false, false, "driver456"
        );

        RideMatch match = scorer.scoreMatch(request, offer);
        assertTrue("No budget constraint should allow any price", match.matchScore > 0.0);
    }

    // ============================================================
    // TC-U09: Preference Matching
    // ============================================================

    @Test
    public void TC_U09_matchingPreferences_boostsScore() {
        RideRequest requestMatch = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user1"
        );

        RideOffer offerMatch = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver1"
        );

        RideRequest requestMismatch = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, true, false, "user2"
        );

        RideOffer offerMismatch = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, true, false, "driver2"
        );

        RideMatch matchGood = scorer.scoreMatch(requestMatch, offerMatch);
        RideMatch matchBad = scorer.scoreMatch(requestMismatch, offerMismatch);

        assertTrue("Matching preferences should have higher or equal score",
                matchGood.matchScore >= matchBad.matchScore);
    }

    // ============================================================
    // TC-U10: Rank Ordering
    // ============================================================

    @Test
    public void TC_U10_rankOrdering_descendingByScore() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer perfectOffer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver1"
        );

        RideOffer goodOffer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME + FORTY_FIVE_MINUTES, 30.0, false, false, "driver2"
        );

        RideOffer fairOffer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT + 0.02, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME + THIRTY_MINUTES, 35.0, true, false, "driver3"
        );

        RideMatch perfectMatch = scorer.scoreMatch(request, perfectOffer);
        RideMatch goodMatch = scorer.scoreMatch(request, goodOffer);
        RideMatch fairMatch = scorer.scoreMatch(request, fairOffer);

        assertTrue("Perfect should score >= good",
                perfectMatch.matchScore >= goodMatch.matchScore);
        assertTrue("Good should score >= fair",
                goodMatch.matchScore >= fairMatch.matchScore);
    }

    @Test
    public void TC_U10_scoresAreInValidRange() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver1"
        );

        RideMatch match = scorer.scoreMatch(request, offer);

        assertTrue("Score should be >= 0", match.matchScore >= 0.0);
        assertTrue("Score should be <= 1", match.matchScore <= 1.0);
    }

    @Test
    public void TC_U10_matchContainsReasonString() {
        RideRequest request = createTestRequest(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 50.0, false, false, "user123"
        );

        RideOffer offer = createTestOffer(
                new GeoPoint(STATE_COLLEGE_LAT, STATE_COLLEGE_LNG),
                new GeoPoint(PHILADELPHIA_LAT, PHILADELPHIA_LNG),
                BASE_TIME, 25.0, false, false, "driver1"
        );

        RideMatch match = scorer.scoreMatch(request, offer);

        assertNotNull("Match should have a reason", match.compatibilityReason);
        assertFalse("Reason should not be empty", match.compatibilityReason.isEmpty());
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private RideRequest createTestRequest(GeoPoint pickupLocation,
                                          GeoPoint dropoffLocation,
                                          long timeMillis,
                                          double maxBudget,
                                          boolean needsNonSmoking,
                                          boolean needsNoPets,
                                          String ownerUid) {
        RideRequest request = new RideRequest();
        request.pickupLocation = pickupLocation;
        request.dropoffLocation = dropoffLocation;
        request.timeMillis = timeMillis;
        request.maxBudget = maxBudget;
        request.needsNonSmoking = needsNonSmoking;
        request.needsNoPets = needsNoPets;
        request.ownerUid = ownerUid;
        request.musicPreference = "no-preference";
        request.conversationLevel = "no-preference";
        return request;
    }

    private RideOffer createTestOffer(GeoPoint startLocation,
                                      GeoPoint endLocation,
                                      long timeMillis,
                                      double pricePerSeat,
                                      boolean allowsSmoking,
                                      boolean allowsPets,
                                      String ownerUid) {
        RideOffer offer = new RideOffer();
        offer.startLocation = startLocation;
        offer.endLocation = endLocation;
        offer.timeMillis = timeMillis;
        offer.pricePerSeat = pricePerSeat;
        offer.allowsSmoking = allowsSmoking;
        offer.allowsPets = allowsPets;
        offer.ownerUid = ownerUid;
        offer.musicPreference = "no-preference";
        offer.conversationLevel = "no-preference";
        offer.seats = 3;
        return offer;
    }
}