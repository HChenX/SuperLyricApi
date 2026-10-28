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
// ISuperLyricManager.aidl
package com.hchen.superlyricapi;

import com.hchen.superlyricapi.SuperLyricData;
import com.hchen.superlyricapi.ISuperLyricReceiver;

/**
 * 3.x 服务接口，注册于 super_lyric 服务名下。
 *
 * <p>已冻结，仅用于兼容 3.x 客户端。
 */
interface ISuperLyricManager {
    // 注册发布方
    void registerPublisher();

    // 解除发布方注册
    void unregisterPublisher();

    // 是否已注册为发布方
    boolean isPublisherRegistered();

    // 发布歌词数据
    void sendLyric(in SuperLyricData data);

    // 结束当前发布会话并广播停止
    void sendStop(in SuperLyricData data);

    // 注册接收器
    void registerReceiver(in ISuperLyricReceiver receiver);

    // 解除接收器注册
    void unregisterReceiver(in ISuperLyricReceiver receiver);

    // 接收器是否已注册
    boolean isReceiverRegistered(in ISuperLyricReceiver receiver);

    // 已废弃，服务端不处理该调用
    void setSystemPlayStateListenerEnabled(in boolean enabled);

    // 获取当前曲目的歌词数据
    SuperLyricData getLatestLyric();

    // 获取服务端实现的 API 版本号
    int getServerApiVersion();
}
