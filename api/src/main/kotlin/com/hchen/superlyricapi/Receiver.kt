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

/**
 * 歌词接收接口。
 */
interface Receiver {
    /**
     * 注册监听器。同一时刻只保留一个，重复注册会替换前一个。
     *
     * @param listener 歌词回调
     * @return 注册结果
     */
    fun register(listener: LyricListener): RegisterResult

    /**
     * 解除监听器注册。
     */
    fun unregister()

    /**
     * 获取当前的歌词状态。
     *
     * @return 当前状态；无播放或服务不可用时为 null
     */
    fun latest(): LyricView?

    enum class RegisterResult {
        OK,
        SERVICE_UNAVAILABLE,
        REJECTED,
    }
}
