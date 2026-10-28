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
import java.util.Objects

/**
 * 播放状态快照，由系统生成。
 */
class State private constructor() {
    @field:Wire(1)
    var id: String? = null
        private set

    @field:Wire(2)
    var index: Int = -1
        private set

    @field:Wire(3)
    var position: Long = 0L
        private set

    @field:Wire(4)
    var speed: Float = 1f
        private set

    @field:Wire(5)
    var state: PlaybackState? = null
        private set

    constructor(
        id: String?,
        index: Int,
        position: Long,
        speed: Float,
        state: PlaybackState?,
    ) : this() {
        this.id = id
        this.index = index
        this.position = position
        this.speed = if (speed <= 0f) 1f else speed
        this.state = state
    }

    override fun equals(other: Any?): Boolean {
        return other is State && index == other.index && position == other.position &&
                speed.compareTo(other.speed) == 0 && id == other.id && state == other.state
    }

    override fun hashCode(): Int = Objects.hash(id, index, position, speed, state)
}
