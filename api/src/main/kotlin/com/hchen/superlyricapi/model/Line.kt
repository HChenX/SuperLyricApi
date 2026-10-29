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
 *
 * 时序合法性由发布端保证：start 不大于 end，words 按 start 升序且落在行区间内。
 * 模型层不做校验，避免拒绝真实歌词数据中的常见瑕疵。
 */
class Line private constructor() {
    /**
     * 行文本。
     */
    @field:Wire(1)
    var text: String = ""
        private set

    /**
     * 行开始时间，毫秒。
     */
    @field:Wire(2)
    var start: Long = 0L
        private set

    /**
     * 行结束时间，毫秒。
     */
    @field:Wire(3)
    var end: Long = 0L
        private set

    /**
     * 逐字数据，没有逐字时为 null。
     */
    @field:Wire(4)
    var words: List<Word>? = null
        private set

    /**
     * 翻译文本。
     */
    @field:Wire(5)
    var translation: String? = null
        private set

    /**
     * 注音或副歌词文本，例如罗马音。
     */
    @field:Wire(6)
    var phonetic: String? = null
        private set

    constructor(text: String, start: Long, end: Long) :
        this(text, start, end, null, null, null)

    constructor(
        text: String,
        start: Long,
        end: Long,
        words: List<Word>?,
        translation: String?,
        phonetic: String?,
    ) : this() {
        this.text = text
        this.start = start
        this.end = end
        this.words = words?.takeIf { it.isNotEmpty() }?.toList()
        this.translation = translation
        this.phonetic = phonetic
    }

    fun hasTranslation(): Boolean = !translation.isNullOrEmpty()

    fun hasPhonetic(): Boolean = !phonetic.isNullOrEmpty()

    override fun equals(other: Any?): Boolean {
        if (other !is Line) return false
        return start == other.start && end == other.end &&
            text == other.text && words == other.words &&
            translation == other.translation && phonetic == other.phonetic
    }

    override fun hashCode(): Int = Objects.hash(text, start, end, words, translation, phonetic)

    override fun toString(): String = "Line{$text @$start-$end}"
}
