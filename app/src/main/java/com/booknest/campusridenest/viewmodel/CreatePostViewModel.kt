package com.booknest.campusridenest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.booknest.campusridenest.util.DateTimeUtil
import com.booknest.campusridenest.util.ValidationRules
import com.booknest.campusridenest.util.ValidationResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

//ViewModel for creating ride offers and requests
class CreatePostViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Form state
    private val _formState = MutableLiveData(FormState())
    val formState: LiveData<FormState> = _formState

    // Creation result
    private val _creationResult = MutableLiveData<CreationResult>()
    val creationResult: LiveData<CreationResult> = _creationResult

    //Immutable form state with error fields
    data class FormState(
        val origin: String = "",
        val originError: String? = null,

        val destination: String = "",
        val destinationError: String? = null,

        val dateTime: Date? = null,
        val dateTimeError: String? = null,

        val seats: Int = 1,
        val seatsError: String? = null,

        val isSubmitting: Boolean = false
    ) {
        //Form is valid if no errors and all required fields present
        val isValid: Boolean
            get() = originError == null &&
                    destinationError == null &&
                    dateTimeError == null &&
                    seatsError == null &&
                    origin.isNotBlank() &&
                    destination.isNotBlank() &&
                    dateTime != null &&
                    seats > 0
    }

    //Result of creating offer/request
    sealed class CreationResult {
        data class Success(val postId: String) : CreationResult()
        data class Error(val message: String) : CreationResult()

        fun isSuccess(): Boolean = this is Success
        fun getError(): String = if (this is Error) message else ""
    }

    // ========== Validation Methods ==========

    //Validate origin field
    fun validateOrigin(value: String) {
        val result = ValidationRules.requireNonEmpty(value, "Origin")
        _formState.value = _formState.value?.copy(
            origin = value,
            originError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    //Validate destination field
    fun validateDestination(value: String) {
        val result = ValidationRules.requireNonEmpty(value, "Destination")
        _formState.value = _formState.value?.copy(
            destination = value,
            destinationError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    //Validate seats field
    fun validateSeats(value: Int?) {
        val result = ValidationRules.requirePositiveInt(value, "Seats", min = 1, max = 8)
        _formState.value = _formState.value?.copy(
            seats = value ?: 1,
            seatsError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    //Validate date/time field
    fun validateDateTime(value: Date?) {
        val result = ValidationRules.requireFutureDateTime(value)
        _formState.value = _formState.value?.copy(
            dateTime = value,
            dateTimeError = if (result is ValidationResult.Invalid) result.error else null
        )
    }

    // ========== Creation Methods ==========

    //Create a ride offer
    fun createOffer(origin: String, destination: String, dateTime: Date, seats: Int) {
        // Validate all fields one more time
        validateOrigin(origin)
        validateDestination(destination)
        validateSeats(seats)
        validateDateTime(dateTime)

        if (_formState.value?.isValid != true) {
            _creationResult.value = CreationResult.Error("Please fix all errors before submitting")
            return
        }

        // Get current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _creationResult.value = CreationResult.Error("You must be signed in to create an offer")
            return
        }

        if (!currentUser.isEmailVerified) {
            _creationResult.value = CreationResult.Error("Please verify your email before creating offers")
            return
        }

        // Set submitting state
        _formState.value = _formState.value?.copy(isSubmitting = true)

        // Create offer in Firestore
        viewModelScope.launch {
            try {
                val offer = hashMapOf(
                    "ownerUid" to currentUser.uid,
                    "type" to "offer",
                    "origin" to origin.trim(),
                    "destination" to destination.trim(),
                    "dateTime" to DateTimeUtil.dateToTimestamp(dateTime),
                    "seats" to seats,
                    "status" to "open",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                val docRef = firestore.collection("offers")
                    .add(offer)
                    .await()

                _creationResult.value = CreationResult.Success(docRef.id)
                // Clear form
                _formState.value = FormState()

            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Failed to create offer")
                _formState.value = _formState.value?.copy(isSubmitting = false)
            }
        }
    }

    //Create a ride request
    fun createRequest(origin: String, destination: String, dateTime: Date, seats: Int) {
        // Validate all fields
        validateOrigin(origin)
        validateDestination(destination)
        validateSeats(seats)
        validateDateTime(dateTime)

        if (_formState.value?.isValid != true) {
            _creationResult.value = CreationResult.Error("Please fix all errors before submitting")
            return
        }

        // Get current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _creationResult.value = CreationResult.Error("You must be signed in to create a request")
            return
        }

        if (!currentUser.isEmailVerified) {
            _creationResult.value = CreationResult.Error("Please verify your email before creating requests")
            return
        }

        // Set submitting state
        _formState.value = _formState.value?.copy(isSubmitting = true)

        // Create request in Firestore
        viewModelScope.launch {
            try {
                val request = hashMapOf(
                    "ownerUid" to currentUser.uid,
                    "type" to "request",
                    "origin" to origin.trim(),
                    "destination" to destination.trim(),
                    "dateTime" to DateTimeUtil.dateToTimestamp(dateTime),
                    "seats" to seats,
                    "status" to "open",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                val docRef = firestore.collection("requests")
                    .add(request)
                    .await()

                _creationResult.value = CreationResult.Success(docRef.id)
                // Clear form
                _formState.value = FormState()

            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Failed to create request")
                _formState.value = _formState.value?.copy(isSubmitting = false)
            }
        }
    }

    /**
     * Reset form state
     */
    fun resetForm() {
        _formState.value = FormState()
    }
}