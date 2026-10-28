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

import com.hchen.superlyricapi.internal.BinderHub
import com.hchen.superlyricapi.internal.PublisherImpl
import com.hchen.superlyricapi.internal.ReceiverImpl

/**
 * API 入口，提供发布端与接收端句柄。
 */
object SuperLyric {
    /**
     * 服务是否可用。任一版本的服务均可视为可用，调用不会抛出异常。
     *
     * @return 服务存在时为 true
     */
    @JvmStatic
    fun isAvailable(): Boolean = BinderHub.transport() != null

    /**
     * 查询服务端实现的 API 版本。
     *
     * @return v4 服务端返回的版本号；3.x 服务端或服务不可用时为 0
     */
    @JvmStatic
    fun serverApiVersion(): Int = BinderHub.transport()?.apiVersion() ?: 0

    @JvmStatic
    fun publisher(): Publisher = PublisherImpl

    @JvmStatic
    fun receiver(): Receiver = ReceiverImpl
}
