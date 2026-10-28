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
package com.hchen.superlyricapi.model

import com.hchen.superlyricapi.wire.Wire
import java.util.Objects

/**
 * 一行歌词。
 */
class Line private constructor() {
    @field:Wire(1)
    var text: String = ""
        private set

    @field:Wire(2)
    var start: Long = 0L
        private set

    @field:Wire(3)
    var end: Long = 0L
        private set

    @field:Wire(4)
    var words: List<Word>? = null
        private set

    @field:Wire(5)
    var translation: String? = null
        private set

    @field:Wire(6)
    var secondary: String? = null
        private set

    constructor(text: String, start: Long, end: Long) :
        this(text, start, end, null, null, null)

    constructor(
        text: String,
        start: Long,
        end: Long,
        words: List<Word>?,
        translation: String?,
        secondary: String?,
    ) : this() {
        this.text = text
        this.start = start
        this.end = end
        this.words = words?.toList()
        this.translation = translation
        this.secondary = secondary
    }

    fun hasTranslation(): Boolean = !translation.isNullOrEmpty()

    fun hasSecondary(): Boolean = !secondary.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        return other is Line && start == other.start && end == other.end &&
                text == other.text && words == other.words &&
                translation == other.translation && secondary == other.secondary
    }

    override fun hashCode(): Int = Objects.hash(text, start, end, words, translation, secondary)

    override fun toString(): String = "Line{$text @$start-$end}"
}
