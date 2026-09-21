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

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 歌词数据
 *
 * @author 焕晨HChen
 */
public class SuperLyricData implements Parcelable {
    /**
     * 歌曲的标题
     */
    private String title = null;
    /**
     * 歌曲的艺术家
     */
    private String artist = null;
    /**
     * 歌曲的专辑
     */
    private String album = null;

    /**
     * 歌词数据
     */
    private SuperLyricLine lyric = null;
    /**
     * 次要歌词数据，仅用于兼容旧版 API 或仅传递单行歌词的情况
     */
    private SuperLyricLine secondary = null;
    /**
     * 歌词翻译数据，仅用于兼容旧版 API 或仅传递单行歌词的情况
     */
    private SuperLyricLine translation = null;

    /**
     * 当前歌曲的 MediaMetadata 数据
     * <p>
     * 此参数已被废弃，仅作为占位存在，用以保证 API 稳定性
     */
    @Deprecated(since = "3.3")
    private MediaMetadata mediaMetadata = null;
    /**
     * 当前的播放状态
     * <p>
     * 此参数已被废弃，仅作为占位存在，用以保证 API 稳定性
     */
    @Deprecated(since = "3.3")
    private PlaybackState playbackState = null;

    /**
     * 音乐软件的图标
     * <p>
     * Note：用途有限，要求传递方提供 Base64 样式的 Icon 不现实
     * <p>
     * 如果愿意您依然可以使用此方法传递 Base64 样式的 Icon
     *
     * @deprecated
     */
    @Deprecated(since = "1.8")
    private String base64Icon = null;
    /**
     * 自定义附加数据
     */
    private Bundle extra = null;

    /**
     * 歌曲的所有歌词行数据
     */
    @Nullable
    private SuperLyricLine[] allLyrics = null;
    /**
     * 当前正在播放的歌词行索引（以 allLyrics 为基准，从 0 开始）
     */
    private int currentLyricIndex = -1;
    /**
     * 歌曲总时长（毫秒）
     */
    private long duration = 0L;
    /**
     * 当前播放进度点位（毫秒）
     */
    private long position = -1L;
    /**
     * 歌词全局唯一标识/版本指纹（用于客户端缓存匹配）
     */
    @Nullable
    private String lyricId = null;

    public SuperLyricData() {
    }

    /**
     * 是否存在歌曲标题数据
     */
    public boolean hasTitle() {
        return Objects.nonNull(title);
    }

    /**
     * 是否存在歌曲艺术家数据
     */
    public boolean hasArtist() {
        return Objects.nonNull(artist);
    }

    /**
     * 是否存在歌曲专辑数据
     */
    public boolean hasAlbum() {
        return Objects.nonNull(album);
    }

    /**
     * 是否存在歌词数据
     */
    public boolean hasLyric() {
        return Objects.nonNull(lyric);
    }

    /**
     * 是否存在次要歌词数据
     */
    public boolean hasSecondary() {
        return Objects.nonNull(secondary);
    }

    /**
     * 是否存在歌词翻译数据
     */
    public boolean hasTranslation() {
        return Objects.nonNull(translation);
    }

    /**
     * 是否存在 Base64 Icon 数据
     *
     * @deprecated
     */
    @Deprecated(since = "1.8")
    public boolean hasBase64Icon() {
        return Objects.nonNull(base64Icon);
    }

    /**
     * 是否存在附加数据
     */
    public boolean hasExtra() {
        return Objects.nonNull(extra);
    }

    /**
     * 是否存在全量歌词数据
     */
    public boolean hasAllLyrics() {
        return Objects.nonNull(allLyrics) && allLyrics.length > 0;
    }

    /**
     * 是否存在当前播放歌词行索引
     */
    public boolean hasCurrentLyricIndex() {
        return currentLyricIndex >= 0;
    }

    /**
     * 是否存在歌曲总时长数据
     */
    public boolean hasDuration() {
        return duration > 0L;
    }

    /**
     * 是否存在当前播放进度点位数据
     */
    public boolean hasPosition() {
        return position >= 0L;
    }

    /**
     * 是否存在歌词唯一标识/指纹
     */
    public boolean hasLyricId() {
        return Objects.nonNull(lyricId) && !lyricId.isEmpty();
    }

    public SuperLyricData setTitle(String title) {
        this.title = title;
        return this;
    }

