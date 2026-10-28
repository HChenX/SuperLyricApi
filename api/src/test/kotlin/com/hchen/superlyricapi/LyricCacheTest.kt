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

import com.hchen.superlyricapi.internal.FrameSink
import com.hchen.superlyricapi.internal.LyricCache
import com.hchen.superlyricapi.internal.Transport
import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.PlaybackState
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.StopReason
import com.hchen.superlyricapi.wire.Frame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LyricCacheTest {

    private class Fake : Transport {
        var latestFrame: Frame? = null
        var published: Lyric? = null

        override fun publish(lyric: Lyric): Int {
            published = lyric
            return 0
        }

        override fun clear() {
            published = null
        }

        override fun register(sink: FrameSink) = Unit

        override fun unregister() = Unit

        override fun latest(): Frame? = latestFrame
    }

    private fun doc(id: String): Lyric = Lyric(
        id,
        3000L,
        listOf(
            Line("a", 0, 1000),
            Line("b", 1000, 2000),
            Line("c", 2000, 3000),
        ),
    )

    private fun state(id: String, index: Int, position: Long): State =
        State(id, index, position, 1f, PlaybackState.PLAYING)

    @Test
    fun fullFrameProducesAView() {
        val cache = LyricCache(Fake())

        val view = cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0)))

        assertNotNull(view)
        assertEquals("s1", view!!.lyric.id)
        assertEquals("a", view.line!!.text)
    }

    @Test
    fun stateUpdatesIndexAndPosition() {
        val cache = LyricCache(Fake())
        cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0)))

        val view = cache.resolve(Frame.state(state("s1", 2, 2200)))

        assertEquals(2, view!!.index)
        assertEquals("c", view.line!!.text)
        assertEquals(2200L, view.position)
    }

    @Test
    fun stateWithoutDocumentSelfHealsFromTheService() {
        val fake = Fake()
        fake.latestFrame = Frame.full(doc("s1"), state("s1", 1, 1200))
        val cache = LyricCache(fake)

        // 缓存为空时只收到 STATE 帧
        val view = cache.resolve(Frame.state(state("s1", 1, 1200)))

        assertNotNull(view)
        assertEquals("s1", view!!.lyric.id)
        assertEquals("b", view.line!!.text)
    }

    @Test
    fun stateForAnotherTrackIsDropped() {
        val fake = Fake()
        fake.latestFrame = Frame.full(doc("track-B"), state("track-B", 0, 0))
        val cache = LyricCache(fake)

        val view = cache.resolve(Frame.state(state("track-A", 1, 1000)))

        assertNull("must never splice another track's lyrics in", view)
    }

    @Test
    fun stopClearsTheDocument() {
        val cache = LyricCache(Fake())
        cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0)))

        assertNull(cache.resolve(Frame.stop(Stop("s1", StopReason.STOPPED))))
        assertNull(cache.doc)
    }

    @Test
    fun missingIndexIsDerivedFromPosition() {
        val cache = LyricCache(Fake())
        cache.resolve(Frame.full(doc("s1"), null))

        val view = cache.resolve(Frame.state(state("s1", -1, 1500)))

        assertEquals(1, view!!.index)
    }
}
