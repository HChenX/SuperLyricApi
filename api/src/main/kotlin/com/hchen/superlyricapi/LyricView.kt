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

import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.PlaybackState

/**
 * 某一时刻的歌词状态，包含歌词文档、当前行下标、播放进度与倍速。
 *
 * [position] 为推送时刻的快照，连续推进时可结合 SystemClock.elapsedRealtime() 与 [speed] 推算。
 * [publisher] 为发布方包名；3.x 服务端或未填写时为 null。
 * [state] 为 null 表示播放状态未知，不等同于暂停或停止。
 */
class LyricView(
    val lyric: Lyric,
    val index: Int,
    val position: Long,
    val speed: Float,
    val state: PlaybackState?,
    val publisher: String?,
) {
    /**
     * 当前行，可能为 null。
     */
    val line: Line?
        get() = lyric.lines.getOrNull(index)

    /**
     * 已播放的行，不含当前行。
     */
    val played: List<Line>
        get() = if (index <= 0) {
            emptyList()
        } else {
            lyric.lines.subList(0, index.coerceAtMost(lyric.lineCount)).toList()
        }

    /**
     * 尚未播放的行，不含当前行。
     */
    val upcoming: List<Line>
        get() = if (index !in 0 until lyric.lineCount - 1) {
            emptyList()
        } else {
            lyric.lines.subList(index + 1, lyric.lineCount).toList()
        }

    override fun toString(): String =
        "LyricView{${lyric.id} #$index ${line?.text ?: "-"} from=$publisher}"
}
