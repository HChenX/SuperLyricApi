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
 *
 * <p>线程与锁契约：
 * <ul>
 *   <li>回调可能发生在任意线程，包括 registerListener 返回之前；</li>
 *   <li>客户端在持有自身锁时不会发起 binder 调用，服务端可以同步回调，不会因此死锁；</li>
 *   <li>latest 必须可从任意线程调用，且不得阻塞在广播锁上。</li>
 * </ul>
 *
 * <p>帧上限：单帧不超过 Frame.MAX_SIZE 字节，入站与出站一致。超限的载荷应返回
 * PublishResult.PAYLOAD_TOO_LARGE，而不是抛出异常。
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
     * 注册帧监听器。注册成功后立即推送一次当前状态：有在播会话推送 FULL，
     * 无在播但存在最近一次停止则推送 STOP，两者皆无则不推送。
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
     * 获取当前歌词快照。
     *
     * @return FULL 帧，包含当前状态；无播放时为 null
     */
    byte[] latest();

    /**
     * 查询服务端实现的 API 版本。客户端用它判断服务端是否支持某条新语义。
     *
     * @return 版本号，与 BuildConfig.API_VERSION 一致
     */
    int getApiVersion();
}
