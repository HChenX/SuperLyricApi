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
import com.hchen.superlyricapi.Publisher
import com.hchen.superlyricapi.model.Lyric

/**
 * [Publisher] 的实现。
 */
internal object PublisherImpl : Publisher {

    override fun publish(lyric: Lyric): Publisher.PublishResult {
        val transport = BinderHub.transport() ?: return Publisher.PublishResult.SERVICE_UNAVAILABLE
        return try {
            Publisher.PublishResult.fromCode(transport.publish(lyric))
        } catch (_: RemoteException) {
            BinderHub.reset()
            Publisher.PublishResult.SERVICE_UNAVAILABLE
        }
    }

    override fun clear() {
        val transport = BinderHub.transport() ?: return
        try {
            transport.clear()
        } catch (_: RemoteException) {
            BinderHub.reset()
        }
    }
}
