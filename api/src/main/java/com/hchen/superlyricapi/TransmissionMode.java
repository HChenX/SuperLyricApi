/*
 * This file is part of SuperLyricApi.
 *
 * SuperLyricApi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * SuperLyricApi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SuperLyricApi. If not, see <https://www.gnu.org/licenses/lgpl-2.1>.
 *
 * Copyright (C) 2025–2026 HChenX
 */
package com.hchen.superlyricapi;

/**
 * 传输模式
 *
 * @author 焕晨HChen
 */
public enum TransmissionMode {
    // 通过 魅族状态栏歌词 功能获取的歌词
    MEIZU_STATUS_BAR_LYRIC,
    // 通过 状态栏歌词 功能获取的歌词
    STATUS_BAR_LYRIC,
    // 通过 桌面歌词 功能获取的歌词
    DESKTOP_LYRIC,
    // 通过 蓝牙歌词 功能获取的歌词
    BLUETOOTH_LYRIC,
    // 通过 Hook 获取的歌词
    HOOK_LYRIC,
    // 通过 API 原生获取的歌词
    API_LYRIC,
    // 通过其他途径获取的歌词
    OTHER_LYRIC
}
