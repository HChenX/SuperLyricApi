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

/**
 * 大端字节读取器。
 */
internal class WireReader(private val buffer: ByteArray) {
    private val limit = buffer.size
    private var cursor = 0

    private fun ensure(count: Int) {
        if (count < 0 || cursor + count > limit) {
            throw WireFormatException("Truncated payload at $cursor, need $count")
        }
    }

    fun readByte(): Int {
        ensure(1)
        return buffer[cursor++].toInt() and 0xFF
    }

    fun readInt(): Int {
        ensure(4)
        val value = ((buffer[cursor].toInt() and 0xFF) shl 24) or
            ((buffer[cursor + 1].toInt() and 0xFF) shl 16) or
            ((buffer[cursor + 2].toInt() and 0xFF) shl 8) or
            (buffer[cursor + 3].toInt() and 0xFF)
        cursor += 4
        return value
    }

    fun readLong(): Long {
        val high = readInt().toLong() and 0xFFFFFFFFL
        val low = readInt().toLong() and 0xFFFFFFFFL
        return (high shl 32) or low
    }

    fun readFloat(): Float = Float.fromBits(readInt())

    fun readDouble(): Double = Double.fromBits(readLong())

    fun readRaw(length: Int): ByteArray {
        ensure(length)
        val out = buffer.copyOfRange(cursor, cursor + length)
        cursor += length
        return out
    }

    fun readString(): String? {
        val length = readInt()
        if (length < 0) return null

        ensure(length)
        val value = String(buffer, cursor, length, Charsets.UTF_8)
        cursor += length
        return value
    }

    fun position(): Int = cursor

    fun position(newPosition: Int) {
        if (newPosition !in 0..limit) {
            throw WireFormatException("Illegal position $newPosition")
        }
        cursor = newPosition
    }
}
