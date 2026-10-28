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
// ISuperLyricService.aidl
package com.hchen.superlyricapi.v4;

import com.hchen.superlyricapi.v4.ILyricFrameListener;

/**
 * v4 服务接口，注册于 super_lyric.v4 服务名下。
 *
 * <p>与 3.x 的 ISuperLyricManager（注册于 super_lyric）相互独立。一个 IBinder 只能实现一套
 * stub 与事务号，因此两代接口使用不同的服务名。接口发布后只允许在末尾追加方法。
 */
interface ISuperLyricService {
    /**
     * 发布全量歌词文档（FULL 帧）。
     *
     * @param frame 帧字节
     * @return PublishResult 的 int 值
     */
    int publish(in byte[] frame);

    /**
     * 清除当前歌词。切歌与停止通常由系统处理。
     */
    void clear();

    /**
     * 注册帧监听器。注册成功后立即收到一帧当前状态。
     *
     * @param listener 监听器
     */
    void registerListener(in ILyricFrameListener listener);

    /**
     * 解除监听器注册。
     *
     * @param listener 监听器
     */
    void unregisterListener(in ILyricFrameListener listener);

    /**
     * 获取当前歌词快照。
     *
     * @return FULL 帧，包含当前状态；无播放时为 null
     */
    byte[] latest();
}
