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
package com.hchen.superlyricapi.internal

import android.os.RemoteException
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.wire.Frame

/**
 * 传输层接口，屏蔽 4.0 与 3.x 两代服务端。
 */
internal interface Transport {
    @Throws(RemoteException::class)
    fun publish(lyric: Lyric): Int

    @Throws(RemoteException::class)
    fun clear()

    @Throws(RemoteException::class)
    fun register(sink: FrameSink)

    @Throws(RemoteException::class)
    fun unregister()

    @Throws(RemoteException::class)
    fun latest(): Frame?
}