    public SuperLyricData setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public SuperLyricData setAlbum(String album) {
        this.album = album;
        return this;
    }

    public SuperLyricData setLyric(SuperLyricLine lyric) {
        this.lyric = lyric;
        return this;
    }

    public SuperLyricData setSecondary(SuperLyricLine secondary) {
        this.secondary = secondary;
        return this;
    }

    public SuperLyricData setTranslation(SuperLyricLine translation) {
        this.translation = translation;
        return this;
    }

    @Deprecated(since = "1.8")
    public SuperLyricData setBase64Icon(String base64Icon) {
        this.base64Icon = base64Icon;
        return this;
    }

    public SuperLyricData setExtra(Bundle extra) {
        if (this.extra == null) {
            if (extra != null) {
                this.extra = new Bundle(extra);
            }
        } else {
            if (extra != null) {
                this.extra.putAll(extra);
            }
        }
        return this;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nullable
    public SuperLyricLine getLyric() {
        return lyric;
    }

    @Nullable
    public SuperLyricLine getSecondary() {
        return secondary;
    }

    @Nullable
    public SuperLyricLine getTranslation() {
        return translation;
    }

    @Nullable
    @Deprecated(since = "1.8")
    public String getBase64Icon() {
        return base64Icon;
    }

    @Nullable
    public Bundle getExtra() {
        return extra;
    }

    public SuperLyricData setAllLyrics(@Nullable SuperLyricLine[] allLyrics) {
        this.allLyrics = allLyrics;
        return this;
    }

    public SuperLyricData setAllLyrics(@Nullable List<SuperLyricLine> allLyrics) {
        if (allLyrics == null) {
            this.allLyrics = null;
        } else {
            this.allLyrics = allLyrics.toArray(new SuperLyricLine[0]);
        }
        return this;
    }

    @Nullable
    public SuperLyricLine[] getAllLyrics() {
        return allLyrics;
    }

    @NonNull
    public List<SuperLyricLine> getAllLyricsList() {
        if (allLyrics == null || allLyrics.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(allLyrics);
    }

    public int getAllLyricsCount() {
        return allLyrics != null ? allLyrics.length : 0;
    }

    public SuperLyricData setCurrentLyricIndex(int currentLyricIndex) {
        this.currentLyricIndex = currentLyricIndex;
        return this;
    }

    public int getCurrentLyricIndex() {
        return currentLyricIndex;
    }

    public SuperLyricData setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public SuperLyricData setPosition(long position) {
        this.position = position;
        return this;
    }

    public long getPosition() {
        return position;
    }

    public SuperLyricData setLyricId(@Nullable String lyricId) {
        this.lyricId = lyricId;
        return this;
    }

    @Nullable
    public String getLyricId() {
        return lyricId;
    }

    /**
     * 获取当前正在播放的歌词行
     * <p>
     * 优先返回已设置的单行 {@link #getLyric()}；
     * 若未设置，但存在全量歌词且当前行索引有效，则自动从全量列表中获取对应行
     */
    @Nullable
    public SuperLyricLine getCurrentLyric() {
        if (lyric != null) {
            return lyric;
        }
        if (allLyrics != null && currentLyricIndex >= 0 && currentLyricIndex < allLyrics.length) {
            return allLyrics[currentLyricIndex];
        }
        return null;
    }

    /**
     * 安全获取指定索引位置的歌词行
     *
     * @param index 索引下标（从 0 开始）
     * @return 对应歌词行，若越界或无全量歌词则返回 null
     */
    @Nullable
    public SuperLyricLine getLyricAt(int index) {
        if (allLyrics != null && index >= 0 && index < allLyrics.length) {
            return allLyrics[index];
        }
        return null;
    }

    /**
     * 获取已播放完毕的历史歌词行列表（从第 0 行到当前行前一行）
     */
    @NonNull
    public List<SuperLyricLine> getPlayedLyrics() {
        if (allLyrics == null || allLyrics.length == 0 || currentLyricIndex <= 0) {
            return Collections.emptyList();
        }
        int endIndex = Math.min(currentLyricIndex, allLyrics.length);
        List<SuperLyricLine> played = new ArrayList<>(endIndex);
        for (int i = 0; i < endIndex; i++) {
            played.add(allLyrics[i]);
        }
        return played;
    }

    /**
     * 获取尚未播放的未来歌词行列表（从当前行后一行到末尾）
     */
    @NonNull
    public List<SuperLyricLine> getUpcomingLyrics() {
        if (allLyrics == null || allLyrics.length == 0 || currentLyricIndex < 0 || currentLyricIndex >= allLyrics.length - 1) {
            return Collections.emptyList();
        }
        int startIndex = currentLyricIndex + 1;
        List<SuperLyricLine> upcoming = new ArrayList<>(allLyrics.length - startIndex);
        for (int i = startIndex; i < allLyrics.length; i++) {
            upcoming.add(allLyrics[i]);
        }
        return upcoming;
    }

    /**
     * 是否为全量歌词数据包（包含整首歌曲的所有行）
     */
    public boolean isFullPayload() {
        return hasAllLyrics();
    }

    /**
     * 是否为增量/点位数据包（不含整首歌曲，仅提供点位或单行更新）
     */
    public boolean isDeltaPayload() {
        return !hasAllLyrics() && hasLyricId();
    }

    @NonNull
    @Override
    public String toString() {
        return "SuperLyricData{" +
            "title='" + title + '\'' +
            ", artist='" + artist + '\'' +
            ", album='" + album + '\'' +
            ", lyric=" + lyric +
            ", secondary=" + secondary +
            ", translation=" + translation +
            ", base64Icon='" + base64Icon + '\'' +
            ", extra=" + extra +
            ", allLyricsCount=" + (allLyrics != null ? allLyrics.length : 0) +
            ", currentLyricIndex=" + currentLyricIndex +
            ", duration=" + duration +
            ", position=" + position +
            ", lyricId='" + lyricId + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SuperLyricData that)) return false;
        return currentLyricIndex == that.currentLyricIndex &&
            duration == that.duration &&
            position == that.position &&
            Objects.equals(title, that.title) &&
            Objects.equals(artist, that.artist) &&
            Objects.equals(album, that.album) &&
            Objects.equals(lyric, that.lyric) &&
            Objects.equals(secondary, that.secondary) &&
            Objects.equals(translation, that.translation) &&
            Objects.equals(base64Icon, that.base64Icon) &&
            Objects.equals(extra, that.extra) &&
            Objects.equals(lyricId, that.lyricId) &&
            Arrays.deepEquals(allLyrics, that.allLyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            title, artist, album,
            lyric, secondary, translation,
            base64Icon, extra,
            Arrays.hashCode(allLyrics), currentLyricIndex,
            duration, position, lyricId
        );
    }

