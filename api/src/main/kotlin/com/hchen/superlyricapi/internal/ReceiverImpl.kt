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
import android.util.Log
import com.hchen.superlyricapi.LyricListener
import com.hchen.superlyricapi.LyricView
import com.hchen.superlyricapi.Receiver
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.StopReason
import com.hchen.superlyricapi.wire.Frame

/**
 * [Receiver] 的实现。
 */
internal object ReceiverImpl : Receiver {
    private const val TAG = "SuperLyric"

    private val lock = Any()
    private var callback: LyricListener? = null
    private var cache: LyricCache? = null
    private var bound: Transport? = null

    override fun register(listener: LyricListener): Receiver.RegisterResult {
        synchronized(lock) {
            unregisterLocked()
            val transport = BinderHub.transport()
                ?: return Receiver.RegisterResult.SERVICE_UNAVAILABLE

            val nextCache = LyricCache(transport)
            try {
                transport.register(::dispatch)
            } catch (_: RemoteException) {
                BinderHub.reset()
                return Receiver.RegisterResult.SERVICE_UNAVAILABLE
            }

            bound = transport
            cache = nextCache
            callback = listener
            return Receiver.RegisterResult.OK
        }
    }

    override fun unregister() {
        synchronized(lock) {
            unregisterLocked()
        }
    }

    private fun unregisterLocked() {
        try {
            bound?.unregister()
        } catch (_: RemoteException) {
        }

        bound = null
        cache = null
        callback = null
    }

    override fun latest(): LyricView? {
        synchronized(lock) {
            val transport = bound ?: BinderHub.transport() ?: return null
            return try {
                val frame = transport.latest()
                if (frame == null || frame.kind == Frame.Kind.STOP) {
                    null
                } else {
                    val active = cache ?: LyricCache(transport).also { cache = it }
                    active.resolve(frame)
                }
            } catch (e: Exception) {
                Log.w(TAG, "latest() failed: ${e.message}")
                null
            }
        }
    }

    private fun dispatch(frame: Frame) {
        val target: LyricListener?
        val current: LyricCache?
        synchronized(lock) {
            target = callback
            current = cache
        }

        if (target == null || current == null) return

        try {
            if (frame.kind == Frame.Kind.STOP) {
                val stop = frame.stop ?: Stop(null, StopReason.STOPPED)
                current.clear()
                target.onStop(stop)
                return
            }
            current.resolve(frame)?.let(target::onLyric)
        } catch (t: Throwable) {
            // 回调异常不应中断 binder 线程
            Log.e(TAG, "LyricListener threw", t)
        }
    }
}
