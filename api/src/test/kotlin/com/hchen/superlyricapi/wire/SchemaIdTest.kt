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

import com.hchen.superlyricapi.model.Line
import com.hchen.superlyricapi.model.Lyric
import com.hchen.superlyricapi.model.State
import com.hchen.superlyricapi.model.Stop
import com.hchen.superlyricapi.model.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 校验字段号与 wire-ids.txt 中的记录一致。
 *
 * 新增或删除字段时需同步更新该文件。
 */
class SchemaIdTest {

    @Test
    fun idsMatchTheCommittedSnapshot() {
        val snapshot = loadSnapshot()

        for (type in TYPES) {
            val expected = snapshot[type.name]
            assertNotNull("wire-ids.txt has no entry for " + type.name, expected)
            val actual = Schema.of(type).idToName().keys.sorted().toSet()
            assertEquals(
                type.simpleName + " wire ids changed, update wire-ids.txt deliberately",
                expected,
                actual,
            )
        }
    }

    private fun loadSnapshot(): Map<String, Set<Int>> {
        val stream = SchemaIdTest::class.java.getResourceAsStream("/wire-ids.txt")
        assertNotNull("wire-ids.txt is missing from the test resources", stream)
        val out = HashMap<String, Set<Int>>()
        stream!!.bufferedReader(Charsets.UTF_8).useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue
                }
                val split = trimmed.indexOf('=')
                val ids = trimmed.substring(split + 1)
                    .split(",")
                    .map { it.trim().toInt() }
                    .sorted()
                    .toSet()
                out[trimmed.substring(0, split).trim()] = ids
            }
        }
        return out
    }

    private companion object {
        val TYPES = listOf(
            Frame::class.java,
            Lyric::class.java,
            Line::class.java,
            Word::class.java,
            State::class.java,
            Stop::class.java,
        )
    }
}
