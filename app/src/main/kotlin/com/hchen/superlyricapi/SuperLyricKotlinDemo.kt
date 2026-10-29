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
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.Word

/**
 * Kotlin 用法示例。
 */
object SuperLyricKotlinDemo {

    /**
     * 接收端示例。
     */
    fun receiverDemo() {
        SuperLyric.receiver().register(object : LyricListener {
            override fun onLyric(view: LyricView) {
                val lyric = view.lyric
                val line = view.line
                val index = view.index
                val position = view.position
                val speed = view.speed
                val state = view.state

                line?.words?.forEach { word ->
                    val text = word.text
                }

                val played = view.played
                val upcoming = view.upcoming
            }

            override fun onStop(stop: Stop) {
                val reason = stop.reason
                val publisher = stop.publisher
            }
        })

        val current = SuperLyric.receiver().latest()

        SuperLyric.receiver().unregisterAll()
    }

    /**
     * 发布端示例。
     */
    fun publisherDemo() {
        if (!SuperLyric.isAvailable()) {
            return
        }

        val lines = listOf(
            Line(
                "你好世界", 0, 900,
                listOf(Word("你好", 0, 400), Word("世界", 400, 900)),
                "Hello World",
                "Ni Hao Shi Jie",
            ),
            Line("下一句歌词", 900, 2000),
        )
        val doc = Lyric("song_1001", 180_000L, lines, "歌曲标题", "艺术家名称", "专辑名称")

        when (SuperLyric.publisher().publish(doc)) {
            Publisher.PublishResult.OK -> Unit
            Publisher.PublishResult.SERVICE_UNAVAILABLE -> Unit
            Publisher.PublishResult.PAYLOAD_TOO_LARGE -> Unit
            // REJECTED 及其细分原因：REJECTED_TRACK_MISMATCH、REJECTED_MALFORMED 等
            else -> Unit
        }
    }
}
