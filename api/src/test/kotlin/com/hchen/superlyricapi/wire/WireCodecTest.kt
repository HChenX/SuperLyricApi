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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WireCodecTest {

    /**
     * 旧版字段集，只包含 1、2。
     */
    class Old {
        @field:Wire(1)
        var name: String? = null

        @field:Wire(2)
        var count: Int = 0
    }

    /**
     * 新版字段集，在 1、2 之后追加 3 到 8。
     */
    class Fresh {
        @field:Wire(1)
        var name: String? = null

        @field:Wire(2)
        var count: Int = 0

        @field:Wire(3)
        var tags: List<String>? = null

        @field:Wire(4)
        var big: Long = 0L

        @field:Wire(5)
        var ratio: Float = 0f

        @field:Wire(6)
        var flag: Boolean = false

        @field:Wire(7)
        var blob: ByteArray? = null

        @field:Wire(8)
        var nested: Old? = null
    }

    enum class Colour { RED, GREEN }

    enum class ColourOld { RED }

    class HasColour {
        @field:Wire(1)
        var colour: Colour? = null
    }

    class HasColourOld {
        @field:Wire(1)
        var colour: ColourOld? = null
    }

    private fun <T> roundTrip(value: Any, type: Class<T>): T {
        val writer = WireWriter()
        WireCodec.writeStruct(writer, value)
        return WireCodec.readStruct(WireReader(writer.toByteArray()), type)
    }

    @Test
    fun roundTripsEverySupportedType() {
        val source = Fresh().apply {
            name = "hello"
            count = 42
            tags = listOf("a", "b")
            big = 9_000_000_000L
            ratio = 1.25f
            flag = true
            blob = byteArrayOf(1, 2, 3)
            nested = Old().apply {
                name = "inner"
                count = 7
            }
        }

        val decoded = roundTrip(source, Fresh::class.java)

        assertEquals("hello", decoded.name)
        assertEquals(42, decoded.count)
        assertEquals(listOf("a", "b"), decoded.tags)
        assertEquals(9_000_000_000L, decoded.big)
        assertEquals(1.25f, decoded.ratio, 0.0001f)
        assertTrue(decoded.flag)
        assertArrayEquals(byteArrayOf(1, 2, 3), decoded.blob)
        assertEquals("inner", decoded.nested?.name)
        assertEquals(7, decoded.nested?.count)
    }

    @Test
    fun oldReaderSkipsFieldsItDoesNotKnow() {
        val source = Fresh().apply {
            name = "keep"
            count = 5
            tags = listOf("x")
            nested = Old()
        }

        // 由新版字段集编码，再用旧版字段集解码
        val decoded = roundTrip(source, Old::class.java)

        assertEquals("keep", decoded.name)
        assertEquals(5, decoded.count)
    }

    @Test
    fun newReaderToleratesOldData() {
        val source = Old().apply {
            name = "old"
            count = 1
        }

        val decoded = roundTrip(source, Fresh::class.java)

        assertEquals("old", decoded.name)
        assertEquals(1, decoded.count)
        assertNull(decoded.tags)
        assertEquals(0L, decoded.big)
        assertNull(decoded.nested)
    }

    @Test
    fun unknownEnumConstantBecomesNull() {
        val source = HasColour().apply { colour = Colour.GREEN }

        val decoded = roundTrip(source, HasColourOld::class.java)

        assertNull(decoded.colour)
    }

    @Test
    fun truncatedPayloadThrows() {
        val source = Fresh().apply { name = "abc" }
        val writer = WireWriter()
        WireCodec.writeStruct(writer, source)
        val full = writer.toByteArray()
        val cut = full.copyOf(full.size - 2)

        assertThrows(WireFormatException::class.java) {
            WireCodec.readStruct(WireReader(cut), Fresh::class.java)
        }
    }

    @Test
    fun unsupportedTypeFailsLoudly() {
        assertThrows(IllegalArgumentException::class.java) {
            WireCodec.writeStruct(WireWriter(), Any())
        }
    }
}
