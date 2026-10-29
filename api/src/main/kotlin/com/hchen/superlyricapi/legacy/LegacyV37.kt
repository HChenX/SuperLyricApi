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
@file:Suppress("DEPRECATION")

package com.hchen.superlyricapi.legacy

import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.SuperLyricLine
import com.hchen.superlyricapi.SuperLyricWord
import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.StopReason
import com.hchen.superlyricapi.model.Word
import com.hchen.superlyricapi.wire.Frame

/**
 * v4 模型与 3.x 数据模型互转。
 */
object LegacyV37 {

    /**
     * 转为 3.x 数据。当前行取首行，进度为 0。
     *
     * @param lyric v4 歌词文档
     * @return 3.x 数据
     */
    @JvmStatic
    fun project(lyric: Lyric): SuperLyricData {
        val lines = lyric.lines
        val projected = Array(lines.size) { index ->
            val line = lines[index]
            SuperLyricLine(
                line.text,
                projectWords(line.words),
                line.translation,
                line.phonetic,
                line.start,
                line.end,
            )
        }
        return SuperLyricData()
            .setTitle(lyric.title)
            .setArtist(lyric.artist)
            .setAlbum(lyric.album)
            .setLyricId(lyric.id)
            .setDuration(lyric.duration)
            .setAllLyrics(projected)
            .setCurrentLyricIndex(0)
            .setPosition(0L)
            .setLyric(projected.firstOrNull())
    }

    /**
     * 转为 v4 帧。全量生成 FULL，点位生成 STATE；state 为 null，speed 为 1.0。
     *
     * @param data 3.x 数据，可为 null
     * @param publisher 发布方包名，可为 null
     * @return 帧；无法转换时为 null
     */
    @JvmStatic
    @JvmOverloads
    fun lift(data: SuperLyricData?, publisher: String? = null): Frame? {
        if (data == null) return null

        if (data.hasAllLyrics()) {
            val doc = toLyric(data) ?: return null
            val index = if (data.hasCurrentLyricIndex()) {
                data.currentLyricIndex
            } else {
                doc.indexAt(data.position.coerceAtLeast(0L))
            }
            return Frame.full(
                doc,
                State(doc.id, index, data.position.coerceAtLeast(0L), 1f, null),
                publisher,
            )
        }

        if (data.hasCurrentLyricIndex() || data.hasLyric() || data.hasPosition()) {
            return Frame.state(
                State(
                    idOf(data),
                    data.currentLyricIndex,
                    data.position.coerceAtLeast(0L),
                    1f,
                    null,
                ),
                publisher,
            )
        }
        return Frame.stop(Stop(idOf(data), StopReason.STOPPED, publisher))
    }

    private fun toLyric(data: SuperLyricData): Lyric? {
        @Suppress("UNCHECKED_CAST")
        val raw = (data.allLyrics ?: return null) as Array<SuperLyricLine?>

        val lines = raw.filterNotNull().map { source ->
            Line(
                source.text,
                source.startTime,
                source.endTime,
                liftWords(source.words),
                source.translation,
                source.secondary,
            )
        }
        if (lines.isEmpty()) return null

        return try {
            Lyric(
                idOf(data),
                data.title ?: "",
                data.artist ?: "",
                data.album ?: "",
                data.duration,
                lines,
            )
        } catch (invalid: IllegalArgumentException) {
            null
        }
    }

    private fun projectWords(words: List<Word>?): Array<SuperLyricWord>? =
        words?.takeIf { it.isNotEmpty() }?.map { SuperLyricWord(it.text, it.start, it.end) }?.toTypedArray()

    private fun liftWords(words: Array<SuperLyricWord>?): List<Word>? =
        words?.filterNotNull()?.takeIf { it.isNotEmpty() }?.map { Word(it.word, it.startTime, it.endTime) }

    private fun idOf(data: SuperLyricData): String =
        data.lyricId?.takeIf { it.isNotEmpty() } ?: metaKey(data.title, data.artist)

    private fun metaKey(title: String?, artist: String?): String =
        "${title.orEmpty().trim()}##${artist.orEmpty().trim()}"
}
