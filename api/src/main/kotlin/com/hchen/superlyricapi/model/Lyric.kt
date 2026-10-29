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
package com.hchen.superlyricapi.model

import com.hchen.superlyricapi.wire.Wire

/**
 * 一首歌的歌词文档。
 *
 * lines 必须按 start 升序；duration 为 0 表示未知，由服务端补齐。
 */
class Lyric private constructor() {
    @field:Wire(1)
    var id: String = ""
        private set

    @field:Wire(2)
    var title: String? = null
        private set

    @field:Wire(3)
    var artist: String? = null
        private set

    @field:Wire(4)
    var album: String? = null
        private set

    /**
     * 总时长，毫秒；0 表示未知。
     */
    @field:Wire(5)
    var duration: Long = 0L
        private set

    @field:Wire(6)
    var lines: List<Line> = emptyList()
        private set

    /**
     * @param id 曲目标识，不能为空白
     * @param duration 总时长，毫秒，不能为负数；0 表示未知
     * @param lines 歌词行，不能为空，必须按 start 升序
     * @throws IllegalArgumentException 必填项缺失或取值非法
     */
    constructor(id: String, duration: Long, lines: List<Line>) : this() {
        require(id.isNotBlank()) { "lyricId is required and must not be blank" }
        require(duration >= 0L) { "duration must not be negative, was $duration" }
        require(lines.isNotEmpty()) { "lines must not be empty" }
        this.id = id
        this.duration = duration
        this.lines = lines.toList()
    }

    constructor(
        id: String,
        duration: Long,
        lines: List<Line>,
        title: String?,
        artist: String?,
        album: String?,
    ) : this(id, duration, lines) {
        this.title = title
        this.artist = artist
        this.album = album
    }

    val lineCount: Int
        get() = lines.size

    /**
     * 计算指定播放进度对应的行下标。
     *
     * @param positionMs 播放进度，毫秒
     * @return 行下标；早于首行起始时间时为 -1
     */
    fun indexAt(positionMs: Long): Int {
        var low = 0
        var high = lines.size - 1
        var result = -1
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].start <= positionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return result
    }

    override fun toString(): String = "Lyric{$id / $title ($lineCount lines)}"
}
