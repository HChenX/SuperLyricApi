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

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.hchen.superlyricapi.Publisher
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.v4.ILyricFrameListener
import com.hchen.superlyricapi.v4.ISuperLyricService
import com.hchen.superlyricapi.wire.Frame

/**
 * 4.0 服务端的传输实现。
 */
internal class V4Transport(private val service: ISuperLyricService) : Transport {
    private var listener: ILyricFrameListener.Stub? = null
    private var warnedUnknownKind = false

    override fun publish(lyric: Lyric): Int {
        val frame = try {
            Frame.full(lyric, null).encode()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Frame too large: ${e.message}")
            return Publisher.PublishResult.PAYLOAD_TOO_LARGE.code
        }
        return service.publish(frame)
    }

    override fun clear() {
        service.clear()
    }

    override fun register(sink: FrameSink): Int {
        val stub = object : ILyricFrameListener.Stub() {
            override fun onFrame(frame: ByteArray) {
                val decoded = try {
                    Frame.decode(frame)
                } catch (t: Throwable) {
                    Log.w(TAG, "Dropping malformed frame: ${t.message}")
                    return
                }

                if (decoded.kind == null) {
                    warnUnknownKind(frame)
                    return
                }
                sink.onFrame(decoded)
            }
        }
        listener = stub
        return service.registerListener(stub)
    }

    override fun unregister() {
        val stub = listener ?: return
        service.unregisterListener(stub)
        listener = null
    }

    override fun latest(): Frame? = service.latest()?.let(Frame::decode)

    /**
     * 帧类型是本端不认识的枚举值，通常意味着服务端比库新。只记一次，避免每帧刷屏。
     */
    private fun warnUnknownKind(frame: ByteArray) {
        if (warnedUnknownKind) return

        warnedUnknownKind = true
        val head = hexHead(frame)
        Log.w(TAG, "Unknown frame kind, size=${frame.size}, head=$head, serverApiVersion=${apiVersion()}")
    }

    private fun hexHead(frame: ByteArray, limit: Int = 48): String {
        val builder = StringBuilder(limit * 2)
        for (i in 0 until minOf(limit, frame.size)) {
            val value = frame[i].toInt() and 0xFF
            builder.append(value.toString(16).padStart(2, '0'))
        }
        return builder.toString()
    }

    override fun apiVersion(): Int = try {
        service.apiVersion
    } catch (e: RemoteException) {
        0
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
            val service = binder?.let(ISuperLyricService.Stub::asInterface) ?: return null
            return V4Transport(service)
        }
    }
}
