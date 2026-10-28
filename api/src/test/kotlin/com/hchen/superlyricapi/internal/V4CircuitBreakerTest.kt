/*
 * This file is part of SuperLyricApi.
 *
 * SuperLyricApi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Copyright (C) 2025–2026 HChenX
 */
package com.hchen.superlyricapi.internal

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class V4CircuitBreakerTest {

    @Before
    fun setUp() {
        V4Transport.noteSuccess()
    }

    @After
    fun tearDown() {
        V4Transport.noteSuccess()
    }

    @Test
    fun breakerOpensAfterRepeatedFailures() {
        repeat(3) { V4Transport.noteFailure() }

        assertTrue(V4Transport.isBlocked())
    }

    @Test
    fun breakerStaysClosedBelowThreshold() {
        repeat(2) { V4Transport.noteFailure() }

        assertFalse(V4Transport.isBlocked())
    }

    @Test
    fun successClosesTheBreaker() {
        repeat(3) { V4Transport.noteFailure() }
        V4Transport.noteSuccess()

        assertFalse(V4Transport.isBlocked())
    }
}
