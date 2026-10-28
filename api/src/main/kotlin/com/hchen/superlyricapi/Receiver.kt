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
package com.hchen.superlyricapi

import com.hchen.superlyricapi.model.Stop

/**
 * 歌词接收接口。
 *
 * 同一进程可以注册多个监听器，各自收到相同的事件。
 */
interface Receiver {
    /**
     * 注册监听器。同一个监听器重复注册不会重复投递。
     *
     * @param listener 歌词回调
     * @return 注册结果
     */
    fun register(listener: LyricListener): RegisterResult

    /**
     * 移除指定监听器。最后一个监听器移除后断开与服务端的连接。
     *
     * @param listener 要移除的监听器
     */
    fun unregister(listener: LyricListener)

    /**
     * 移除全部监听器并断开连接。
     */
    fun unregisterAll()

    /**
     * 获取当前的歌词状态。
     *
     * @return 当前状态；无播放或服务不可用时为 null
     */
    fun latest(): LyricView?

    /**
     * 获取最近一次停止事件。用于冷启动时区分"从未播放"与"已停止"。
     *
     * @return 最近一次停止事件；尚未收到时为 null
     */
    fun lastStop(): Stop?

    enum class RegisterResult {
        OK,
        SERVICE_UNAVAILABLE,
        REJECTED,
    }
}
