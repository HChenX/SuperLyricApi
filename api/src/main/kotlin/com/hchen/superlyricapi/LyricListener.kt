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
 * 歌词回调。
 *
 * 回调可能在任意线程执行，包括 [Receiver.register] 返回之前，以及库内部的工作线程。
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
     * 播放会话结束时调用。暂停不会触发该回调，请以 [LyricView.state] 判断。
     *
     * @param stop 停止事件
     * @param publisher 发布方包名，未知时为 null
     */
    fun onStop(stop: Stop, publisher: String?)
}
