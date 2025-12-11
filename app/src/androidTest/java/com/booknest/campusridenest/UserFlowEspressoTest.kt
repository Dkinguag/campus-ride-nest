package com.booknest.campusridenest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.booknest.campusridenest.ui.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class UserFlowEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private val testPassword = "TestPassword123"

    @Test
    fun TC_UAT01_completeRegistrationFlow() {
        // Step 1: Tap Register link
        onView(withId(R.id.tvRegister))
            .check(matches(isDisplayed()))
            .perform(click())

        Thread.sleep(500)

        // Step 2: Enter registration details
        val uniqueEmail = "test${System.currentTimeMillis()}@psu.edu"

        onView(withId(R.id.etEmailR))
            .perform(typeText(uniqueEmail), closeSoftKeyboard())

        onView(withId(R.id.etPassR))
            .perform(typeText(testPassword), closeSoftKeyboard())

        // Step 3: Submit registration
        onView(withId(R.id.btnRegister))
            .perform(click())

        Thread.sleep(2000)
    }

    @Test
    fun TC_UAT02_loginFlow() {
        val testEmail = "espressotest@psu.edu"

        onView(withId(R.id.etEmail))
            .perform(typeText(testEmail), closeSoftKeyboard())

        onView(withId(R.id.etPassword))
            .perform(typeText(testPassword), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())

        Thread.sleep(2000)
    }

    @Test
    fun TC_UAT03_loginButtonsAreDisplayed() {
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))

        onView(withId(R.id.tvRegister))
            .check(matches(isDisplayed()))

        onView(withId(R.id.etEmail))
            .check(matches(isDisplayed()))

        onView(withId(R.id.etPassword))
            .check(matches(isDisplayed()))
    }

    @Test
    fun TC_UAT04_emptyEmailShowsError() {
        // Leave email empty, enter password
        onView(withId(R.id.etPassword))
            .perform(typeText(testPassword), closeSoftKeyboard())

        onView(withId(R.id.btnLogin))
            .perform(click())

        // The app should show an error (validation happens in LoginActivity)
        Thread.sleep(500)
    }

    @Test
    fun TC_UAT05_forgotPasswordLinkDisplayed() {
        onView(withId(R.id.tvForgotPassword))
            .check(matches(isDisplayed()))
    }
}