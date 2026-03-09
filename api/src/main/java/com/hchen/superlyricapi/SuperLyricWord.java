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

import java.util.Objects;
import java.util.Optional;

/**
 * 逐字歌词数据信息
 * <p>
 * 我们将通过此数据包实现对逐字歌词的支持
 */
public class SuperLyricWord implements Parcelable {
    /**
     * 单词
     * <p>
     * 我们将使用此字段传递当前歌词的某个单词数据
     */
    @NonNull
    private String word = "";
    /**
     * 持续时间 (毫秒)
     * <p>
     * 当前单词的持续时间
     * <p>
     * 为了方便传递着使用，我们改用传递单词开始与结束时间来倒推当前单词的持续时间
     * <p>
     * 您依然可以使用此字段传递持续时间，我们也推荐使用 {@link SuperLyricWord#getDelay()} 来获取持续时间
     *
     * @deprecated
     */
    @Deprecated(since = "2.3")
    private int delay = 0;
    /**
     * 当前单词的开始时间
     */
    private int startTime = 0;
    /**
     * 当前单词的结束时间
     */
    private int endTime = 0;

    public SuperLyricWord(@NonNull String word, int startTime, int endTime) {
        if (Objects.isNull(word)) word = "";
        this.word = word;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 此构造函数已经弃用
     * <p>
     * 请使用 {@link SuperLyricWord#SuperLyricWord(String, int, int)}
     */
    @Deprecated(since = "2.6")
    public SuperLyricWord(@NonNull String word, int delay) {
        if (Objects.isNull(word)) word = "";
        this.word = word;
        this.delay = delay;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public int getDelay() {
        if (delay != 0) return delay;
        if (endTime > startTime) {
            return endTime - startTime;
        }
        return 0;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "SuperLyricWord{" +
            "word='" + word + '\'' +
            ", delay=" + getDelay() +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SuperLyricWord that)) return false;
        return delay == that.delay &&
            startTime == that.startTime &&
            endTime == that.endTime &&
            Objects.equals(word, that.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, delay, startTime, endTime);
    }

    public static final Creator<SuperLyricWord> CREATOR = new Creator<SuperLyricWord>() {
        @NonNull
        @Override
        public SuperLyricWord createFromParcel(Parcel in) {
            return new SuperLyricWord(in);
        }

        @NonNull
        @Override
        public SuperLyricWord[] newArray(int size) {
            return new SuperLyricWord[size];
        }
    };

    private SuperLyricWord(@NonNull Parcel in) {
        word = Optional.ofNullable(in.readString()).orElse("");
        delay = in.readInt();
        startTime = in.readInt();
        endTime = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeInt(delay);
        dest.writeInt(startTime);
        dest.writeInt(endTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
