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
 * 歌词发布接口。首次 publish 即完成登记，无单独的注册调用。
 */
interface Publisher {
    /**
     * 发布一整首歌词。切歌时再次调用。
     *
     * [PublishResult.OK] 表示已接收，不表示正在展示。
     *
     * @param lyric 歌词文档
     * @return 发布结果
     * @throws IllegalArgumentException title 为空白
     */
    fun publish(lyric: Lyric): PublishResult

    /**
     * 清除已发布的歌词。切歌与停止通常由系统处理，仅在需要立即清除时调用。
     */
    fun clear()

    /**
     * 发布结果。未知码按 [REJECTED] 处理。
     */
    enum class PublishResult(val code: Int) {
        OK(0),

        /**
         * 服务端未安装或不可达
         */
        SERVICE_UNAVAILABLE(1),

        /**
         * 帧超过 [com.hchen.superlyricapi.wire.Frame.MAX_SIZE]
         */
        PAYLOAD_TOO_LARGE(2),

        /**
         * 服务端拒绝，未细分原因
         */
        REJECTED(3),

        /**
         * title 或 duration 与当前播放会话不一致
         */
        REJECTED_TRACK_MISMATCH(4),

        /**
         * 帧无法解析，或缺少必要字段
         */
        REJECTED_MALFORMED(5),

        /**
         * 调用方不受支持，与是否调用过发布接口无关
         */
        REJECTED_NOT_REGISTERED(6),

        /**
         * 超出发布方配额
         */
        REJECTED_QUOTA(7),
        ;

        companion object {
            /**
             * 整数映射为枚举，未知值回落 [REJECTED]。
             */
            @JvmStatic
            fun fromCode(code: Int): PublishResult =
                entries.firstOrNull { it.code == code } ?: REJECTED
        }
    }
}
