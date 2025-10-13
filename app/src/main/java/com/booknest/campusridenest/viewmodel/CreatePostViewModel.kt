package com.booknest.campusridenest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booknest.campusridenest.util.ValidationResult
import com.booknest.campusridenest.util.ValidationRules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Data class representing the form state for creating a post (offer or request).
 * Immutable design with copy() ensures predictable state updates.
 */
data class FormState(
    val origin: String = "",
    val originError: String? = null,
    val destination: String = "",
    val destinationError: String? = null,
    val dateTime: LocalDateTime? = null,
    val dateTimeError: String? = null,
    val seats: Int = 1,
    val seatsError: String? = null,
    val price: String = "",
    val priceError: String? = null,
    val notes: String = "",
    val isSubmitting: Boolean = false
) {
    /**
     * Computed property that recalculates on every state change.
     * Ensures button enable/disable stays synchronized with validation state.
     */
    val isValid: Boolean
        get() = originError == null &&
                destinationError == null &&
                dateTimeError == null &&
                seatsError == null &&
                priceError == null &&
                origin.isNotBlank() &&
                destination.isNotBlank() &&
                dateTime != null
}

/**
 * Base ViewModel for Create Offer and Create Request screens.
 * Manages form state and validation logic shared between both screens.
 */
open class CreatePostViewModel : ViewModel() {

    protected val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    /**
     * Validates the origin field and updates state.
     * Called when user types in origin field or field loses focus.
     */
    fun validateOrigin(value: String) {
        val result = ValidationRules.requireNonEmpty(value, "Origin")
        _formState.value = _formState.value.copy(
            origin = value,
            originError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    /**
     * Validates the destination field and updates state.
     * Called when user types in destination field or field loses focus.
     */
    fun validateDestination(value: String) {
        val result = ValidationRules.requireNonEmpty(value, "Destination")
        _formState.value = _formState.value.copy(
            destination = value,
            destinationError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    /**
     * Validates the date/time and updates state.
     * Called when user selects a date/time from pickers.
     */
    fun validateDateTime(dateTime: LocalDateTime?) {
        val result = ValidationRules.requireFutureDateTime(dateTime)
        _formState.value = _formState.value.copy(
            dateTime = dateTime,
            dateTimeError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    /**
     * Validates the number of seats and updates state.
     * Called when user changes seat count via stepper or keyboard.
     */
    fun validateSeats(seats: Int?) {
        val result = ValidationRules.requirePositiveInt(seats, "Seats", min = 1, max = 8)
        _formState.value = _formState.value.copy(
            seats = seats ?: 1,
            seatsError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    /**
     * Validates the optional price field and updates state.
     * Called when user types in price field.
     */
    fun validatePrice(price: String) {
        val result = ValidationRules.validatePrice(price)
        _formState.value = _formState.value.copy(
            price = price,
            priceError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    /**
     * Updates the notes field (no validation needed as it's optional).
     */
    fun updateNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    /**
     * Sets the submitting state (shows loading indicator on button).
     */
    fun setSubmitting(isSubmitting: Boolean) {
        _formState.value = _formState.value.copy(isSubmitting = isSubmitting)
    }

    /**
     * Increments seat count by 1 (up to max of 8).
     */
    fun incrementSeats() {
        val currentSeats = _formState.value.seats
        if (currentSeats < 8) {
            validateSeats(currentSeats + 1)
        }
    }

    /**
     * Decrements seat count by 1 (down to min of 1).
     */
    fun decrementSeats() {
        val currentSeats = _formState.value.seats
        if (currentSeats > 1) {
            validateSeats(currentSeats - 1)
        }
    }

    /**
     * Resets the form to initial state.
     * Called after successful submission or when user cancels.
     */
    fun resetForm() {
        _formState.value = FormState()
    }
}