    public static final Creator<SuperLyricData> CREATOR = new Creator<SuperLyricData>() {
        @Override
        public SuperLyricData createFromParcel(Parcel in) {
            return new SuperLyricData(in);
        }

        @Override
        public SuperLyricData[] newArray(int size) {
            return new SuperLyricData[size];
        }
    };

    private SuperLyricData(@NonNull Parcel in) {
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        lyric = in.readParcelable(SuperLyricLine.class.getClassLoader());
        secondary = in.readParcelable(SuperLyricLine.class.getClassLoader());
        translation = in.readParcelable(SuperLyricLine.class.getClassLoader());
        mediaMetadata = in.readParcelable(MediaMetadata.class.getClassLoader());
        playbackState = in.readParcelable(PlaybackState.class.getClassLoader());
        base64Icon = in.readString();
        extra = in.readBundle(SuperLyricData.class.getClassLoader());

        if (in.dataAvail() > 0) allLyrics = in.createTypedArray(SuperLyricLine.CREATOR);
        if (in.dataAvail() > 0) currentLyricIndex = in.readInt();
        if (in.dataAvail() > 0) duration = in.readLong();
        if (in.dataAvail() > 0) position = in.readLong();
        if (in.dataAvail() > 0) lyricId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeParcelable(lyric, flags);
        dest.writeParcelable(secondary, flags);
        dest.writeParcelable(translation, flags);
        dest.writeParcelable(mediaMetadata, flags);
        dest.writeParcelable(playbackState, flags);
        dest.writeString(base64Icon);
        dest.writeBundle(extra);

        dest.writeTypedArray(allLyrics, flags);
        dest.writeInt(currentLyricIndex);
        dest.writeLong(duration);
        dest.writeLong(position);
        dest.writeString(lyricId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
