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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LyricCacheTest {

    private class Fake : Transport {
        @Volatile
        var latestFrame: Frame? = null

        @Volatile
        var latestDelayMs: Long = 0

        val latestCalled = CountDownLatch(1)

        override fun publish(lyric: Lyric): Int = 0

        override fun clear() = Unit

        override fun register(sink: FrameSink): Int = 0

        override fun unregister() = Unit

        override fun latest(): Frame? {
            if (latestDelayMs > 0) {
                Thread.sleep(latestDelayMs)
            }
            latestCalled.countDown()
            return latestFrame
        }

        override fun apiVersion(): Int = 0
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
        val cache = LyricCache(Fake()) { }

        val view = cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0), "com.example.music"))

        assertNotNull(view)
        assertEquals("s1", view!!.lyric.id)
        assertEquals("a", view.line!!.text)
        assertEquals("com.example.music", view.publisher)
    }

    @Test
    fun stateUpdatesIndexAndPosition() {
        val cache = LyricCache(Fake()) { }
        cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0)))

        val view = cache.resolve(Frame.state(state("s1", 2, 2200)))

        assertEquals(2, view!!.index)
        assertEquals("c", view.line!!.text)
        assertEquals(2200L, view.position)
    }

    @Test
    fun stateWithoutDocumentHealsAsynchronously() {
        val fake = Fake()
        fake.latestFrame = Frame.full(doc("s1"), state("s1", 1, 1200))

        val delivered = CopyOnWriteArrayList<LyricView>()
        val latch = CountDownLatch(1)
        val cache = LyricCache(fake) { view ->
            delivered += view
            latch.countDown()
        }

        // 缓存为空时只收到 STATE 帧：同步返回 null，自愈在后台线程完成
        assertNull(cache.resolve(Frame.state(state("s1", 1, 1200))))

        assertTrue("heal result was not delivered", latch.await(2, TimeUnit.SECONDS))
        assertEquals("b", delivered.first().line!!.text)
    }

    @Test
    fun resolveNeverBlocksOnTheService() {
        val fake = Fake()
        fake.latestDelayMs = 500
        fake.latestFrame = Frame.full(doc("s1"), state("s1", 0, 0))
        val cache = LyricCache(fake) { }

        val startedAt = System.nanoTime()
        cache.resolve(Frame.state(state("s1", 1, 1000)))
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000

        assertTrue("resolve must not block on the service, took $elapsedMs ms", elapsedMs < 100)
    }

    @Test
    fun stateForAnotherTrackIsNotSpliced() {
        val fake = Fake()
        fake.latestFrame = Frame.full(doc("track-B"), state("track-B", 0, 0))

        val delivered = CopyOnWriteArrayList<LyricView>()
        val cache = LyricCache(fake) { delivered += it }

        assertNull(cache.resolve(Frame.state(state("track-A", 1, 1000))))
        assertTrue(fake.latestCalled.await(2, TimeUnit.SECONDS))
        Thread.sleep(100)

        assertTrue("must never splice another track's lyrics in", delivered.isEmpty())
        assertNull(cache.doc)
    }

    @Test
    fun stopClearsTheDocument() {
        val cache = LyricCache(Fake()) { }
        cache.resolve(Frame.full(doc("s1"), state("s1", 0, 0)))

        assertNull(cache.resolve(Frame.stop(Stop("s1", StopReason.STOPPED))))
        assertNull(cache.doc)
    }

    @Test
    fun missingIndexIsDerivedFromPosition() {
        val cache = LyricCache(Fake()) { }
        cache.resolve(Frame.full(doc("s1"), null))

        val view = cache.resolve(Frame.state(state("s1", -1, 1500)))

        assertEquals(1, view!!.index)
    }
}
