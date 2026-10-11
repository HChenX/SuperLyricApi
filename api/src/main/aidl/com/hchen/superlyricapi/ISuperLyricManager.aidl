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
    void registerPublisher();

    // 解除发行商注册
    void unregisterPublisher();

    // 是否已注册为发行商
    boolean isPublisherRegistered();

    // 发布歌词数据（唯一的发布入口，应携带整首歌词）
    void sendLyric(in SuperLyricData data);

    // 结束当前发布会话：清除已发布歌词并向接收端广播 onStop
    void sendStop(in SuperLyricData data);

    // 注册接收器
    void registerReceiver(in ISuperLyricReceiver receiver);

    // 解除注册接收器
    void unregisterReceiver(in ISuperLyricReceiver receiver);

    // 此接收器是否已经被注册
    boolean isReceiverRegistered(in ISuperLyricReceiver receiver);

    // ⚠️ 保留声明，但已不再对集成方开放
    void setSystemPlayStateListenerEnabled(in boolean enabled);

    // 获取当前正在播放曲目的最新歌词数据（包含全量歌词与播放状态）
    SuperLyricData getLatestLyric();

    // 查询服务端（SuperLyric 模块）实际实现的 API 版本号
    int getServerApiVersion();
}