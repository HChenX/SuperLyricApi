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

import java.util.concurrent.ConcurrentHashMap

/**
 * 一个类的字段号表，按字段号排序。
 *
 * 字段由 [Wire] 注解在运行期扫描得到，每个类只缓存一次。
 */
internal class Schema private constructor(private val type: Class<*>) {
    val fields: List<WireField>
    private val byId: Map<Int, WireField>

    init {
        val collected = buildList {
            var current: Class<*>? = type
            while (current != null && current != Any::class.java) {
                for (field in current.declaredFields) {
                    val wire = field.getAnnotation(Wire::class.java) ?: continue
                    field.isAccessible = true
                    add(WireField(wire.value, field))
                }
                current = current.superclass
            }
        }.sortedBy { it.id }

        val index = LinkedHashMap<Int, WireField>(collected.size * 2)
        for (field in collected) {
            val previous = index.put(field.id, field)
            check(previous == null) {
                "Duplicate wire id ${field.id} in ${type.name}: ${previous?.name} / ${field.name}"
            }
        }

        fields = collected
        byId = index
    }

    fun isStruct(): Boolean = fields.isNotEmpty()

    operator fun get(id: Int): WireField? = byId[id]

    /**
     * 返回字段号与字段名的对应关系，供稳定性测试使用。
     */
    fun idToName(): Map<Int, String> = fields.associate { it.id to it.name }

    companion object {
        private val CACHE = ConcurrentHashMap<Class<*>, Schema>()

        fun of(type: Class<*>): Schema = CACHE.computeIfAbsent(type) { Schema(it) }
    }
}
