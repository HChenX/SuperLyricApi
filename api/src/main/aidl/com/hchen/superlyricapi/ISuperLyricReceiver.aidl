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
// ISuperLyricReceiver.aidl
package com.hchen.superlyricapi;

import com.hchen.superlyricapi.SuperLyricData;

/**
 * 3.x 接收器接口。
 *
 * <p>已冻结，仅用于兼容 3.x 客户端。
 */
interface ISuperLyricReceiver {
    // 收到歌词数据
    void onLyric(in String publisher, in SuperLyricData data);

    // 收到停止事件
    void onStop(in String publisher, in SuperLyricData data);
}
