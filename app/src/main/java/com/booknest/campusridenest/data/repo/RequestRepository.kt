package com.booknest.campusridenest.data.repo

import com.booknest.campusridenest.model.RideRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class RequestRepository {

    private val collectionPath = "requests"
    private val db = Firebase.firestore
    private val col get() = db.collection(collectionPath)

    @JvmOverloads
    fun createRequestAsync(
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
            "type" to "request",
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

    fun getOpenRequests(): Flow<List<RideRequest>> = callbackFlow {
        val reg = col
            .whereEqualTo("status", "open")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toRideRequest() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun getMyRequests(uid: String): Flow<List<RideRequest>> = callbackFlow {
        val reg = col
            .whereEqualTo("ownerUid", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toRideRequest() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // --- mapper used above ---
    private fun DocumentSnapshot.toRideRequest(): RideRequest? = try {
        val base = this.toObject(RideRequest::class.java)
        base?.apply { id = this@toRideRequest.id }
    } catch (_: Exception) {
        null
    }
}
