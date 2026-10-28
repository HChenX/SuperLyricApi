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
import com.hchen.superlyricapi.internal.FrameSink
import com.hchen.superlyricapi.internal.ReceiverImpl
import com.hchen.superlyricapi.internal.Transport
import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.PlaybackState
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.StopReason
import com.hchen.superlyricapi.wire.Frame
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReceiverTest {

    private class Fake : Transport {
        var sink: FrameSink? = null

        override fun publish(lyric: Lyric): Int = 0

        override fun clear() = Unit

        override fun register(sink: FrameSink) {
            this.sink = sink
        }

        override fun unregister() {
            sink = null
        }

        override fun latest(): Frame? = null

        override fun apiVersion(): Int = 40

        fun emit(frame: Frame) {
            sink?.onFrame(frame)
        }
    }

    private class Recording : LyricListener {
        val views = ArrayList<LyricView>()
        val stops = ArrayList<Stop>()

        override fun onLyric(view: LyricView) {
            views += view
        }

        override fun onStop(stop: Stop) {
            stops += stop
        }
    }

    private val fake = Fake()
    private val receiver = SuperLyric.receiver()

    @Before
    fun setUp() {
        ReceiverImpl.unregisterAll()
        BinderHub.injectForTest(fake)
    }

    @After
    fun tearDown() {
        ReceiverImpl.unregisterAll()
        BinderHub.injectForTest(null)
    }

    private fun lyric(): Lyric = Lyric(
        "t",
        3000L,
        listOf(Line("a", 0, 1000), Line("b", 1000, 2000)),
    )

    private fun fullFrame(): Frame =
        Frame.full(lyric(), State("t", 1, 1500L, 1f, PlaybackState.PLAYING))

    @Test
    fun multipleListenersReceiveTheSameView() {
        val first = Recording()
        val second = Recording()

        assertEquals(Receiver.RegisterResult.OK, receiver.register(first))
        assertEquals(Receiver.RegisterResult.OK, receiver.register(second))

        fake.emit(fullFrame())

        assertEquals(1, first.views.size)
        assertEquals(1, second.views.size)
        assertEquals("b", first.views[0].line?.text)
        assertEquals("b", second.views[0].line?.text)
    }

    @Test
    fun unregisterRemovesOnlyOneListener() {
        val first = Recording()
        val second = Recording()
        receiver.register(first)
        receiver.register(second)

        receiver.unregister(first)
        fake.emit(fullFrame())

        assertTrue(first.views.isEmpty())
        assertEquals(1, second.views.size)
    }

    @Test
    fun lastStopIsRemembered() {
        val listener = Recording()
        receiver.register(listener)

        assertNull(receiver.lastStop())

        fake.emit(Frame.stop(Stop("t", StopReason.CLEARED)))

        assertEquals(StopReason.CLEARED, receiver.lastStop()?.reason)
        assertEquals(1, listener.stops.size)
    }

    @Test
    fun serverApiVersionComesFromTheTransport() {
        assertEquals(40, SuperLyric.serverApiVersion())
    }
}
