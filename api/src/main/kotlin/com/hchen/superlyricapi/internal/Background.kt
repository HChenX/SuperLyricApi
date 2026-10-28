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

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 库内部的后台线程，用于自愈拉取与断线重连。
 */
internal object Background {
    private val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "SuperLyric-worker").apply { isDaemon = true }
    }

    fun execute(task: () -> Unit) {
        executor.execute(task)
    }

    fun schedule(delayMs: Long, task: () -> Unit) {
        executor.schedule(task, delayMs, TimeUnit.MILLISECONDS)
    }
}
