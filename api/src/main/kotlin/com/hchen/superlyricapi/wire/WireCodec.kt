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
 * TLV 编解码器。
 *
 * 布局（大端）：
 *
 *     Struct := [i32 fieldCount] ( Field ){fieldCount}
 *     Field  := [i32 id] [u8 type] [i32 len]? [payload]
 *
 * 定长类型不带 len，变长类型（STR、BYTES、LIST、STRUCT、ENUM）带 len。
 * 读端遇到未知字段号时按类型跳过。
 */
internal object WireCodec {
    private const val T_NULL = 0
    private const val T_BOOL = 1
    private const val T_I32 = 2
    private const val T_I64 = 3
    private const val T_F32 = 4
    private const val T_F64 = 5
    private const val T_STR = 6
    private const val T_BYTES = 7
    private const val T_LIST = 8
    private const val T_STRUCT = 9
    private const val T_ENUM = 10

    private const val MAX_FIELDS = 4096
    private const val MAX_ELEMENTS = 1 shl 20

    fun writeStruct(writer: WireWriter, value: Any) {
        val schema = Schema.of(value.javaClass)
        require(schema.isStruct()) { "Not a @Wire type: ${value.javaClass.name}" }

        writer.writeInt(schema.fields.size)
        schema.fields.forEach { it.write(writer, value) }
    }

    fun <T> readStruct(reader: WireReader, type: Class<T>): T {
        val schema = Schema.of(type)
        val target = newInstance(type)
        val count = reader.readInt()
        if (count !in 0..MAX_FIELDS) {
            throw WireFormatException("Bad field count: $count")
        }

        repeat(count) {
            val id = reader.readInt()
            val tag = reader.readByte()
            val variable = isVariable(tag)
            val length = if (variable) reader.readInt() else 0
            if (length < 0) {
                throw WireFormatException("Negative field length: $length")
            }

            val start = reader.position()
            schema[id]?.read(reader, target as Any, tag)
            // 读完或跳过后归位，保证未知字段不会影响后续解析
            reader.position(if (variable) start + length else start + fixedSize(tag))
        }
        return target
    }

    fun writeTagged(writer: WireWriter, value: Any?) {
        val tag = tagOf(value)
        writer.writeByte(tag)

        if (!isVariable(tag)) {
            writeFixed(writer, tag, value)
            return
        }

        val lengthPosition = writer.position()
        writer.writeInt(0)
        val bodyStart = writer.position()
        writeVariable(writer, tag, value)
        writer.patchInt(lengthPosition, writer.position() - bodyStart)
    }

    fun readTagged(
        reader: WireReader,
        tag: Int,
        declaredType: Class<*>,
        elementType: Class<*>?,
    ): Any? = when (tag) {
        T_NULL -> null
        T_BOOL -> reader.readByte() != 0
        T_I32 -> reader.readInt()
        T_I64 -> reader.readLong()
        T_F32 -> reader.readFloat()
        T_F64 -> reader.readDouble()
        T_STR -> reader.readString()
        T_BYTES -> readBytes(reader)
        T_ENUM -> enumConstant(declaredType, reader.readString())
        T_STRUCT -> readStruct(reader, declaredType)
        T_LIST -> readList(reader, elementType)
        else -> throw WireFormatException("Unknown type tag $tag")
    }

    private fun writeFixed(writer: WireWriter, tag: Int, value: Any?) {
        when (tag) {
            T_NULL -> Unit
            T_BOOL -> writer.writeByte(if (value as Boolean) 1 else 0)
            T_I32 -> writer.writeInt(value as Int)
            T_I64 -> writer.writeLong(value as Long)
            T_F32 -> writer.writeFloat(value as Float)
            T_F64 -> writer.writeDouble(value as Double)
            else -> throw IllegalStateException("Unhandled tag $tag")
        }
    }

    private fun writeVariable(writer: WireWriter, tag: Int, value: Any?) {
        when (tag) {
            T_STR -> writer.writeString(value as String)
            T_ENUM -> writer.writeString((value as Enum<*>).name)
            T_BYTES -> {
                val bytes = value as ByteArray
                writer.writeInt(bytes.size)
                writer.writeRaw(bytes)
            }
            T_LIST -> {
                val list = value as List<*>
                writer.writeInt(list.size)
                list.forEach { writeTagged(writer, it) }
            }
            T_STRUCT -> writeStruct(writer, value!!)
            else -> throw IllegalStateException("Unhandled tag $tag")
        }
    }

    private fun readBytes(reader: WireReader): ByteArray {
        val length = reader.readInt()
        if (length < 0) {
            throw WireFormatException("Negative byte length: $length")
        }
        return reader.readRaw(length)
    }

    private fun readList(reader: WireReader, elementType: Class<*>?): List<Any?> {
        val count = reader.readInt()
        if (count !in 0..MAX_ELEMENTS) {
            throw WireFormatException("Bad list size: $count")
        }

        val out = ArrayList<Any?>(count.coerceAtMost(64))
        repeat(count) {
            val tag = reader.readByte()
            val variable = isVariable(tag)
            val length = if (variable) reader.readInt() else 0
            if (length < 0) {
                throw WireFormatException("Negative element length: $length")
            }

            val start = reader.position()
            out.add(readTagged(reader, tag, elementType ?: Any::class.java, null))
            reader.position(if (variable) start + length else start + fixedSize(tag))
        }
        return out
    }

    // 未知枚举值按未设置处理
    private fun enumConstant(type: Class<*>, name: String?): Any? {
        if (name == null) return null
        return type.enumConstants?.firstOrNull { (it as Enum<*>).name == name }
    }

    private fun tagOf(value: Any?): Int = when (value) {
        null -> T_NULL
        is String -> T_STR
        is ByteArray -> T_BYTES
        is Boolean -> T_BOOL
        is Int -> T_I32
        is Long -> T_I64
        is Float -> T_F32
        is Double -> T_F64
        is Enum<*> -> T_ENUM
        is List<*> -> T_LIST
        else -> if (Schema.of(value.javaClass).isStruct()) {
            T_STRUCT
        } else {
            throw IllegalArgumentException("Unsupported wire type: ${value.javaClass.name}")
        }
    }

    private fun isVariable(tag: Int): Boolean = when (tag) {
        T_STR, T_BYTES, T_LIST, T_STRUCT, T_ENUM -> true
        T_NULL, T_BOOL, T_I32, T_I64, T_F32, T_F64 -> false
        else -> throw WireFormatException("Unknown type tag $tag")
    }

    private fun fixedSize(tag: Int): Int = when (tag) {
        T_NULL -> 0
        T_BOOL -> 1
        T_I32, T_F32 -> 4
        T_I64, T_F64 -> 8
        else -> throw WireFormatException("Not a fixed-size tag: $tag")
    }

    private fun <T> newInstance(type: Class<T>): T = try {
        type.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
    } catch (e: ReflectiveOperationException) {
        throw WireFormatException("Cannot instantiate ${type.name}", e)
    }
}
