package com.booknest.campusridenest.ui.posts

import com.booknest.campusridenest.model.RideOffer
import com.booknest.campusridenest.model.RideRequest

fun RideOffer.toPostUi(): PostUi {
    val safeUpdatedAt = (updatedAt as? Number)?.toLong() ?: System.currentTimeMillis()

    val safeTimeMillis = when {
        timeMillis > 0 -> timeMillis
        dateTime is com.google.firebase.Timestamp -> (dateTime as com.google.firebase.Timestamp).seconds * 1000
        dateTime is Long -> dateTime as Long
        else -> System.currentTimeMillis()
    }

    return PostUi(
        id = id.orEmpty(),
        type = "offer",
        ownerUid = ownerUid.orEmpty(),
        from = from.orEmpty(),
        to = to.orEmpty(),
        dateTime = dateTime?.toString() ?: timeMillis.toString(),
        seats = seats,
        updatedAt = safeUpdatedAt,
        status = (status ?: "open").toString(),
        price = pricePerSeat.toInt(),

        timeMillis = safeTimeMillis,
        createdAt = createdAt,
        pickupLocation = startLocation,
        dropoffLocation = endLocation,
        needsNonSmoking = null,
        needsNoPets = null,
        musicPreference = musicPreference,
        conversationLevel = conversationLevel,
        maxBudget = null
    )
}

fun RideRequest.toPostUi(): PostUi {
    val safeUpdatedAt = (updatedAt as? Number)?.toLong() ?: System.currentTimeMillis()

    val safeTimeMillis = when {
        timeMillis > 0 -> timeMillis
        dateTime is com.google.firebase.Timestamp -> (dateTime as com.google.firebase.Timestamp).seconds * 1000
        dateTime is Long -> dateTime as Long
        else -> System.currentTimeMillis()
    }

    return PostUi(
        id = id.orEmpty(),
        type = "request",
        ownerUid = ownerUid.orEmpty(),
        from = from.orEmpty(),
        to = to.orEmpty(),
        dateTime = dateTime?.toString() ?: timeMillis.toString(),
        seats = seats,
        updatedAt = safeUpdatedAt,
        status = (status ?: "open").toString(),
        price = maxBudget.toInt(),

        timeMillis = safeTimeMillis,
        createdAt = createdAt,
        pickupLocation = pickupLocation,
        dropoffLocation = dropoffLocation,
        needsNonSmoking = needsNonSmoking,
        needsNoPets = needsNoPets,
        musicPreference = musicPreference,
        conversationLevel = conversationLevel,
        maxBudget = maxBudget
    )
}