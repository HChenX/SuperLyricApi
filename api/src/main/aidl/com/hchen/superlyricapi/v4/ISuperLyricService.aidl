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
 * <p>已发布的接口只允许在末尾追加方法。
 *
 * <p>线程：
 * <ul>
 *   <li>回调可能发生在任意线程，包括 registerListener 返回之前；</li>
 *   <li>客户端不会在持锁时发起 binder 调用，服务端可以同步回调；</li>
 *   <li>latest 可从任意线程调用，不得阻塞在广播锁上。</li>
 * </ul>
 *
 * <p>帧上限：单帧不超过 Frame.MAX_SIZE 字节；超限返回 PublishResult.PAYLOAD_TOO_LARGE，
 * 不抛异常。
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
     * 注册帧监听器。
     *
     * 若注册时存在在播会话，立即推送一次 FULL；否则不推送任何帧，
     * 客户端可调用 latest 主动拉取，或等待下一次状态变化。
     *
     * @param listener 监听器
     * @return RegisterResult 的 int 值
     */
    int registerListener(in ILyricFrameListener listener);

    /**
     * 解除监听器注册。
     *
     * @param listener 监听器
     */
    void unregisterListener(in ILyricFrameListener listener);

    /**
     * 获取最近一次会话的终态。
     *
     * @return 在播时返回 FULL；会话已结束时返回 STOP；从未有会话时为 null
     */
    byte[] latest();

    /**
     * 服务端 API 版本。
     *
     * @return 版本号，与 BuildConfig.API_VERSION 一致
     */
    int getApiVersion();
}
