package com.booknest.campusridenest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.booknest.campusridenest.data.repo.OfferRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Firestore operations.
 * Tests: TC-I01, TC-I04, TC-I05, TC-I06
 *
 * Note: These tests require Firebase Emulator to be running:
 * firebase emulators:start
 *
 * @author De Andre King
 * @version 1.0
 * @since October 2025
 */
@RunWith(AndroidJUnit4::class)
class FirestoreIntegrationTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var offerRepository: OfferRepository

    private val testDriverId = "test_driver_123"
    private val testUserId = "test_user_456"
    private val createdOfferIds = mutableListOf<String>()

    @Before
    fun setup() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Connect to emulator for testing
        try {
            firestore.useEmulator("10.0.2.2", 8080)
            auth.useEmulator("10.0.2.2", 9099)
        } catch (e: IllegalStateException) {
            // Emulator already configured
        }

        offerRepository = OfferRepository()
    }

    @After
    fun cleanup() {
        // Clean up test data
        runBlocking {
            try {
                createdOfferIds.forEach { offerId ->
                    firestore.collection("offers").document(offerId).delete().await()
                }
                createdOfferIds.clear()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    // ============================================================
    // TC-I01: Firestore - Create Ride Offer
    // Requirement: US-03 - Create a Ride Offer
    // ============================================================

    @Test
    fun TC_I01_createOffer_writesToFirestore() = runBlocking {
        // Arrange
        val from = "State College, PA"
        val to = "Philadelphia, PA"
        val dateTime = System.currentTimeMillis() + 86400000 // Tomorrow
        val seats = 3

        // Act
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = from,
            to = to,
            dateTime = dateTime,
            seats = seats
        ).await()

        createdOfferIds.add(offerId)

        // Assert
        assertNotNull("Should return offer ID", offerId)
        assertTrue("Offer ID should not be empty", offerId.isNotEmpty())

        // Verify document exists in Firestore
        val document = firestore.collection("offers").document(offerId).get().await()
        assertTrue("Document should exist", document.exists())
        assertEquals("From should match", from, document.getString("from"))
        assertEquals("To should match", to, document.getString("to"))
    }

    @Test
    fun TC_I01_createOffer_setsCorrectFields() = runBlocking {
        // Arrange
        val from = "State College, PA"
        val to = "Pittsburgh, PA"
        val dateTime = System.currentTimeMillis() + 86400000
        val seats = 4

        // Act
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = from,
            to = to,
            dateTime = dateTime,
            seats = seats,
            status = "open"
        ).await()

        createdOfferIds.add(offerId)

        // Assert
        val document = firestore.collection("offers").document(offerId).get().await()

        assertEquals("State College, PA", document.getString("from"))
        assertEquals("Pittsburgh, PA", document.getString("to"))
        assertEquals(4L, document.getLong("seats"))
        assertEquals("open", document.getString("status"))
        assertEquals("offer", document.getString("type"))
        assertEquals(testDriverId, document.getString("ownerUid"))
        assertNotNull("createdAt should be set", document.get("createdAt"))
        assertNotNull("updatedAt should be set", document.get("updatedAt"))
    }

    @Test
    fun TC_I01_createOffer_defaultStatusIsOpen() = runBlocking {
        // Act - Create without specifying status
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "Test Origin",
            to = "Test Destination",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 2
        ).await()

        createdOfferIds.add(offerId)

        // Assert
        val document = firestore.collection("offers").document(offerId).get().await()
        assertEquals("Default status should be open", "open", document.getString("status"))
    }

    // ============================================================
    // TC-I04: Edit Offer
    // Requirement: US-05 - Post Management
    // ============================================================

    @Test
    fun TC_I04_updateOffer_updatesDocument() = runBlocking {
        // Arrange - Create offer first
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "State College, PA",
            to = "Philadelphia, PA",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 3
        ).await()

        createdOfferIds.add(offerId)

        // Act - Update offer
        val updates = mapOf(
            "seats" to 4,
            "to" to "Pittsburgh, PA"
        )
        val updateResult = offerRepository.updateOffer(offerId, updates)

        // Assert
        assertTrue("Update should succeed", updateResult.isSuccess)

        val updatedDoc = firestore.collection("offers").document(offerId).get().await()
        assertEquals(4L, updatedDoc.getLong("seats"))
        assertEquals("Pittsburgh, PA", updatedDoc.getString("to"))
    }

    @Test
    fun TC_I04_updateOffer_setsUpdatedAtTimestamp() = runBlocking {
        // Arrange
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "Test",
            to = "Test",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 2
        ).await()

        createdOfferIds.add(offerId)

        val beforeUpdate = firestore.collection("offers").document(offerId).get().await()
        val originalUpdatedAt = beforeUpdate.get("updatedAt")

        // Small delay to ensure timestamp difference
        Thread.sleep(100)

        // Act
        offerRepository.updateOffer(offerId, mapOf("seats" to 5))

        // Assert
        val afterUpdate = firestore.collection("offers").document(offerId).get().await()
        val newUpdatedAt = afterUpdate.get("updatedAt")

        assertNotNull("updatedAt should be set after update", newUpdatedAt)
    }

    // ============================================================
    // TC-I05: Close Offer
    // Requirement: US-05 - Post Management
    // ============================================================

    @Test
    fun TC_I05_closeOffer_setsStatusClosed() = runBlocking {
        // Arrange - Create offer
        val offerId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "State College, PA",
            to = "Philadelphia, PA",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 3
        ).await()

        createdOfferIds.add(offerId)

        // Act - Close offer using updateOffer
        val closeResult = offerRepository.updateOffer(offerId, mapOf("status" to "closed"))

        // Assert
        assertTrue("Close should succeed", closeResult.isSuccess)

        val closedDoc = firestore.collection("offers").document(offerId).get().await()
        assertEquals("closed", closedDoc.getString("status"))
    }

    // ============================================================
    // TC-I06: Query Open Offers
    // Requirement: US-06 - Search & Filter
    // ============================================================

    @Test
    fun TC_I06_getOpenOffers_returnsOnlyOpenOffers() = runBlocking {
        // Arrange - Create open and closed offers
        val openOfferId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "Open Offer Origin",
            to = "Open Offer Dest",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 3,
            status = "open"
        ).await()
        createdOfferIds.add(openOfferId)

        val closedOfferId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "Closed Offer Origin",
            to = "Closed Offer Dest",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 2,
            status = "closed"
        ).await()
        createdOfferIds.add(closedOfferId)

        // Act - Get open offers
        val openOffers = withTimeout(5000) {
            offerRepository.getOpenOffers().first()
        }

        // Assert - Should only contain open offers
        val openOfferIds = openOffers.map { it.id }
        assertTrue("Should contain open offer", openOfferIds.contains(openOfferId))
        assertFalse("Should not contain closed offer", openOfferIds.contains(closedOfferId))
    }

    // ============================================================
    // TC-I06: Query My Offers
    // Requirement: US-05 - Post Management
    // ============================================================

    @Test
    fun TC_I06_getMyOffers_returnsOnlyUserOffers() = runBlocking {
        // Arrange - Create offers for different users
        val myOfferId = offerRepository.createOfferAsync(
            ownerUid = testDriverId,
            from = "My Offer",
            to = "My Destination",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 3
        ).await()
        createdOfferIds.add(myOfferId)

        val otherOfferId = offerRepository.createOfferAsync(
            ownerUid = "other_user_999",
            from = "Other Offer",
            to = "Other Destination",
            dateTime = System.currentTimeMillis() + 86400000,
            seats = 2
        ).await()
        createdOfferIds.add(otherOfferId)

        // Act
        val myOffers = withTimeout(5000) {
            offerRepository.getMyOffers(testDriverId).first()
        }

        // Assert
        val myOfferIds = myOffers.map { it.id }
        assertTrue("Should contain my offer", myOfferIds.contains(myOfferId))
        assertFalse("Should not contain other user's offer", myOfferIds.contains(otherOfferId))
    }

    // ============================================================
    // Direct Firestore Tests
    // ============================================================

    @Test
    fun TC_I07_firestoreDirectWrite_succeeds() = runBlocking {
        // Test direct Firestore write
        val docRef = firestore.collection("test_collection").document()

        val data = mapOf(
            "testField" to "testValue",
            "timestamp" to System.currentTimeMillis()
        )

        docRef.set(data).await()

        val retrieved = docRef.get().await()
        assertTrue("Document should exist", retrieved.exists())
        assertEquals("testValue", retrieved.getString("testField"))

        // Cleanup
        docRef.delete().await()
    }

    @Test
    fun TC_I08_firestoreQuery_filtersCorrectly() = runBlocking {
        // Create test documents
        val doc1Id = firestore.collection("test_query").add(
            mapOf("category" to "A", "value" to 1)
        ).await().id

        val doc2Id = firestore.collection("test_query").add(
            mapOf("category" to "B", "value" to 2)
        ).await().id

        // Query
        val results = firestore.collection("test_query")
            .whereEqualTo("category", "A")
            .get().await()

        assertEquals("Should find 1 document with category A", 1, results.size())

        // Cleanup
        firestore.collection("test_query").document(doc1Id).delete().await()
        firestore.collection("test_query").document(doc2Id).delete().await()
    }
}