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

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * [Wire] 字段的读写句柄。
 */
internal class WireField(val id: Int, private val member: Field) {
    private val elementType: Class<*>? = resolveElementType(member)

    val name: String
        get() = member.name

    fun write(writer: WireWriter, target: Any) {
        val value = try {
            member.get(target)
        } catch (e: IllegalAccessException) {
            throw WireFormatException("Cannot read $name", e)
        }
        writer.writeInt(id)
        WireCodec.writeTagged(writer, value)
    }

    fun read(reader: WireReader, target: Any, tag: Int) {
        val value = WireCodec.readTagged(reader, tag, member.type, elementType)
        try {
            member.set(target, value)
        } catch (e: IllegalAccessException) {
            throw WireFormatException("Cannot write $name", e)
        }
    }

    private companion object {
        fun resolveElementType(member: Field): Class<*>? {
            if (!List::class.java.isAssignableFrom(member.type)) return null

            val arguments = (member.genericType as? ParameterizedType)?.actualTypeArguments
            if (arguments != null && arguments.size == 1 && arguments[0] is Class<*>) {
                return arguments[0] as Class<*>
            }
            throw IllegalStateException(
                "List field without a concrete element type: ${member.declaringClass.name}#${member.name}",
            )
        }
    }
}
