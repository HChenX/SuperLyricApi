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
 * 采集模式
 *
 * @author 焕晨HChen
 */
public enum AcquisitionMode {
    // 我们在此特地区分使用 蓝牙歌词 功能获取歌词的类型
    // 因为使用 蓝牙歌词 获取时，MediaMetadata 的 METADATA_KEY_TITLE 参数会被用于传递歌词
    // 此时使用 METADATA_KEY_TITLE 获取歌曲标题是不准确的，此字段可以帮助您区分是否可以正常使用 METADATA_KEY_TITLE 参数
    // 通过 蓝牙歌词 获取歌词
    BLUETOOTH_LYRIC,
    // 通过 Hook 获取歌词
    HOOK_LYRIC,
    // 通过 API 原生获取歌词
    API_LYRIC
}