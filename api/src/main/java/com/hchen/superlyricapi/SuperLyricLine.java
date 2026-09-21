/*
 * This file is part of SuperLyricApi.
 *
 * SuperLyricApi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * SuperLyricApi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SuperLyricApi. If not, see <https://www.gnu.org/licenses/lgpl-2.1>.
 *
 * Copyright (C) 2025–2026 HChenX
 */
package com.hchen.superlyricapi;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 歌词行数据
 * <p>
 * 我们将使用此数据包实现歌词行数据的传递
 *
 * @author 焕晨HChen
 */
public class SuperLyricLine implements Parcelable {
    /**
     * 当前行文本
     */
    @NonNull
    private String text = "";
    /**
     * 当前行逐字数据
     */
    @Nullable
    private SuperLyricWord[] words = null;
    /**
     * 当前行开始时间
     */
    private long startTime = 0L;
    /**
     * 当前行结束时间
     */
    private long endTime = 0L;
    /**
     * 当前行持续时间
     */
    @Deprecated(since = "3.2")
    private long delay = 0L;
    /**
     * 当前行翻译文本
     */
    @Nullable
    private String translation = null;
    /**
     * 当前行次要/副歌词/罗马音文本
     */
    @Nullable
    private String secondary = null;

    public SuperLyricLine(@NonNull String text) {
        ensureText(text);
        this.text = text;
    }

    public SuperLyricLine(@NonNull String text, long startTime, long endTime) {
        ensureText(text);
        this.text = text;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SuperLyricLine(@NonNull String text, @Nullable SuperLyricWord[] words, long startTime, long endTime) {
        ensureText(text);
        this.text = text;
        this.words = words;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SuperLyricLine(@NonNull String text, @Nullable SuperLyricWord[] words, @Nullable String translation, long startTime, long endTime) {
        ensureText(text);
        this.text = text;
        this.words = words;
        this.translation = translation;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public SuperLyricLine(@NonNull String text, @Nullable SuperLyricWord[] words, @Nullable String translation, @Nullable String secondary, long startTime, long endTime) {
        ensureText(text);
        this.text = text;
        this.words = words;
        this.translation = translation;
        this.secondary = secondary;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 请使用 {@link SuperLyricLine#SuperLyricLine(String, long, long)}
     */
    @Deprecated(since = "3.2")
    public SuperLyricLine(@NonNull String text, long delay) {
        ensureText(text);
        this.text = text;
        this.delay = delay;
    }

    /**
     * 请使用 {@link SuperLyricLine#SuperLyricLine(String, SuperLyricWord[], long, long)}
     */
    @Deprecated(since = "3.2")
    public SuperLyricLine(@NonNull String text, @Nullable SuperLyricWord[] words, long delay) {
        ensureText(text);
        this.text = text;
        this.words = words;
        this.delay = delay;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @Nullable
    public SuperLyricWord[] getWords() {
        return words;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Deprecated(since = "3.2")
    public long getDelay() {
        if (delay != 0L) {
            return delay;
        }

        if (endTime > startTime) {
            return endTime - startTime;
        }
        return 0L;
    }

    /**
     * 是否存在翻译文本
     */
    public boolean hasTranslation() {
        return Objects.nonNull(translation) && !translation.isEmpty();
    }

    /**
     * 是否存在次要/副歌词/罗马音文本
     */
    public boolean hasSecondary() {
        return Objects.nonNull(secondary) && !secondary.isEmpty();
    }

    @Nullable
    public String getTranslation() {
        return translation;
    }

    public SuperLyricLine setTranslation(@Nullable String translation) {
        this.translation = translation;
        return this;
    }

    @Nullable
    public String getSecondary() {
        return secondary;
    }

    public SuperLyricLine setSecondary(@Nullable String secondary) {
        this.secondary = secondary;
        return this;
    }

    /**
     * 将当前行的翻译文本包装为独立的 {@link SuperLyricLine}
     */
    @Nullable
    public SuperLyricLine getTranslationLine() {
        return translation != null ? new SuperLyricLine(translation, startTime, endTime) : null;
    }

    /**
     * 将当前行的副歌词文本包装为独立的 {@link SuperLyricLine}
     */
    @Nullable
    public SuperLyricLine getSecondaryLine() {
        return secondary != null ? new SuperLyricLine(secondary, startTime, endTime) : null;
    }

    private void ensureText(String text) {
        Objects.requireNonNull(text, "Lyric text must not be null.");
    }

    @NonNull
    @Override
    public String toString() {
        return "SuperLyricLine{" +
            "text='" + text + '\'' +
            ", words=" + Arrays.toString(words) +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", delay=" + getDelay() +
            ", translation='" + translation + '\'' +
            ", secondary='" + secondary + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SuperLyricLine that)) return false;
        return startTime == that.startTime &&
            endTime == that.endTime &&
            delay == that.delay &&
            Objects.equals(text, that.text) &&
            Objects.equals(translation, that.translation) &&
            Objects.equals(secondary, that.secondary) &&
            Arrays.deepEquals(words, that.words);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, Arrays.hashCode(words), startTime, endTime, delay, translation, secondary);
    }

    public static final Creator<SuperLyricLine> CREATOR = new Creator<SuperLyricLine>() {
        @Override
        public SuperLyricLine createFromParcel(Parcel in) {
            return new SuperLyricLine(in);
        }

        @Override
        public SuperLyricLine[] newArray(int size) {
            return new SuperLyricLine[size];
        }
    };

    private SuperLyricLine(Parcel in) {
        text = Optional.ofNullable(in.readString()).orElse("");
        words = in.createTypedArray(SuperLyricWord.CREATOR);
        startTime = in.readLong();
        endTime = in.readLong();
        delay = in.readLong();

        if (in.dataAvail() > 0) translation = in.readString();
        if (in.dataAvail() > 0) secondary = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeTypedArray(words, flags);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeLong(delay);

        dest.writeString(translation);
        dest.writeString(secondary);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
