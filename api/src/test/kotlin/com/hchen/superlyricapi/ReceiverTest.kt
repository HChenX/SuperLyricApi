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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReceiverTest {

    private class Fake : Transport {
        var sink: FrameSink? = null
        var registerCode: Int = 0

        override fun publish(lyric: Lyric): Int = 0

        override fun clear() = Unit

        override fun register(sink: FrameSink): Int {
            if (registerCode != 0) return registerCode

            this.sink = sink
            return 0
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
        var lastPublisher: String? = null

        override fun onLyric(view: LyricView) {
            views += view
        }

        override fun onStop(stop: Stop, publisher: String?) {
            stops += stop
            lastPublisher = publisher
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

    private fun fullFrame(publisher: String? = "com.example.music"): Frame =
        Frame.full(lyric(), State("t", 1, 1500L, 1f, PlaybackState.PLAYING), publisher)

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
        assertEquals("com.example.music", first.views[0].publisher)
    }

    @Test
    fun unregisterRemovesOnlyOneListener() {
        val first = Recording()
        val second = Recording()
        receiver.register(first)
        receiver.register(second)

        assertTrue(receiver.isRegistered(first))
        receiver.unregister(first)

        assertFalse(receiver.isRegistered(first))
        assertTrue(receiver.isRegistered(second))

        fake.emit(fullFrame())

        assertTrue(first.views.isEmpty())
        assertEquals(1, second.views.size)
    }

    @Test
    fun lastStopIsRememberedAndClearedByNewLyric() {
        val listener = Recording()
        receiver.register(listener)

        assertNull(receiver.lastStop())

        fake.emit(Frame.stop(Stop("t", StopReason.CLEARED), "com.example.music"))
        assertEquals(StopReason.CLEARED, receiver.lastStop()?.reason)
        assertEquals("com.example.music", listener.lastPublisher)

        fake.emit(fullFrame())
        assertNull(receiver.lastStop())
    }

    @Test
    fun rejectedRegistrationIsReported() {
        fake.registerCode = 3

        assertEquals(Receiver.RegisterResult.REJECTED_QUOTA, receiver.register(Recording()))
    }

    @Test
    fun serverApiVersionComesFromTheTransport() {
        assertEquals(40, SuperLyric.serverApiVersion())
    }
}
