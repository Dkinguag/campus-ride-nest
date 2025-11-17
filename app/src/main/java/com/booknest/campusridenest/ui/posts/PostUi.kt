package com.booknest.campusridenest.ui.posts

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class PostUi(
    val id: String,
    val type: String,
    val ownerUid: String,
    val from: String,
    val to: String,
    val dateTime: Serializable?,
    val seats: Int?,
    val updatedAt: Any,
    val status: String? = "open",
    val price: Int? = null,

    // NEW: Fields for matching algorithm
    val timeMillis: Long? = null,
    val createdAt: Any? = null,
    val pickupLocation: GeoPoint? = null,
    val dropoffLocation: GeoPoint? = null,
    val needsNonSmoking: Boolean? = null,
    val needsNoPets: Boolean? = null,
    val musicPreference: String? = null,
    val conversationLevel: String? = null,
    val maxBudget: Double? = null
)

fun Long.toShortDateTime(): String {
    if (this <= 0L) return ""
    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return fmt.format(Date(this))
}