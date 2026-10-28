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

/**
 * 停止原因。
 *
 * 暂停不是停止，通过 [State.state] 为 [PlaybackState.PAUSED] 表达。
 */
enum class StopReason {
    /** 播放器停止或会话结束 */
    STOPPED,

    /** 播放器报错 */
    ERROR,

    /** 发布方进程退出 */
    PROCESS_DIED,

    /** 发布方主动清除 */
    CLEARED,
}
