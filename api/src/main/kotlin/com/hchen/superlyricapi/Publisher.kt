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

    enum class PublishResult(internal val code: Int) {
        OK(0),

        /** 服务端未安装或不可达 */
        SERVICE_UNAVAILABLE(1),

        /** 编码后的帧超过 [com.hchen.superlyricapi.wire.Frame.MAX_SIZE] */
        PAYLOAD_TOO_LARGE(2),

        /** 服务端拒绝，例如与当前播放曲目不一致 */
        REJECTED(3),
        ;

        internal companion object {
            fun fromCode(code: Int): PublishResult =
                entries.firstOrNull { it.code == code } ?: REJECTED
        }
    }
}
