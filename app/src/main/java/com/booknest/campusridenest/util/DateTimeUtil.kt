package com.booknest.campusridenest.util

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for handling date and time operations
 *
 * This version uses standard Java Date/Calendar (no Java 8 Time API)
 */
object DateTimeUtil {

    // Display formats
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    //Converts Firestore Timestamp to Date
    fun timestampToDate(timestamp: Timestamp): Date {
        return timestamp.toDate()
    }

    //Converts Date to Firestore Timestamp
    fun dateToTimestamp(date: Date): Timestamp {
        return Timestamp(date)
    }

    //Format date/time for display
    fun formatDateTime(date: Date): String {
        return dateTimeFormatter.format(date)
    }

    //Format date only
    fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }

    //Format time only
    fun formatTime(date: Date): String {
        return timeFormatter.format(date)
    }

    //Check if date/time is in the future
    fun isFutureDateTime(date: Date): Boolean {
        return date.after(Date())
    }

    //Check if date/time is within reasonable range (less than 1 year in future)
    fun isReasonableFuture(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        val oneYearFromNow = calendar.time
        return date.before(oneYearFromNow)
    }

    //Show Material Date Picker
    fun showDatePicker(
        context: Context,
        fragmentManager: FragmentManager,
        initialDate: Long = MaterialDatePicker.todayInUtcMilliseconds(),
        onDateSelected: (Long) -> Unit
    ) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select departure date")
            .setSelection(initialDate)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }

        datePicker.show(fragmentManager, "DATE_PICKER")
    }

    //Show Material Time Picker
    fun showTimePicker(
        fragmentManager: FragmentManager,
        initialHour: Int = 12,
        initialMinute: Int = 0,
        onTimeSelected: (hour: Int, minute: Int) -> Unit
    ) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .setTitleText("Select departure time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            onTimeSelected(timePicker.hour, timePicker.minute)
        }

        timePicker.show(fragmentManager, "TIME_PICKER")
    }

    //Combine date (in millis) and time (hour, minute) into Date
    fun combineDateAndTime(dateMillis: Long, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    //Get display text for date button
    fun getDateButtonText(dateMillis: Long?): String {
        return if (dateMillis != null) {
            val date = Date(dateMillis)
            dateFormatter.format(date)
        } else {
            "Select Date"
        }
    }

    //Get display text for time button
    fun getTimeButtonText(hour: Int?, minute: Int?): String {
        return if (hour != null && minute != null) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            timeFormatter.format(calendar.time)
        } else {
            "Select Time"
        }
    }

    //Get current date/time
    fun now(): Date {
        return Date()
    }
}