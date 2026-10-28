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
// ILyricFrameListener.aidl
package com.hchen.superlyricapi.v4;

/**
 * v4 帧回调。
 */
interface ILyricFrameListener {
    /**
     * 接收一帧数据。
     *
     * @param frame 帧字节，格式见 Frame
     */
    void onFrame(in byte[] frame);
}
