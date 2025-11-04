package com.booknest.campusridenest.data.repo

import android.util.Log
import com.booknest.campusridenest.model.RideOffer
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class OfferRepository {

    private val collectionPath = "offers"
    private val db = Firebase.firestore
    private val col get() = db.collection(collectionPath)
    private val firestore = FirebaseFirestore.getInstance()

    @JvmOverloads
    fun createOfferAsync(
        ownerUid: String,
        from: String,
        to: String,
        dateTime: Long,
        seats: Int,
        status: String = "open"
    ): Task<String> {
        val resolvedOwner = if (ownerUid.trim().isEmpty()) {
            Firebase.auth.currentUser?.uid ?: ""
        } else ownerUid

        val now = System.currentTimeMillis()

        val payload = hashMapOf(
            "type" to "offer",
            "ownerUid" to resolvedOwner,
            "from" to from,
            "to" to to,
            "dateTime" to dateTime,
            "seats" to seats,
            "status" to status,
            "createdAt" to now,
            "updatedAt" to now
        )

        return col.add(payload).continueWith { it.result.id }
    }

    fun getOpenOffers(): Flow<List<RideOffer>> = callbackFlow {
        val reg = col
            .whereEqualTo("status", "open")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toRideOffer() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun getMyOffers(uid: String): Flow<List<RideOffer>> = callbackFlow {
        val reg = col
            .whereEqualTo("ownerUid", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toRideOffer() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // --- mapper used above ---
    private fun DocumentSnapshot.toRideOffer(): RideOffer? = try {
        val base = this.toObject(RideOffer::class.java)
        base?.apply { id = this@toRideOffer.id }
    } catch (_: Exception) {
        null
    }

    suspend fun updateOffer(offerId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            // Add server timestamp to updates
            val updatesWithTimestamp = updates.toMutableMap().apply {
                put("updatedAt", FieldValue.serverTimestamp())
            }

            firestore.collection("offers")
                .document(offerId)
                .update(updatesWithTimestamp)
                .await()

            Log.d("OfferRepository", "Successfully updated offer $offerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("OfferRepository", "Failed to update offer $offerId", e)
            Result.failure(e)
        }
    }

}
