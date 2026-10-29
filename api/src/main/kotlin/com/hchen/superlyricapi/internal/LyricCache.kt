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
package com.hchen.superlyricapi.internal

import android.util.Log
import com.hchen.superlyricapi.LyricView
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.PlaybackState
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.wire.Frame

/**
 * 将 FULL 与 STATE 帧合并为完整视图。
 *
 * [resolve] 不会发起 binder 调用：缓存未命中时把补拉任务交给 [Background]，成功后通过 [deliver] 投递。
 */
internal class LyricCache(
    private val transport: Transport,
    private val deliver: (LyricView) -> Unit,
) {
    var doc: Lyric? = null
        private set

    private var index = -1
    private var position = 0L
    private var speed = 1f
    private var state: PlaybackState? = null
    private var publisher: String? = null
    private var healing = false

    fun resolve(frame: Frame?): LyricView? {
        val source = frame ?: return null
        synchronized(this) {
            when (source.kind) {
                null -> return null
                Frame.Kind.STOP -> {
                    clear()
                    return null
                }
                Frame.Kind.FULL -> {
                    publisher = source.publisher ?: publisher
                    acceptFull(source)
                }
                Frame.Kind.STATE -> {
                    val snapshot = source.state ?: return null
                    if (!matches(snapshot)) {
                        scheduleHeal(snapshot)
                        return null
                    }
                    publisher = source.publisher ?: publisher
                    apply(snapshot)
                }
            }
            return build()
        }
    }

    fun clear() {
        synchronized(this) {
            doc = null
            index = -1
            position = 0L
            speed = 1f
            state = null
            publisher = null
        }
    }

    private fun acceptFull(frame: Frame) {
        frame.doc?.let {
            doc = it
            index = -1
            position = 0L
        }

        val snapshot = frame.state
        if (snapshot != null) {
            apply(snapshot)
        } else {
            doc?.let { index = it.indexAt(position) }
        }
    }

    private fun matches(snapshot: State): Boolean {
        val current = doc ?: return false
        // 帧未携带 id 时按当前曲目处理
        val id = snapshot.id ?: return true
        return id == current.id
    }

    private fun scheduleHeal(snapshot: State) {
        if (healing) return

        healing = true
        Background.execute { heal(snapshot) }
    }

    private fun heal(snapshot: State) {
        val remote = try {
            transport.latest()
        } catch (e: Exception) {
            Log.w(TAG, "Self-heal failed: ${e.message}")
            null
        }

        val view = synchronized(this) {
            healing = false
            val remoteDoc = remote?.doc
            if (remoteDoc != null && (snapshot.id == null || snapshot.id == remoteDoc.id)) {
                doc = remoteDoc
                publisher = remote.publisher ?: publisher
                apply(snapshot)
                build()
            } else {
                null
            }
        }
        view?.let(deliver)
    }

    private fun apply(snapshot: State) {
        position = snapshot.position.coerceAtLeast(0L)
        speed = snapshot.speed
        state = snapshot.state

        val current = doc
        var next = snapshot.index
        if (next < 0 && current != null) {
            next = current.indexAt(position)
        }
        if (current != null && next >= current.lineCount) {
            next = current.lineCount - 1
        }
        index = next
    }

    private fun build(): LyricView? {
        val current = doc ?: return null
        return LyricView(current, index, position, speed, state, publisher)
    }

    private companion object {
        const val TAG = "SuperLyric"
    }
}
