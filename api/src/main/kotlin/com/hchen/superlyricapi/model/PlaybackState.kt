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
 * 播放状态。由系统从 MediaSession 采集，状态为 null 时表示未知。
 */
enum class PlaybackState {
    PLAYING,
    PAUSED,
    BUFFERING,
    STOPPED,
    ERROR,
}
