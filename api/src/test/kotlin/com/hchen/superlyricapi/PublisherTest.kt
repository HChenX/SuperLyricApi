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
package com.hchen.superlyricapi

import com.hchen.superlyricapi.internal.BinderHub
import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class PublisherTest {

    private val publisher = SuperLyric.publisher()

    @Before
    fun setUp() {
        BinderHub.injectForTest(null)
    }

    @After
    fun tearDown() {
        BinderHub.injectForTest(null)
    }

    private fun lyric(title: String): Lyric = Lyric(
        "t",
        title,
        "",
        "",
        3000L,
        listOf(Line("a", 0, 1000)),
    )

    @Test
    fun blankTitleIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { publisher.publish(lyric("")) }
        assertThrows(IllegalArgumentException::class.java) { publisher.publish(lyric("   ")) }
    }

    @Test
    fun emptyArtistAndAlbumPassValidation() {
        assertEquals(
            Publisher.PublishResult.SERVICE_UNAVAILABLE,
            publisher.publish(lyric("标题")),
        )
    }
}
