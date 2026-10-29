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
 * 播放状态。
 *
 * 由系统从 MediaSession 采集后下发。没有 UNKNOWN 常量：状态为 null 即表示未知，
 * 这同时覆盖"3.x 服务端不提供状态"与"尚未收到状态"两种情况。
 */
enum class PlaybackState {
    PLAYING,
    PAUSED,
    BUFFERING,
    STOPPED,
    ERROR,
}
