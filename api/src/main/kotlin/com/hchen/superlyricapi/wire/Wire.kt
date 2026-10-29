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
package com.hchen.superlyricapi.wire

/**
 * 标记参与线上编解码的字段。
 *
 * 同一类内字段号唯一，只能新增，不能修改或复用。字段名可以修改，线上数据只使用字段号。
 *
 * 只允许新增字段，不允许新增 type tag：旧读端遇到未知 tag 无法计算长度，连跳过都做不到。
 * 需要承载新的数据类型时，放进已有 BYTES 字段的子结构，由上层自行解析。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Wire(val value: Int)
