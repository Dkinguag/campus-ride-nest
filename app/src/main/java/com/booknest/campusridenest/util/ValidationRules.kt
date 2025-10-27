package com.booknest.campusridenest.util

import java.util.*

//Composable validation rules for form inputs
object ValidationRules {

    //Validate that a string field is not empty
    fun requireNonEmpty(value: String, fieldName: String): ValidationResult {
        return if (value.trim().isBlank()) {
            ValidationResult.Invalid("$fieldName is required")
        } else {
            ValidationResult.Valid
        }
    }

    //Validate that an integer is within a specified range
    fun requirePositiveInt(
        value: Int?,
        fieldName: String,
        min: Int = 1,
        max: Int = Int.MAX_VALUE
    ): ValidationResult {
        return when {
            value == null -> ValidationResult.Invalid("$fieldName is required")
            value < min -> ValidationResult.Invalid("$fieldName must be at least $min")
            value > max -> ValidationResult.Invalid("$fieldName cannot exceed $max")
            else -> ValidationResult.Valid
        }
    }
    @JvmStatic
    //Validate that a date/time is in the future
    fun requireFutureDateTime(dateTime: Date?): ValidationResult {
        if (dateTime == null) {
            return ValidationResult.Invalid("Date and time are required")
        }

        val now = Date()
        if (dateTime.before(now)) {
            return ValidationResult.Invalid("Date and time must be in the future")
        }

        // Check not more than 1 year in future
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 1)
        val oneYearFromNow = calendar.time

        if (dateTime.after(oneYearFromNow)) {
            return ValidationResult.Invalid("Date cannot be more than 1 year in the future")
        }

        return ValidationResult.Valid
    }
    @JvmStatic
    //Validate email format
    fun validateEmail(email: String): ValidationResult {
        if (email.trim().isBlank()) {
            return ValidationResult.Invalid("Email is required")
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (emailRegex.matches(email)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Please enter a valid email address")
        }
    }
    @JvmStatic
    //Validate .edu email (for university verification)
    fun validateEduEmail(email: String): ValidationResult {
        val baseValidation = validateEmail(email)
        if (baseValidation is ValidationResult.Invalid) {
            return baseValidation
        }

        return if (email.lowercase().endsWith(".edu")) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Must use a .edu email address")
        }
    }

    @JvmStatic
    //Validate password strength
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.length < 8 ->
                ValidationResult.Invalid("Password must be at least 8 characters")
            !password.any { it.isDigit() } ->
                ValidationResult.Invalid("Password must contain at least one number")
            !password.any { it.isLetter() } ->
                ValidationResult.Invalid("Password must contain at least one letter")
            else -> ValidationResult.Valid
        }
    }

    @JvmStatic
    //Validate string length
    fun validateMaxLength(value: String, fieldName: String, maxLength: Int): ValidationResult {
        return if (value.length > maxLength) {
            ValidationResult.Invalid("$fieldName cannot exceed $maxLength characters")
        } else {
            ValidationResult.Valid
        }
    }
    @JvmStatic
    //Validate that two fields match
    fun requireMatch(value1: String, value2: String, fieldName: String): ValidationResult {
        return if (value1 == value2) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName does not match")
        }
    }

    @JvmStatic
    // Validate origin location
    fun validateOrigin(origin: String): String? {
        return when {
            origin.trim().isBlank() -> "Origin is required"
            origin.length < 2 -> "Origin must be at least 2 characters"
            origin.length > 100 -> "Origin cannot exceed 100 characters"
            else -> null // null means valid
        }
    }

    @JvmStatic
    // Validate destination location
    fun validateDestination(destination: String): String? {
        return when {
            destination.trim().isBlank() -> "Destination is required"
            destination.length < 2 -> "Destination must be at least 2 characters"
            destination.length > 100 -> "Destination cannot exceed 100 characters"
            else -> null // null means valid
        }
    }

    @JvmStatic
    // Validate number of seats
    fun validateSeats(seats: Int): String? {
        return when {
            seats < 1 -> "At least 1 seat is required"
            seats > 8 -> "Cannot exceed 8 seats"
            else -> null // null means valid
        }
    }

    @JvmStatic
    // Validate price
    fun validatePrice(price: Int): String? {
        return when {
            price < 0 -> "Price cannot be negative"
            price > 10000 -> "Price seems too high (max: $10,000)"
            else -> null // null means valid
        }
    }

    @JvmStatic
    // Validate future date/time (wrapper that returns String?)
    fun validateFutureDateTime(dateTimeMillis: Long): String? {
        val dateTime = Date(dateTimeMillis)
        val result = requireFutureDateTime(dateTime)
        return if (result is ValidationResult.Invalid) {
            result.error
        } else {
            null
        }
    }
}

//Sealed class representing validation result
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
}