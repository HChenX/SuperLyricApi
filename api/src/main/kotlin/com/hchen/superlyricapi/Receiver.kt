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
     * 注册时若有在播会话，服务端会立即推送一次 FULL；若没有则不推送，可调用 [latest] 主动拉取。
     *
     * @param listener 歌词回调
     * @return 注册结果
     */
    fun register(listener: LyricListener): RegisterResult

    /**
     * 移除指定监听器。最后一个监听器移除后断开与服务端的连接。
     *
     * @param listener 要移除的监听器
     * @return true 表示本地已断开连接
     */
    fun unregister(listener: LyricListener): Boolean

    /**
     * 移除全部监听器并断开连接。
     *
     * @return true 表示本地已断开连接
     */
    fun unregisterAll(): Boolean

    /**
     * 指定的监听器是否已注册。
     */
    fun isRegistered(listener: LyricListener): Boolean

    /**
     * 主动拉取最近一次会话的终态。
     *
     * 会话已结束时会把 STOP 记入 [lastStop] 并返回 null。
     *
     * @return 当前状态；无会话或服务不可用时为 null
     */
    fun latest(): LyricView?

    /**
     * 获取最近一次停止事件。收到新的歌词后会被清空。
     *
     * @return 最近一次停止事件；尚未收到时为 null
     */
    fun lastStop(): Stop?

    /**
     * 注册结果。服务端返回对应的整数值，未知值按 [REJECTED] 处理。
     */
    enum class RegisterResult(val code: Int) {
        OK(0),

        /** 服务端未安装或不可达 */
        SERVICE_UNAVAILABLE(1),

        /** 服务端拒绝，未细分原因 */
        REJECTED(2),

        /** 超出监听器配额 */
        REJECTED_QUOTA(3),
        ;

        companion object {
            @JvmStatic
            fun fromCode(code: Int): RegisterResult =
                entries.firstOrNull { it.code == code } ?: REJECTED
        }
    }
}
