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

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 服务定位与链路死亡处理。
 */
@SuppressLint("SoonBlockedPrivateApi", "DiscouragedPrivateApi", "PrivateApi")
internal object BinderHub {
    private const val TAG = "SuperLyric"
    private const val SERVICE_V4 = "super_lyric.v4"
    private const val SERVICE_LEGACY = "super_lyric"

    @Volatile
    private var cached: Transport? = null

    @Volatile
    private var injected: Transport? = null

    private val observers = CopyOnWriteArrayList<(Transport?) -> Unit>()

    /**
     * 获取可用的传输实现。结果会被缓存，服务断开后自动失效。
     *
     * @return 传输实现；服务不存在时为 null
     */
    fun transport(): Transport? {
        injected?.let { return it }
        cached?.let { return it }

        var probed = false
        val result = synchronized(this) {
            if (cached == null) {
                cached = probe()
                probed = true
            }
            cached
        }
        if (probed) notifyObservers(result)
        return result
    }

    /**
     * 订阅传输变化。服务断开时以 null 通知。
     */
    fun addObserver(observer: (Transport?) -> Unit) {
        observers += observer
    }

    fun reset() {
        val previous = synchronized(this) {
            val current = cached
            cached = null
            current
        }
        if (previous != null) notifyObservers(null)
    }

    /**
     * 测试用：覆盖探测结果。
     */
    fun injectForTest(transport: Transport?) {
        injected = transport
        cached = null
    }

    private fun notifyObservers(transport: Transport?) {
        for (observer in observers) {
            runCatching { observer(transport) }
        }
    }

    private fun probe(): Transport? {
        if (!V4Transport.isBlocked()) {
            probeService(SERVICE_V4) { binder ->
                V4Transport.probe(binder)?.also { watch(binder) }
            }?.let { return it }
        }

        probeService(SERVICE_LEGACY) { binder ->
            LegacyTransport.probe(binder)?.also {
                Log.i(TAG, "Falling back to the legacy 3.x service")
                watch(binder)
            }
        }?.let { return it }

        return null
    }

    private inline fun probeService(name: String, create: (IBinder) -> Transport?): Transport? = try {
        ServiceManager.getService(name)?.let(create)
    } catch (t: Throwable) {
        Log.w(TAG, "Service probe failed for $name: ${t.message}")
        null
    }

    private fun watch(binder: IBinder) {
        try {
            binder.linkToDeath({
                synchronized(this) { cached = null }
                notifyObservers(null)
            }, 0)
        } catch (ignored: RemoteException) {
        }
    }
}
