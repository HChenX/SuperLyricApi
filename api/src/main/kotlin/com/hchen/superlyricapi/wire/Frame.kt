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

import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.model.Stop

/**
 * 线上单帧数据。
 *
 * 帧头为 [u32 MAGIC][u32 FORMAT]，其后是一个 TLV Struct。
 */
class Frame private constructor() {
    enum class Kind {
        /** 全量歌词文档，可附带当前状态 */
        FULL,

        /** 播放状态变化 */
        STATE,

        /** 停止事件 */
        STOP,
    }

    @field:Wire(1)
    var kind: Kind? = null
        private set

    @field:Wire(2)
    var doc: Lyric? = null
        private set

    @field:Wire(3)
    var state: State? = null
        private set

    @field:Wire(4)
    var stop: Stop? = null
        private set

    fun encode(): ByteArray {
        val writer = WireWriter(1024)
        writer.writeInt(MAGIC)
        writer.writeInt(FORMAT)
        WireCodec.writeStruct(writer, this)
        return writer.toByteArray()
    }

    companion object {
        /**
         * 单帧最大字节数。超过此值的载荷在发送前被拒绝。
         */
        const val MAX_SIZE: Int = 512 * 1024

        private const val MAGIC = 0x534C5904
        private const val FORMAT = 1
        private const val HEADER_SIZE = 8

        @JvmStatic
        fun full(doc: Lyric, state: State?): Frame = Frame().apply {
            this.kind = Kind.FULL
            this.doc = doc
            this.state = state
        }

        @JvmStatic
        fun state(state: State): Frame = Frame().apply {
            this.kind = Kind.STATE
            this.state = state
        }

        @JvmStatic
        fun stop(stop: Stop): Frame = Frame().apply {
            this.kind = Kind.STOP
            this.stop = stop
        }

        /**
         * 解析一帧数据。
         *
         * @param data 完整帧字节
         * @return 解析结果
         * @throws WireFormatException 帧头不匹配或数据损坏，调用方应丢弃该帧
         */
        @JvmStatic
        fun decode(data: ByteArray): Frame {
            if (data.size < HEADER_SIZE) {
                throw WireFormatException("Frame too short: ${data.size}")
            }

            val reader = WireReader(data)
            val magic = reader.readInt()
            if (magic != MAGIC) {
                throw WireFormatException("Bad magic: 0x${Integer.toHexString(magic)}")
            }

            val format = reader.readInt()
            if (format != FORMAT) {
                throw WireFormatException("Unsupported format $format, expected $FORMAT")
            }
            return WireCodec.readStruct(reader, Frame::class.java)
        }
    }
}
