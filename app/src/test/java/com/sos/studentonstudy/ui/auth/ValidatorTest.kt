package com.sos.studentonstudy.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit test untuk validateEmail dan validatePassword di ui/auth/AuthViewModels.kt.
 * Keduanya mengembalikan null kalau input valid, atau pesan error kalau tidak.
 */
class ValidatorTest {

    // --- validateEmail ---------------------------------------------------

    @Test fun `validateEmail accepts a normal address`() {
        assertNull(validateEmail("felix@example.com"))
        assertNull(validateEmail("student.on.study+01@mail.co.id"))
    }

    @Test fun `validateEmail trims surrounding spaces before checking`() {
        assertNull(validateEmail("  felix@example.com  "))
    }

    @Test fun `validateEmail rejects an empty or blank value`() {
        assertEquals("Email is required", validateEmail(""))
        assertEquals("Email is required", validateEmail("   "))
    }

    @Test fun `validateEmail rejects an address without an at sign`() {
        assertEquals("Enter a valid email address", validateEmail("felix"))
    }

    @Test fun `validateEmail rejects an address without a domain`() {
        assertEquals("Enter a valid email address", validateEmail("felix@"))
    }

    @Test fun `validateEmail rejects a domain without a dot`() {
        assertEquals("Enter a valid email address", validateEmail("felix@example"))
    }

    @Test fun `validateEmail rejects an address with a space inside it`() {
        assertEquals("Enter a valid email address", validateEmail("felix example@mail.com"))
    }

    // --- validatePassword ------------------------------------------------

    @Test fun `validatePassword accepts a password at the minimum length`() {
        assertNull(validatePassword("123456"))
    }

    @Test fun `validatePassword accepts a longer password`() {
        assertNull(validatePassword("password123"))
    }

    @Test fun `validatePassword rejects an empty password`() {
        assertEquals("Password is required", validatePassword(""))
    }

    @Test fun `validatePassword rejects a password one character too short`() {
        assertEquals("Password must be at least 6 characters", validatePassword("12345"))
    }

    @Test fun `validatePassword counts spaces as characters`() {
        assertEquals("Password must be at least 6 characters", validatePassword("  a  "))
        assertNull(validatePassword("  a   "))
    }

    @Test fun `the minimum length constant is six`() {
        assertEquals(6, MIN_PASSWORD_LENGTH)
    }
}
