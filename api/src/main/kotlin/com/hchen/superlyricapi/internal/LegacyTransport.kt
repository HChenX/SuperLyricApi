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
@file:Suppress("DEPRECATION")

package com.hchen.superlyricapi.internal

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.hchen.superlyricapi.ISuperLyricManager
import com.hchen.superlyricapi.ISuperLyricReceiver
import com.hchen.superlyricapi.Publisher
import com.hchen.superlyricapi.Receiver
import com.hchen.superlyricapi.SuperLyricData
import com.hchen.superlyricapi.legacy.LegacyV37
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.StopReason
import com.hchen.superlyricapi.wire.Frame

/**
 * 3.x 服务端的传输实现。
 */
@Suppress("DEPRECATION")
internal class LegacyTransport(private val manager: ISuperLyricManager) : Transport {
    private var publisherRegistered = false
    private var receiver: ISuperLyricReceiver.Stub? = null

    override fun publish(lyric: Lyric): Int {
        ensurePublisher()
        val data = LegacyV37.project(lyric)
        if (!data.hasAllLyrics()) {
            return Publisher.PublishResult.REJECTED.code
        }
        manager.sendLyric(data)
        return Publisher.PublishResult.OK.code
    }

    override fun clear() {
        ensurePublisher()
        manager.sendStop(SuperLyricData())
    }

    override fun register(sink: FrameSink): Int {
        val stub = object : ISuperLyricReceiver.Stub() {
            override fun onLyric(publisher: String?, data: SuperLyricData?) {
                LegacyV37.lift(data, publisher)?.let(sink::onFrame)
            }

            override fun onStop(publisher: String?, data: SuperLyricData?) {
                sink.onFrame(Frame.stop(Stop(data?.lyricId, StopReason.STOPPED, publisher)))
            }
        }
        receiver = stub
        manager.registerReceiver(stub)
        return Receiver.RegisterResult.OK.code
    }

    override fun unregister() {
        val stub = receiver ?: return
        manager.unregisterReceiver(stub)
        receiver = null
    }

    override fun latest(): Frame? = LegacyV37.lift(manager.latestLyric, null)

    override fun apiVersion(): Int = 0

    private fun ensurePublisher() {
        if (publisherRegistered) return

        try {
            if (!manager.isPublisherRegistered) {
                manager.registerPublisher()
            }
            publisherRegistered = true
        } catch (e: RemoteException) {
            Log.w(TAG, "Cannot register as a legacy publisher: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SuperLyric"

        /**
         * 由 IBinder 创建实例。
         *
         * @param binder 服务端 binder，可为 null
         * @return 传输实现；binder 为空或接口不匹配时为 null
         */
        fun probe(binder: IBinder?): Transport? {
            val manager = binder?.let(ISuperLyricManager.Stub::asInterface) ?: return null
            return LegacyTransport(manager)
        }
    }
}
