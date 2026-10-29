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
import java.util.concurrent.CopyOnWriteArrayList

/**
 * [Receiver] 的实现。
 *
 * 锁只用于交换字段，任何 binder 调用都在锁外进行，避免与服务端广播互锁。
 */
internal object ReceiverImpl : Receiver {
    private const val TAG = "SuperLyric"
    private const val RECOVER_DELAY_MS = 5_000L
    private const val RETRY_DELAY_MS = 15_000L

    private val lock = Any()
    private val listeners = CopyOnWriteArrayList<LyricListener>()

    private var cache: LyricCache? = null
    private var bound: Transport? = null
    private var lastStopValue: Stop? = null
    private var observing = false

    override fun register(listener: LyricListener): Receiver.RegisterResult {
        ensureObserver()
        listeners.addIfAbsent(listener)

        val transport = BinderHub.transport() ?: return Receiver.RegisterResult.SERVICE_UNAVAILABLE
        if (synchronized(lock) { bound } != null) return Receiver.RegisterResult.OK

        return bind(transport)
    }

    override fun unregister(listener: LyricListener): Boolean {
        listeners.remove(listener)
        if (!listeners.isEmpty()) return false
        return unbind()
    }

    override fun unregisterAll(): Boolean {
        listeners.clear()
        return unbind()
    }

    override fun isRegistered(listener: LyricListener): Boolean = listeners.contains(listener)

    override fun latest(): LyricView? {
        val transport = synchronized(lock) { bound } ?: BinderHub.transport() ?: return null
        val frame = try {
            transport.latest()
        } catch (e: RemoteException) {
            Log.w(TAG, "latest() failed: ${e.message}")
            null
        } ?: return null

        if (frame.kind == Frame.Kind.STOP) return null

        val active = synchronized(lock) { cache } ?: LyricCache(transport, ::deliverView).also {
            synchronized(lock) { cache = it }
        }
        val view = active.resolve(frame)
        if (view != null) synchronized(lock) { lastStopValue = null }
        return view
    }

    override fun lastStop(): Stop? = synchronized(lock) { lastStopValue }

    private fun bind(transport: Transport): Receiver.RegisterResult {
        if (synchronized(lock) { bound } != null) return Receiver.RegisterResult.OK

        val nextCache = LyricCache(transport, ::deliverView)
        synchronized(lock) {
            bound = transport
            cache = nextCache
        }

        val code = try {
            transport.register(::dispatch)
        } catch (e: RemoteException) {
            Log.w(TAG, "register() failed: ${e.message}")
            synchronized(lock) {
                bound = null
                cache = null
            }
            BinderHub.reset()
            return Receiver.RegisterResult.SERVICE_UNAVAILABLE
        }

        val result = Receiver.RegisterResult.fromCode(code)
        if (result != Receiver.RegisterResult.OK) {
            synchronized(lock) {
                bound = null
                cache = null
            }
        }
        return result
    }

    private fun unbind(): Boolean {
        val transport = synchronized(lock) {
            val current = bound
            bound = null
            cache = null
            current
        }

        if (transport == null) return false
        try {
            transport.unregister()
        } catch (ignored: RemoteException) {
        }
        return true
    }

    private fun dispatch(frame: Frame) {
        val current = synchronized(lock) { cache } ?: return
        val publisher = frame.publisher

        if (frame.kind == Frame.Kind.STOP) {
            val stop = frame.stop ?: Stop(null, StopReason.STOPPED)
            synchronized(lock) { lastStopValue = stop }
            current.clear()
            for (listener in listeners) {
                try {
                    listener.onStop(stop, publisher)
                } catch (t: Throwable) {
                    Log.e(TAG, "LyricListener threw", t)
                }
            }
            return
        }

        val view = current.resolve(frame)
        if (view != null) {
            synchronized(lock) { lastStopValue = null }
            deliverView(view)
        }
    }

    private fun deliverView(view: LyricView) {
        for (listener in listeners) {
            try {
                listener.onLyric(view)
            } catch (t: Throwable) {
                Log.e(TAG, "LyricListener threw", t)
            }
        }
    }

    private fun ensureObserver() {
        if (observing) return
        observing = true

        BinderHub.addObserver { transport ->
            if (transport != null) return@addObserver
            // 服务断开：丢弃失效绑定，稍后重连
            synchronized(lock) {
                bound = null
                cache = null
            }
            scheduleRecover(RECOVER_DELAY_MS)
        }
    }

    private fun scheduleRecover(delayMs: Long) {
        if (listeners.isEmpty()) return
        Background.schedule(delayMs) { recover() }
    }

    private fun recover() {
        if (listeners.isEmpty()) return
        if (synchronized(lock) { bound } != null) return

        val transport = BinderHub.transport()
        if (transport == null) {
            scheduleRecover(RETRY_DELAY_MS)
            return
        }
        bind(transport)
    }
}
