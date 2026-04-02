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
// ISuperLyricReceiver.aidl
package com.hchen.superlyricapi;

// Declare any non-default types here with import statements
import com.hchen.superlyricapi.SuperLyricData;

interface ISuperLyricReceiver {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    // void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //          double aDouble, String aString);

    // 发布数据时调用
    void onLyric(in SuperLyricData data);

    // 歌曲被暂停时调用
    void onStop(in SuperLyricData data);
}