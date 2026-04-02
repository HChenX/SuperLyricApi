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
// ISuperLyricManager.aidl
package com.hchen.superlyricapi;

// Declare any non-default types here with import statements
import com.hchen.superlyricapi.SuperLyricData;
import com.hchen.superlyricapi.ISuperLyricReceiver;

interface ISuperLyricManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    // void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
    //         double aDouble, String aString);

    // 注册发行商
    void registerPublisher(in String packageName);

    // 解除发行商注册
    void unregisterPublisher(in String packageName);

    // 是否已注册为发行商
    boolean isPublisherRegistered(in String packageName);

    // 发布歌词
    void sendLyric(in SuperLyricData data);

    // 发布暂停
    void sendStop(in SuperLyricData data);

    // 注册接收器
    void registerReceiver(in ISuperLyricReceiver receiver);

    // 解除注册接收器
    void unregisterReceiver(in ISuperLyricReceiver receiver);

    // 此接收器是否已经被注册
    boolean isReceiverRegistered(in ISuperLyricReceiver receiver);

    // 设置是否启用系统层播放状态监听功能
    void setSystemPlayStateListenerEnabled(in String packageName, in boolean enabled);
}