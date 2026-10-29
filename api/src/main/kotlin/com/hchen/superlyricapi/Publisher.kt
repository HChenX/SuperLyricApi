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
 *
 * 首次 publish 即完成登记，不需要额外的注册调用；服务端按调用方 UID 识别发布方。
 */
interface Publisher {
    /**
     * 发布一整首歌词。切歌时再次调用。
     *
     * [PublishResult.OK] 只表示本会话已接收并生效，是否已展示取决于当前是否有其他发布方在播。
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

        /**
         * title 或 duration 与当前播放会话不一致。
         *
         * 服务端至少应做到：标题去空白后完全相等；时长为 0 或与宿主元数据相差不超过 2 秒。
         * 命中该码说明发布的不是当前在播曲目，不建议自动重试。
         */
        REJECTED_TRACK_MISMATCH(4),

        /** 帧无法解析，或 payload 不变式不成立 */
        REJECTED_MALFORMED(5),

        /** 调用方 UID 不在受支持的发布方名单内；与"未调用注册接口"无关 */
        REJECTED_NOT_REGISTERED(6),

        /**
         * 超出发布方配额。
         *
         * 配额由服务端定义，最低语义为按包名计、至少允许 1 个并发会话。
         * 命中该码后客户端应停止重试，等待其他发布会话结束。
         */
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
