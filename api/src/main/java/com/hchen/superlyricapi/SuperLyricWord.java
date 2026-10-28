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
package com.hchen.superlyricapi;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * 3.x 的逐字数据。
 *
 * @author 焕晨HChen
 * @deprecated 自 4.0 起仅用于 {@code legacy/} 投影器。新代码请使用
 * {@link com.hchen.superlyricapi.model.Word}。parcel 布局已冻结。
 */
@Deprecated(since = "4.0")
public class SuperLyricWord implements Parcelable {
    /**
     * 单词文本。
     */
    @NonNull
    private String word = "";
    /**
     * 持续时间，毫秒。
     *
     * @deprecated 由开始与结束时间推算。
     */
    @Deprecated(since = "2.7")
    private long delay = 0L;
    /**
     * 当前单词的开始时间
     */
    private long startTime = 0L;
    /**
     * 当前单词的结束时间
     */
    private long endTime = 0L;

    public SuperLyricWord(@NonNull String word, long startTime, long endTime) {
        ensureWord(word);
        this.word = word;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @deprecated 请使用 {@link SuperLyricWord#SuperLyricWord(String, long, long)}
     */
    @Deprecated(since = "2.7")
    public SuperLyricWord(@NonNull String word, long delay) {
        ensureWord(word);
        this.word = word;
        this.delay = delay;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    @Deprecated(since = "2.7")
    public long getDelay() {
        if (delay != 0L) {
            return delay;
        }

        if (endTime > startTime) {
            return endTime - startTime;
        }
        return 0L;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    private void ensureWord(String word) {
        Objects.requireNonNull(word, "Word text must not be null.");
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
        delay = in.readLong();
        startTime = in.readLong();
        endTime = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(word);
        dest.writeLong(delay);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
