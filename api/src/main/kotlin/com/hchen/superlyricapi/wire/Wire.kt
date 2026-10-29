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
 * 标记参与线上编解码的字段，仅供本库定义模型时使用。
 *
 * 同一类内字段号唯一，只能新增，不能修改或复用；字段名可以修改。
 * 不允许新增 type tag，新数据类型放进 BYTES 字段的子结构。
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
internal annotation class Wire(val value: Int)
