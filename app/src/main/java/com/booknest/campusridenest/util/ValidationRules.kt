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

    //Validate string length
    fun validateMaxLength(value: String, fieldName: String, maxLength: Int): ValidationResult {
        return if (value.length > maxLength) {
            ValidationResult.Invalid("$fieldName cannot exceed $maxLength characters")
        } else {
            ValidationResult.Valid
        }
    }

    //Validate that two fields match
    fun requireMatch(value1: String, value2: String, fieldName: String): ValidationResult {
        return if (value1 == value2) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("$fieldName does not match")
        }
    }
}

//Sealed class representing validation result
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
}