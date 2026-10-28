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
 * 大端字节写入器，容量按需增长。
 */
internal class WireWriter(initialCapacity: Int = 256) {
    private var buffer = ByteArray(initialCapacity.coerceAtLeast(16))
    private var size = 0

    private fun ensure(extra: Int) {
        val needed = size + extra
        if (needed <= buffer.size) return

        var capacity = buffer.size
        while (capacity < needed) capacity = capacity shl 1
        buffer = buffer.copyOf(capacity)
    }

    fun writeByte(value: Int) {
        ensure(1)
        buffer[size] = value.toByte()
        size += 1
    }

    fun writeInt(value: Int) {
        ensure(4)
        buffer[size] = (value ushr 24).toByte()
        buffer[size + 1] = (value ushr 16).toByte()
        buffer[size + 2] = (value ushr 8).toByte()
        buffer[size + 3] = value.toByte()
        size += 4
    }

    fun writeLong(value: Long) {
        writeInt((value ushr 32).toInt())
        writeInt(value.toInt())
    }

    fun writeFloat(value: Float) {
        writeInt(value.toBits())
    }

    fun writeDouble(value: Double) {
        writeLong(value.toBits())
    }

    fun writeRaw(value: ByteArray) {
        ensure(value.size)
        value.copyInto(buffer, size)
        size += value.size
    }

    fun writeString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        writeInt(bytes.size)
        writeRaw(bytes)
    }

    /**
     * 回填指定位置的四字节整数。
     */
    fun patchInt(offset: Int, value: Int) {
        buffer[offset] = (value ushr 24).toByte()
        buffer[offset + 1] = (value ushr 16).toByte()
        buffer[offset + 2] = (value ushr 8).toByte()
        buffer[offset + 3] = value.toByte()
    }

    fun position(): Int = size

    fun toByteArray(): ByteArray = buffer.copyOf(size)
}
