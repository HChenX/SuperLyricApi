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
 * 某一时刻的歌词状态。
 *
 * [position] 是推送时刻的快照，连续推进可用 SystemClock.elapsedRealtime() 与 [speed] 推算。
 * [publisher] 与 [state] 为 null 时表示未知。
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
