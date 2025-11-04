package com.booknest.campusridenest.ui.posts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Instant
import java.time.ZoneId
import com.google.firebase.Timestamp

@Parcelize
data class FilterState(
    val origin: String? = null,
    val destination: String? = null,
    val dateRange: DateRange? = null
) : Parcelable {

    val isActive: Boolean
        get() = origin != null || destination != null || dateRange != null

    val activeCount: Int
        get() = listOfNotNull(origin, destination, dateRange).size

    // This function checks if a post matches the current filters
    // This function checks if a post matches the current filters
    fun matchesPost(post: PostUi): Boolean {
        // Origin filter - case insensitive contains
        // PostUi uses "from" for origin
        if (origin != null && !post.from.contains(origin, ignoreCase = true)) {
            return false
        }

        // Destination filter - case insensitive contains
        // PostUi uses "to" for destination
        if (destination != null && !post.to.contains(destination, ignoreCase = true)) {
            return false
        }

        // TODO: Date range filter - temporarily disabled due to Serializable? type
        // We'll implement this after getting the basic filters working
        /*
        if (dateRange != null) {
            // Date filtering logic here
        }
        */

        // All filters passed (or no filters active)
        return true
    }
}

sealed class DateRange : Parcelable {
    abstract val start: LocalDate
    abstract val end: LocalDate

    @Parcelize
    object ThisWeek : DateRange() {
        override val start: LocalDate
            get() = LocalDate.now().with(DayOfWeek.MONDAY)
        override val end: LocalDate
            get() = start.plusDays(6)
    }

    @Parcelize
    object NextWeek : DateRange() {
        override val start: LocalDate
            get() = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1)
        override val end: LocalDate
            get() = start.plusDays(6)
    }

    @Parcelize
    data class Custom(
        override val start: LocalDate,
        override val end: LocalDate
    ) : DateRange()
}