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
     * 次要歌词数据
     */
    private SuperLyricLine secondary = null;
    /**
     * 歌词翻译数据
     */
    private SuperLyricLine translation = null;

    /**
     * 当前歌曲的 MediaMetadata 数据
     * <p>
     * Note：请注意，MediaMetadata 数据内的 Bitmap 数据已被抹去
     * <p>
     * 因为部分设备传递 MediaMetadata 内 Bitmap 数据时会因为其大小超出 Binder 限制而导致 Binder 破裂
     * <p>
     * 因此 API 主动抹去 MediaMetadata 中 Bitmap 数据以规避 Binder 破裂风险
     */
    private MediaMetadata mediaMetadata = null;
    /**
     * 当前的播放状态
     * <p>
     * 建议在播放状态暂停时设置
     */
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
     * 是否存在 MediaMetadata 数据
     */
    public boolean hasMediaMetadata() {
        return Objects.nonNull(mediaMetadata);
    }

    /**
     * 是否存在 PlaybackState 数据
     */
    public boolean hasPlaybackState() {
        return Objects.nonNull(playbackState);
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

    public SuperLyricData setMediaMetadata(MediaMetadata mediaMetadata) {
        this.mediaMetadata = SuperLyricHelper.removeMediaMetadataBitmap(mediaMetadata);
        return this;
    }

    public SuperLyricData setPlaybackState(PlaybackState playbackState) {
        this.playbackState = playbackState;
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
    public MediaMetadata getMediaMetadata() {
        return mediaMetadata;
    }

    @Nullable
    public PlaybackState getPlaybackState() {
        return playbackState;
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

    /**
     * 合并已有的 SuperLyricData 的数据
     */
    @NonNull
    public SuperLyricData merge(SuperLyricData data) {
        if (data == null) return this;

        if (data.hasTitle())
            this.title = data.title;

        if (data.hasArtist())
            this.artist = data.artist;

        if (data.hasAlbum())
            this.album = data.album;

        if (data.hasLyric())
            this.lyric = data.lyric;

        if (data.hasSecondary())
            this.secondary = data.secondary;

        if (data.hasTranslation())
            this.translation = data.translation;

        if (data.hasMediaMetadata())
            this.mediaMetadata = data.mediaMetadata;

        if (data.hasPlaybackState())
            this.playbackState = data.playbackState;

        if (data.hasBase64Icon())
            this.base64Icon = data.base64Icon;

        if (data.extra != null) {
            if (this.extra == null)
                this.extra = new Bundle();
            this.extra.putAll(data.extra);
        }

        return this;
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
            ", mediaMetadata=" + mediaMetadata +
            ", playbackState=" + playbackState +
            ", base64Icon='" + base64Icon + '\'' +
            ", extra=" + extra +
            '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SuperLyricData that)) return false;
        return Objects.equals(title, that.title) &&
            Objects.equals(artist, that.artist) &&
            Objects.equals(album, that.album) &&
            Objects.equals(lyric, that.lyric) &&
            Objects.equals(secondary, that.secondary) &&
            Objects.equals(translation, that.translation) &&
            Objects.equals(mediaMetadata, that.mediaMetadata) &&
            Objects.equals(playbackState, that.playbackState) &&
            Objects.equals(base64Icon, that.base64Icon) &&
            Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            title, artist, album,
            lyric, secondary, translation,
            mediaMetadata, playbackState, base64Icon, extra
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

    private SuperLyricData(Parcel in) {
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
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
