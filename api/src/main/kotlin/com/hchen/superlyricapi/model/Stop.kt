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
 * 停止事件。
 *
 * 自带发布方：停止事件会被接收端缓存并在冷启动时重放，不能只依赖帧级字段。
 */
class Stop private constructor() {
    @field:Wire(1)
    var id: String? = null
        private set

    @field:Wire(2)
    var reason: StopReason? = null
        private set

    /**
     * 发布方包名，未知时为 null。
     */
    @field:Wire(3)
    var publisher: String? = null
        private set

    constructor(id: String?, reason: StopReason?) : this(id, reason, null)

    constructor(id: String?, reason: StopReason?, publisher: String?) : this() {
        this.id = id
        this.reason = reason
        this.publisher = publisher
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Stop) return false
        return id == other.id && reason == other.reason && publisher == other.publisher
    }

    override fun hashCode(): Int = Objects.hash(id, reason, publisher)

    override fun toString(): String = "Stop{$id, $reason, $publisher}"
}
