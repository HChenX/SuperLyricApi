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

import com.hchen.superlyricapi.model.Lyric

/**
 * 歌词发布接口。
 */
interface Publisher {
    /**
     * 发布一整首歌词。切歌时再次调用。
     *
     * @param lyric 歌词文档，必填字段由 [Lyric] 校验
     * @return 发布结果
     */
    fun publish(lyric: Lyric): PublishResult

    /**
     * 清除已发布的歌词。切歌与停止通常由系统处理，仅在需要立即清除时调用。
     */
    fun clear()

    /**
     * 发布结果。服务端返回对应的整数值，未知值按 [REJECTED] 处理。
     */
    enum class PublishResult(val code: Int) {
        OK(0),

        /** 服务端未安装或不可达 */
        SERVICE_UNAVAILABLE(1),

        /** 编码后的帧超过 [com.hchen.superlyricapi.wire.Frame.MAX_SIZE] */
        PAYLOAD_TOO_LARGE(2),

        /** 服务端拒绝，未细分原因 */
        REJECTED(3),

        /** title 或 duration 与当前播放会话不一致 */
        REJECTED_TRACK_MISMATCH(4),

        /** 帧无法解析，或 payload 不变式不成立 */
        REJECTED_MALFORMED(5),

        /** 发布方未登记 */
        REJECTED_NOT_REGISTERED(6),

        /** 超出发布方配额 */
        REJECTED_QUOTA(7),
        ;

        companion object {
            /**
             * 把服务端返回的整数映射为枚举，未知值回落 [REJECTED]。
             */
            @JvmStatic
            fun fromCode(code: Int): PublishResult =
                entries.firstOrNull { it.code == code } ?: REJECTED
        }
    }
}
