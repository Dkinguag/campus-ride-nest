package com.booknest.campusridenest.ui.posts

import com.booknest.campusridenest.model.RideOffer
import com.booknest.campusridenest.model.RideRequest

fun RideOffer.toPostUi(): PostUi {
    val safeUpdatedAt = (updatedAt as? Number)?.toLong() ?: System.currentTimeMillis()

    return PostUi(
        id          = id.orEmpty(),
        type        = "offer",
        ownerUid    = ownerUid.orEmpty(),
        from        = from.orEmpty(),
        to          = to.orEmpty(),
        dateTime    = dateTime?.toString() ?: timeMillis.toString(),
        seats       = seats,
        updatedAt   = safeUpdatedAt
    )
}

fun RideRequest.toPostUi(): PostUi {
    val safeUpdatedAt = (updatedAt as? Number)?.toLong() ?: System.currentTimeMillis()

    return PostUi(
        id        = id.orEmpty(),
        type      = "request",
        ownerUid  = ownerUid.orEmpty(),
        from = from.orEmpty(),
        to = to.orEmpty(),
        dateTime  = dateTime?.toString() ?: timeMillis.toString(),
        seats     = null,
        updatedAt = safeUpdatedAt
    )
}
