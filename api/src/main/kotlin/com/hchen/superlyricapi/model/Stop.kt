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
 * 停止事件。
 */
class Stop private constructor() {
    @field:Wire(1)
    var id: String? = null
        private set

    @field:Wire(2)
    var reason: StopReason? = null
        private set

    constructor(id: String?, reason: StopReason?) : this() {
        this.id = id
        this.reason = reason
    }

    override fun toString(): String = "Stop{$id, $reason}"
}
