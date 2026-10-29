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
package com.hchen.superlyricapi.wire

import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * 校验 decode 对 kind 与 payload 的约束。
 */
class FrameInvariantTest {

    /**
     * 与 Frame 同构的裸结构，用来构造 Frame 自身无法表达的非法组合。
     */
    class RawFrame {
        @field:Wire(1)
        var kind: Frame.Kind? = null

        @field:Wire(2)
        var doc: Lyric? = null

        @field:Wire(3)
        var state: com.hchen.superlyricapi.model.State? = null

        @field:Wire(4)
        var stop: com.hchen.superlyricapi.model.Stop? = null

        @field:Wire(5)
        var publisher: String? = null
    }

    private fun encodeRaw(block: RawFrame.() -> Unit): ByteArray {
        val raw = RawFrame().apply(block)
        val writer = WireWriter(64)
        writer.writeInt(0x534C5904)
        writer.writeInt(1)
        WireCodec.writeStruct(writer, raw)
        return writer.toByteArray()
    }

    @Test
    fun fullFrameWithoutDocumentIsRejected() {
        val bytes = encodeRaw { kind = Frame.Kind.FULL }

        assertThrows(WireFormatException::class.java) { Frame.decode(bytes) }
    }

    @Test
    fun stateFrameWithoutStateIsRejected() {
        val bytes = encodeRaw { kind = Frame.Kind.STATE }

        assertThrows(WireFormatException::class.java) { Frame.decode(bytes) }
    }

    @Test
    fun stopFrameWithoutStopIsRejected() {
        val bytes = encodeRaw { kind = Frame.Kind.STOP }

        assertThrows(WireFormatException::class.java) { Frame.decode(bytes) }
    }

    /**
     * 一个把 int 字段写成 NULL 的 State，用来验证类型不匹配被包装成 WireFormatException。
     */
    class BrokenState {
        @field:Wire(1)
        var id: String? = null

        @field:Wire(2)
        var index: Int? = null
    }

    class BrokenStateFrame {
        @field:Wire(1)
        var kind: Frame.Kind? = null

        @field:Wire(3)
        var state: BrokenState? = null
    }

    @Test
    fun typeMismatchIsWrappedAsFormatException() {
        val raw = BrokenStateFrame().apply {
            kind = Frame.Kind.STATE
            state = BrokenState().apply { id = "x" }
        }
        val writer = WireWriter(64)
        writer.writeInt(0x534C5904)
        writer.writeInt(1)
        WireCodec.writeStruct(writer, raw)

        assertThrows(WireFormatException::class.java) { Frame.decode(writer.toByteArray()) }
    }

    @Test
    fun oversizedFrameIsRejectedOnDecode() {
        val bytes = ByteArray(Frame.MAX_SIZE + 1)

        assertThrows(WireFormatException::class.java) { Frame.decode(bytes) }
    }

    @Test
    fun oversizedFrameIsRejectedOnEncode() {
        val lines = (0 until 8_000).map { Line("x".repeat(80), it.toLong(), it.toLong() + 1) }
        val lyric = Lyric("big", 0L, lines)

        assertThrows(IllegalStateException::class.java) { Frame.full(lyric, null).encode() }
    }
}
