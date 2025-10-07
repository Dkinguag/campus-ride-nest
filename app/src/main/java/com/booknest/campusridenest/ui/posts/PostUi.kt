package com.booknest.campusridenest.ui.posts

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
    val updatedAt: Any
)

fun Long.toShortDateTime(): String {
    if (this <= 0L) return ""
    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return fmt.format(Date(this))
}
