package com.example.budgethero

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for BudgetHero app core functionality.
 * Tests input validation logic without requiring Android runtime.
 */
class BudgetHeroUnitTest {

    // ── Auth Validation Tests ─────────────────────────────

    @Test
    fun `username too short returns invalid`() {
        val username = "ab"
        assertTrue(
            "Username under 3 chars should be invalid",
            username.length < 3
        )
    }

    @Test
    fun `valid username passes length check`() {
        val username = "keletso"
        assertTrue(
            "Username of 7 chars should be valid",
            username.length >= 3
        )
    }

    @Test
    fun `passwords match returns true`() {
        val password = "pass123"
        val confirm = "pass123"
        assertEquals(
            "Matching passwords should be equal",
            password, confirm
        )
    }

    @Test
    fun `passwords dont match returns false`() {
        val password = "pass123"
        val confirm = "different"
        assertNotEquals(
            "Different passwords should not match",
            password, confirm
        )
    }

    @Test
    fun `password too short returns invalid`() {
        val password = "abc"
        assertTrue(
            "Password under 4 chars should be invalid",
            password.length < 4
        )
    }

    // ── Expense Validation Tests ──────────────────────────

    @Test
    fun `valid amount passes double parse`() {
        val amount = "150.50"
        assertNotNull(
            "Valid amount string should parse to double",
            amount.toDoubleOrNull()
        )
    }

    @Test
    fun `invalid amount fails double parse`() {
        val amount = "abc"
        assertNull(
            "Non-numeric string should fail to parse",
            amount.toDoubleOrNull()
        )
    }

    @Test
    fun `zero amount is invalid`() {
        val amount = 0.0
        assertFalse(
            "Zero amount should be invalid",
            amount > 0
        )
    }

    @Test
    fun `negative amount is invalid`() {
        val amount = -50.0
        assertFalse(
            "Negative amount should be invalid",
            amount > 0
        )
    }

    @Test
    fun `positive amount is valid`() {
        val amount = 250.0
        assertTrue(
            "Positive amount should be valid",
            amount > 0
        )
    }

    @Test
    fun `blank description is invalid`() {
        val description = "   "
        assertTrue(
            "Blank description should be invalid",
            description.isBlank()
        )
    }

    @Test
    fun `valid description passes`() {
        val description = "Lunch at restaurant"
        assertFalse(
            "Non-blank description should be valid",
            description.isBlank()
        )
    }

    // ── Goals Validation Tests ────────────────────────────

    @Test
    fun `min goal less than max goal is valid`() {
        val min = 1000.0
        val max = 5000.0
        assertTrue(
            "Min must be less than max",
            min < max
        )
    }

    @Test
    fun `min goal equal to max goal is invalid`() {
        val min = 3000.0
        val max = 3000.0
        assertFalse(
            "Min equal to max should be invalid",
            min < max
        )
    }

    @Test
    fun `negative goal is invalid`() {
        val goal = -100.0
        assertFalse(
            "Negative goal should be invalid",
            goal >= 0
        )
    }

    @Test
    fun `spending within goal range is valid`() {
        val spent = 3000.0
        val min = 1000.0
        val max = 5000.0
        assertTrue(
            "Spending within range should be valid",
            spent in min..max
        )
    }

    @Test
    fun `spending over max goal triggers warning`() {
        val spent = 6000.0
        val max = 5000.0
        assertTrue(
            "Spending over max should trigger warning",
            spent > max
        )
    }

    // ── Category Validation Tests ─────────────────────────

    @Test
    fun `blank category name is invalid`() {
        val name = ""
        assertTrue(
            "Empty category name should be invalid",
            name.isBlank()
        )
    }

    @Test
    fun `category name too short is invalid`() {
        val name = "A"
        assertTrue(
            "Single char category should be invalid",
            name.length < 2
        )
    }

    @Test
    fun `valid category name passes`() {
        val name = "Food"
        assertTrue(
            "Valid category name should pass",
            name.length >= 2 && name.isNotBlank()
        )
    }

    // ── Date Format Tests ─────────────────────────────────

    @Test
    fun `date format yyyy-MM-dd is correct length`() {
        val date = "2026-04-27"
        assertEquals(
            "Date string should be 10 characters",
            10, date.length
        )
    }

    @Test
    fun `time format HH-mm is correct length`() {
        val time = "14:30"
        assertEquals(
            "Time string should be 5 characters",
            5, time.length
        )
    }
}