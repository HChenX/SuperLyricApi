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
 * 歌词回调。回调在 binder 线程执行。
 *
 * 回调中抛出的异常会被捕获并记录，不影响其他接收端。
 */
interface LyricListener {
    /**
     * 当前行或播放状态发生变化时调用。
     *
     * @param view 合成后的歌词状态
     */
    fun onLyric(view: LyricView)

    /**
     * 播放停止、暂停或发布方退出时调用。
     *
     * @param stop 停止事件
     */
    fun onStop(stop: Stop)
}
