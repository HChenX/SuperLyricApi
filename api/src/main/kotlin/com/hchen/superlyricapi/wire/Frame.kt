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
 * 线上单帧数据。帧头为 MAGIC 与 FORMAT，其后是 TLV Struct；单帧上限 [MAX_SIZE]。
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

    /**
     * 发布方包名，由服务端填写；STOP 帧取自 [Stop.publisher]。
     */
    @field:Wire(5)
    var publisher: String? = null
        private set

    /**
     * @throws IllegalStateException 编码结果超过 [MAX_SIZE]
     */
    fun encode(): ByteArray {
        val writer = WireWriter(1024)
        writer.writeInt(MAGIC)
        writer.writeInt(FORMAT)
        WireCodec.writeStruct(writer, this)
        val bytes = writer.toByteArray()
        check(bytes.size <= MAX_SIZE) { "Frame exceeds MAX_SIZE: ${bytes.size}" }
        return bytes
    }

    override fun toString(): String =
        "Frame{${kind}, publisher=${publisher}, doc=${doc?.id}, state=${state?.index}}"

    companion object {
        /**
         * 单帧最大字节数，入站与出站一致。
         */
        const val MAX_SIZE: Int = 512 * 1024

        private const val MAGIC = 0x534C5904
        private const val FORMAT = 1
        private const val HEADER_SIZE = 8

        @JvmStatic
        @JvmOverloads
        fun full(doc: Lyric, state: State?, publisher: String? = null): Frame = Frame().apply {
            this.kind = Kind.FULL
            this.doc = doc
            this.state = state
            this.publisher = publisher
        }

        @JvmStatic
        @JvmOverloads
        fun state(state: State, publisher: String? = null): Frame = Frame().apply {
            this.kind = Kind.STATE
            this.state = state
            this.publisher = publisher
        }

        @JvmStatic
        fun stop(stop: Stop): Frame = Frame().apply {
            this.kind = Kind.STOP
            this.stop = stop
            // 与 Stop.publisher 保持一致
            this.publisher = stop.publisher
        }

        /**
         * 解析一帧数据，并校验 kind 与 payload 匹配。
         *
         * @param data 完整帧字节
         * @return 解析结果
         * @throws WireFormatException 帧头不匹配、数据损坏或 payload 缺失
         */
        @JvmStatic
        fun decode(data: ByteArray): Frame {
            if (data.size > MAX_SIZE) {
                throw WireFormatException("Frame exceeds MAX_SIZE: ${data.size}")
            }
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
                throw WireFormatException("Unsupported format ${format}, expected $FORMAT")
            }

            val frame = try {
                WireCodec.readStruct(reader, Frame::class.java)
            } catch (e: WireFormatException) {
                throw e
            } catch (e: RuntimeException) {
                throw WireFormatException("Malformed frame: ${e.message}", e)
            }

            // 已知类型缺 payload 是错误；kind 为 null 表示本端不认识的帧类型，交由调用方跳过
            when (frame.kind) {
                Kind.FULL -> if (frame.doc == null) {
                    throw WireFormatException("FULL frame without doc")
                }
                Kind.STATE -> if (frame.state == null) {
                    throw WireFormatException("STATE frame without state")
                }
                Kind.STOP -> if (frame.stop == null) {
                    throw WireFormatException("STOP frame without stop")
                }
                null -> Unit
            }
            return frame
        }
    }
}
