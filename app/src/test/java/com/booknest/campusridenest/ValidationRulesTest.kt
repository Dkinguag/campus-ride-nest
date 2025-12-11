package com.booknest.campusridenest

import com.booknest.campusridenest.util.ValidationRules
import com.booknest.campusridenest.util.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for ValidationRules utility class.
 * Tests: TC-U01, TC-U02, TC-U11
 *
 * @author De Andre King
 * @version 1.0
 * @since October 2025
 */
@RunWith(JUnit4::class)
class ValidationRulesTest {

    // ============================================================
    // TC-U01: Email Validation - Valid .edu Email
    // Requirement: US-07 - User Registration - Only .edu emails permitted
    // ============================================================

    @Test
    fun tcU01_validEduEmail_returnsValid() {
        // Arrange
        val validEmail = "student@psu.edu"

        // Act
        val result = ValidationRules.validateEduEmail(validEmail)

        // Assert
        assertTrue("Valid .edu email should return Valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU01_validEduEmailWithSubdomain_returnsValid() {
        val validEmail = "student@mail.psu.edu"
        val result = ValidationRules.validateEduEmail(validEmail)
        assertTrue("Subdomain .edu email should be valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU01_validEduEmailWithNumbers_returnsValid() {
        val validEmail = "abc123@psu.edu"
        val result = ValidationRules.validateEduEmail(validEmail)
        assertTrue("Email with numbers should be valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU01_validEduEmailWithDots_returnsValid() {
        val validEmail = "john.doe@psu.edu"
        val result = ValidationRules.validateEduEmail(validEmail)
        assertTrue("Email with dots should be valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU01_variousUniversityEduEmails_returnValid() {
        val validEmails = listOf(
            "student@harvard.edu",
            "student@stanford.edu",
            "student@mit.edu",
            "student@berkeley.edu"
        )
        validEmails.forEach { email ->
            val result = ValidationRules.validateEduEmail(email)
            assertTrue("$email should be valid", result is ValidationResult.Valid)
        }
    }

    // ============================================================
    // TC-U02: Email Validation - Invalid Non-.edu Email
    // Requirement: US-07 - User Registration - Reject non-.edu emails
    // ============================================================

    @Test
    fun tcU02_gmailEmail_returnsInvalid() {
        // Arrange
        val invalidEmail = "user@gmail.com"

        // Act
        val result = ValidationRules.validateEduEmail(invalidEmail)

        // Assert
        assertTrue("Gmail email should return Invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Must use a .edu email address",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun tcU02_yahooEmail_returnsInvalid() {
        val invalidEmail = "user@yahoo.com"
        val result = ValidationRules.validateEduEmail(invalidEmail)
        assertTrue("Yahoo email should be invalid", result is ValidationResult.Invalid)
    }

    @Test
    fun tcU02_hotmailEmail_returnsInvalid() {
        val invalidEmail = "user@hotmail.com"
        val result = ValidationRules.validateEduEmail(invalidEmail)
        assertTrue("Hotmail email should be invalid", result is ValidationResult.Invalid)
    }

    @Test
    fun tcU02_outlookEmail_returnsInvalid() {
        val invalidEmail = "user@outlook.com"
        val result = ValidationRules.validateEduEmail(invalidEmail)
        assertTrue("Outlook email should be invalid", result is ValidationResult.Invalid)
    }

    @Test
    fun tcU02_companyEmail_returnsInvalid() {
        val invalidEmail = "employee@company.com"
        val result = ValidationRules.validateEduEmail(invalidEmail)
        assertTrue("Company email should be invalid", result is ValidationResult.Invalid)
    }

    @Test
    fun tcU02_emptyEmail_returnsInvalid() {
        val invalidEmail = ""
        val result = ValidationRules.validateEduEmail(invalidEmail)
        assertTrue("Empty email should be invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Email is required",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun tcU02_malformedEmails_returnInvalid() {
        val invalidEmails = listOf(
            "notanemail",
            "@psu.edu",
            "student@",
            "student@.edu"
        )
        invalidEmails.forEach { email ->
            val result = ValidationRules.validateEduEmail(email)
            assertTrue("$email should be invalid", result is ValidationResult.Invalid)
        }
    }

    // ============================================================
    // TC-U11: Password Validation - Minimum 8 Characters
    // Requirement: US-07 - User Registration - Password requirements
    // ============================================================

    @Test
    fun tcU11_validPassword_returnsValid() {
        // Arrange - password with 8+ chars, letter, and number
        val validPassword = "Password1"

        // Act
        val result = ValidationRules.validatePassword(validPassword)

        // Assert
        assertTrue("Valid password should return Valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU11_strongPassword_returnsValid() {
        val validPassword = "SecurePassword123!"
        val result = ValidationRules.validatePassword(validPassword)
        assertTrue("Strong password should be valid", result is ValidationResult.Valid)
    }

    @Test
    fun tcU11_passwordWith7Characters_returnsInvalid() {
        val invalidPassword = "Pass123" // 7 chars
        val result = ValidationRules.validatePassword(invalidPassword)
        assertTrue("Password with 7 characters should be invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Password must be at least 8 characters",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun tcU11_passwordWithoutNumber_returnsInvalid() {
        val invalidPassword = "PasswordOnly" // No digit
        val result = ValidationRules.validatePassword(invalidPassword)
        assertTrue("Password without number should be invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Password must contain at least one number",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun tcU11_passwordWithoutLetter_returnsInvalid() {
        val invalidPassword = "12345678" // No letter
        val result = ValidationRules.validatePassword(invalidPassword)
        assertTrue("Password without letter should be invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Password must contain at least one letter",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun tcU11_shortPasswordsOfVariousLengths_returnInvalid() {
        val shortPasswords = listOf("", "1", "12", "Abc1", "Pass1", "Pass12", "Pass123")
        shortPasswords.forEach { password ->
            val result = ValidationRules.validatePassword(password)
            assertTrue(
                "Password '$password' with ${password.length} chars should be invalid",
                result is ValidationResult.Invalid
            )
        }
    }

    // ============================================================
    // Additional Validation Tests - Origin/Destination
    // ============================================================

    @Test
    fun validateOrigin_validOrigin_returnsNull() {
        val origin = "State College, PA"
        val result = ValidationRules.validateOrigin(origin)
        assertEquals("Valid origin should return null", null, result)
    }

    @Test
    fun validateOrigin_emptyOrigin_returnsError() {
        val origin = ""
        val result = ValidationRules.validateOrigin(origin)
        assertEquals("Origin is required", result)
    }

    @Test
    fun validateOrigin_tooShort_returnsError() {
        val origin = "A"
        val result = ValidationRules.validateOrigin(origin)
        assertEquals("Origin must be at least 2 characters", result)
    }

    @Test
    fun validateDestination_validDestination_returnsNull() {
        val destination = "Philadelphia, PA"
        val result = ValidationRules.validateDestination(destination)
        assertEquals("Valid destination should return null", null, result)
    }

    @Test
    fun validateDestination_emptyDestination_returnsError() {
        val destination = ""
        val result = ValidationRules.validateDestination(destination)
        assertEquals("Destination is required", result)
    }

    // ============================================================
    // Additional Validation Tests - Seats
    // ============================================================

    @Test
    fun validateSeats_validSeats_returnsNull() {
        val seats = 3
        val result = ValidationRules.validateSeats(seats)
        assertEquals("Valid seats should return null", null, result)
    }

    @Test
    fun validateSeats_zeroSeats_returnsError() {
        val seats = 0
        val result = ValidationRules.validateSeats(seats)
        assertEquals("At least 1 seat is required", result)
    }

    @Test
    fun validateSeats_tooManySeats_returnsError() {
        val seats = 9
        val result = ValidationRules.validateSeats(seats)
        assertEquals("Cannot exceed 8 seats", result)
    }

    // ============================================================
    // Additional Validation Tests - Price
    // ============================================================

    @Test
    fun validatePrice_validPrice_returnsNull() {
        val price = 25
        val result = ValidationRules.validatePrice(price)
        assertEquals("Valid price should return null", null, result)
    }

    @Test
    fun validatePrice_negativePrice_returnsError() {
        val price = -5
        val result = ValidationRules.validatePrice(price)
        assertEquals("Price cannot be negative", result)
    }

    @Test
    fun validatePrice_tooHighPrice_returnsError() {
        val price = 15000
        val result = ValidationRules.validatePrice(price)
        assertEquals("Price seems too high (max: \$10,000)", result)
    }

    // ============================================================
    // Additional Validation Tests - requireMatch
    // ============================================================

    @Test
    fun requireMatch_matchingValues_returnsValid() {
        val password = "SecurePass123"
        val confirm = "SecurePass123"
        val result = ValidationRules.requireMatch(password, confirm, "Password")
        assertTrue("Matching values should return Valid", result is ValidationResult.Valid)
    }

    @Test
    fun requireMatch_differentValues_returnsInvalid() {
        val password = "SecurePass123"
        val confirm = "DifferentPass456"
        val result = ValidationRules.requireMatch(password, confirm, "Password")
        assertTrue("Different values should return Invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Password does not match",
            (result as ValidationResult.Invalid).error
        )
    }

    // ============================================================
    // Additional Validation Tests - requireNonEmpty
    // ============================================================

    @Test
    fun requireNonEmpty_nonEmptyString_returnsValid() {
        val value = "Hello"
        val result = ValidationRules.requireNonEmpty(value, "Field")
        assertTrue("Non-empty string should return Valid", result is ValidationResult.Valid)
    }

    @Test
    fun requireNonEmpty_emptyString_returnsInvalid() {
        val value = ""
        val result = ValidationRules.requireNonEmpty(value, "Name")
        assertTrue("Empty string should return Invalid", result is ValidationResult.Invalid)
        assertEquals(
            "Name is required",
            (result as ValidationResult.Invalid).error
        )
    }

    @Test
    fun requireNonEmpty_blankString_returnsInvalid() {
        val value = "   "
        val result = ValidationRules.requireNonEmpty(value, "Name")
        assertTrue("Blank string should return Invalid", result is ValidationResult.Invalid)
    }
}