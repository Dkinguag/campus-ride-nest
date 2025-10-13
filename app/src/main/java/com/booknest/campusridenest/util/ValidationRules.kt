package com.booknest.campusridenest.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

/**
 * Sealed class representing validation results.
 * Type-safe approach eliminates null-checking boilerplate.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
}

/**
 * Reusable validation rules for form inputs.
 * Pure functions with no dependencies enable fast unit testing.
 */
object ValidationRules {

    /**
     * Validates that a string field is not empty or blank.
     * @param value The string to validate
     * @param fieldName The name of the field (for error messages)
     * @return ValidationResult.Valid or ValidationResult.Invalid with error message
     */
    fun requireNonEmpty(value: String, fieldName: String): ValidationResult {
        return if (value.trim().isBlank()) {
            ValidationResult.Invalid("$fieldName is required")
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Validates that an integer is within a specified range.
     * @param value The integer to validate (nullable)
     * @param fieldName The name of the field (for error messages)
     * @param min Minimum allowed value (inclusive)
     * @param max Maximum allowed value (inclusive)
     * @return ValidationResult.Valid or ValidationResult.Invalid with error message
     */
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

    /**
     * Validates that a date/time is in the future.
     * @param dateTime The LocalDateTime to validate (nullable)
     * @return ValidationResult.Valid or ValidationResult.Invalid with error message
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun requireFutureDateTime(dateTime: LocalDateTime?): ValidationResult {
        return when {
            dateTime == null -> ValidationResult.Invalid("Date and time are required")
            dateTime.isBefore(LocalDateTime.now()) ->
                ValidationResult.Invalid("Date and time must be in the future")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates an optional price string (must be positive number if provided).
     * @param price The price string to validate
     * @return ValidationResult.Valid or ValidationResult.Invalid with error message
     */
    fun validatePrice(price: String): ValidationResult {
        if (price.isBlank()) {
            return ValidationResult.Valid // Price is optional
        }

        val priceValue = price.toDoubleOrNull()
        return when {
            priceValue == null -> ValidationResult.Invalid("Price must be a valid number")
            priceValue < 0 -> ValidationResult.Invalid("Price cannot be negative")
            priceValue > 1000 -> ValidationResult.Invalid("Price cannot exceed $1000")
            else -> ValidationResult.Valid
        }
    }
}