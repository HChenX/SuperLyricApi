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

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 3.x 的线上数据模型。
 *
 * <p>Parcel 布局已冻结，修改字段顺序或类型会导致旧客户端解析错位。
 *
 * @author 焕晨HChen
 * @deprecated 仅用于 {@code legacy/} 中的格式转换。新代码请使用
 * {@link com.hchen.superlyricapi.model.Lyric}。
 */
@Deprecated(since = "4.0")
public class SuperLyricData implements Parcelable {
    private static final String TAG = "SuperLyricData";

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
     * 次要歌词数据，仅用于兼容旧版 API
     */
    private SuperLyricLine secondary = null;
    /**
     * 歌词翻译数据，仅用于兼容旧版 API
     */
    private SuperLyricLine translation = null;

    /**
     * 已废弃，仅作为占位字段。
     */
    @Deprecated(since = "3.3")
    private MediaMetadata mediaMetadata = null;
    /**
     * 已废弃，仅作为占位字段。
     */
    @Deprecated(since = "3.3")
    private PlaybackState playbackState = null;

    /**
     * 应用图标，Base64 编码。
     *
     * @deprecated 不再使用。
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
     * 歌词唯一标识与版本指纹
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
        if (Objects.nonNull(secondary)) {
            return true;
        }
        return lyric != null && lyric.hasSecondary();
    }

    /**
     * 是否存在歌词翻译数据
     */
    public boolean hasTranslation() {
        if (Objects.nonNull(translation)) {
            return true;
        }
        return lyric != null && lyric.hasTranslation();
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
        if (secondary != null) {
            return secondary;
        }
        if (lyric != null && lyric.hasSecondary()) {
            return lyric.getSecondaryLine();
        }
        return null;
    }

    @Nullable
    public SuperLyricLine getTranslation() {
        if (translation != null) {
            return translation;
        }
        if (lyric != null && lyric.hasTranslation()) {
            return lyric.getTranslationLine();
        }
        return null;
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

    /**
     * 设置当前播放行下标，从 0 开始
     *
     * @param currentLyricIndex 行下标；小于 -1 时归一化为 -1
     */
    public SuperLyricData setCurrentLyricIndex(int currentLyricIndex) {
        if (currentLyricIndex < -1) {
            Log.w(TAG, "setCurrentLyricIndex: " + currentLyricIndex + " normalized to -1 (unset)");
            this.currentLyricIndex = -1;
            return this;
        }
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

    /**
     * 设置当前播放进度
     *
     * @param position 播放进度，毫秒；小于 -1 时归一化为 -1
     */
    public SuperLyricData setPosition(long position) {
        if (position < -1L) {
            Log.w(TAG, "setPosition: " + position + " normalized to -1 (unset)");
            this.position = -1L;
            return this;
        }
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
     * 获取指定下标的歌词行
     *
     * @param index 下标，从 0 开始
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
     * 当前行之前的歌词行
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
     * 当前行之后的歌词行
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
     * 是否为全量数据包
     */
    public boolean isFullPayload() {
        return hasAllLyrics();
    }

    /**
     * 是否为点位数据包，不含全量歌词
     */
    public boolean isDeltaPayload() {
        return !hasAllLyrics() && (hasCurrentLyricIndex() || hasLyric() || hasPosition());
    }

    /**
     * 是否为停止或空包
     */
    public boolean isStopPayload() {
        return !hasAllLyrics() && !isDeltaPayload();
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
            extrasEqual(extra, that.extra) &&
            Objects.equals(lyricId, that.lyricId) &&
            Arrays.deepEquals(allLyrics, that.allLyrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            title, artist, album,
            lyric, secondary, translation,
            base64Icon, extrasHash(extra),
            Arrays.hashCode(allLyrics), currentLyricIndex,
            duration, position, lyricId
        );
    }

    private static boolean extrasEqual(@Nullable Bundle a, @Nullable Bundle b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        for (String key : a.keySet()) {
            if (!b.containsKey(key) || !Objects.equals(a.get(key), b.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static int extrasHash(@Nullable Bundle bundle) {
        if (bundle == null) return 0;
        int hash = 0;
        for (String key : bundle.keySet()) {
            hash += key.hashCode() ^ Objects.hashCode(bundle.get(key));
        }
        return hash;
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

        if (allLyrics != null && in.dataAvail() > 0) {
            boolean hasTrans = in.readByte() != 0;
            if (hasTrans && in.dataAvail() > 0) {
                String[] translations = in.createStringArray();
                if (translations != null) {
                    for (int i = 0; i < Math.min(translations.length, allLyrics.length); i++) {
                        if (allLyrics[i] != null) {
                            allLyrics[i].setTranslation(translations[i]);
                        }
                    }
                }
            }
            if (in.dataAvail() > 0) {
                boolean hasSec = in.readByte() != 0;
                if (hasSec && in.dataAvail() > 0) {
                    String[] secondaries = in.createStringArray();
                    if (secondaries != null) {
                        for (int i = 0; i < Math.min(secondaries.length, allLyrics.length); i++) {
                            if (allLyrics[i] != null) {
                                allLyrics[i].setSecondary(secondaries[i]);
                            }
                        }
                    }
                }
            }
        }

        // 旧版数据可能把翻译与副歌词放在 Data 上，读取时写回当前行
        if (lyric != null) {
            if (translation != null && (lyric.getTranslation() == null || lyric.getTranslation().isEmpty())) {
                lyric.setTranslation(translation.getText());
            }
            if (secondary != null && (lyric.getSecondary() == null || lyric.getSecondary().isEmpty())) {
                lyric.setSecondary(secondary.getText());
            }
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        SuperLyricLine outLyric = lyric;
        SuperLyricLine outSec = (secondary != null) ? secondary : (outLyric != null && outLyric.hasSecondary() ? outLyric.getSecondaryLine() : null);
        SuperLyricLine outTrans = (translation != null) ? translation : (outLyric != null && outLyric.hasTranslation() ? outLyric.getTranslationLine() : null);

        dest.writeParcelable(outLyric, flags);
        dest.writeParcelable(outSec, flags);
        dest.writeParcelable(outTrans, flags);
        dest.writeParcelable(mediaMetadata, flags);
        dest.writeParcelable(playbackState, flags);
        dest.writeString(base64Icon);
        dest.writeBundle(extra);

        dest.writeTypedArray(allLyrics, flags);
        dest.writeInt(currentLyricIndex);
        dest.writeLong(duration);
        dest.writeLong(position);
        dest.writeString(lyricId);

        if (allLyrics != null) {
            String[] translations = new String[allLyrics.length];
            String[] secondaries = new String[allLyrics.length];
            boolean hasTrans = false;
            boolean hasSec = false;
            for (int i = 0; i < allLyrics.length; i++) {
                if (allLyrics[i] != null) {
                    translations[i] = allLyrics[i].getTranslation();
                    if (translations[i] != null) hasTrans = true;
                    secondaries[i] = allLyrics[i].getSecondary();
                    if (secondaries[i] != null) hasSec = true;
                }
            }
            dest.writeByte((byte) (hasTrans ? 1 : 0));
            if (hasTrans) {
                dest.writeStringArray(translations);
            }
            dest.writeByte((byte) (hasSec ? 1 : 0));
            if (hasSec) {
                dest.writeStringArray(secondaries);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
