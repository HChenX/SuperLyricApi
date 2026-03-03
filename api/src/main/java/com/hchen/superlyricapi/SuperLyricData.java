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
 * Copyright (C) 2023–2025 HChenX
 */
package com.hchen.superlyricapi;

import static android.media.MediaMetadata.METADATA_KEY_ALBUM;
import static android.media.MediaMetadata.METADATA_KEY_ARTIST;
import static android.media.MediaMetadata.METADATA_KEY_TITLE;

import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * 歌曲数据
 *
 * @author 焕晨HChen
 */
public class SuperLyricData implements Parcelable {
    /**
     * 歌词
     */
    @NonNull
    private String lyric = "";
    /**
     * 音乐软件的包名
     * <p>
     * 请务必设置包名，这是判断当前播放应用的唯一途径
     */
    @NonNull
    private String packageName = "";
    /**
     * 音乐软件的图标
     * <p>
     * Note：用途有限，要求传递方提供 Base64 样式的 Icon 不太现实
     * <p>
     * 如果愿意您依然可以使用此方法传递 Base64 样式的 Icon
     *
     * @deprecated
     */
    @NonNull
    @Deprecated(since = "1.8")
    private String base64Icon = "";
    /**
     * 当前歌词的持续时间 (毫秒)
     */
    private int delay = 0;
    /**
     * 当前歌曲的 MediaMetadata 数据
     * <p>
     * Note：请注意，MediaMetadata 数据内的 Bitmap 数据已被抹去
     * <p>
     * 因为部分设备传递 MediaMetadata 内 Bitmap 数据时会因为其大小超出 Binder 限制而导致 Binder 破裂
     * <p>
     * 因此 API 主动抹去 MediaMetadata 中 Bitmap 数据以规避 Binder 破裂风险
     */
    @Nullable
    private MediaMetadata mediaMetadata;
    /**
     * 当前的播放状态
     * <p>
     * 建议在播放状态暂停时设置
     */
    @Nullable
    private PlaybackState playbackState;
    /**
     * 自定义附加数据
     */
    @Nullable
    private Bundle extra;
    /**
     * Extra 包中用于存储次要歌词数据的 Key 值
     * <p>
     * 我们将使用此 Key 存储次要歌词数据
     */
    private static final String KEY_SECONDARY_LYRIC = "key_secondary_lyric";
    /**
     * Extra 包中用于存储次要歌词持续时间的 Key 值
     * <p>
     * 我们将使用此 Key 存储次要歌词的持续时间
     */
    private static final String KEY_SECONDARY_LYRIC_DELAY = "key_secondary_lyric_delay";
    /**
     * Extra 包中用于存储次要歌词逐字数据的 Key 值
     * <p>
     * 我们将使用此 Key 存储次要歌词的逐字数据
     */
    private static final String KEY_SECONDARY_LYRIC_ENHANCED_LRC_DATA = "key_secondary_lyric_enhanced_lrc_data";
    /**
     * Extra 包中用于存储歌词翻译数据的 Key 值
     * <p>
     * 我们将使用此 Key 存储歌词翻译数据
     */
    private static final String KEY_TRANSLATION = "key_translation";
    /**
     * Extra 包中用于存储歌词翻译持续时间的 Key 值
     * <p>
     * 我们将使用此 Key 存储歌词翻译的持续时间
     */
    private static final String KEY_TRANSLATION_DELAY = "key_translation_delay";
    /**
     * Extra 包中用于储存歌词翻译逐字数据的 key 值
     * <p>
     * 我们将使用此 Key 存储歌词翻译的逐字数据
     */
    private static final String KEY_TRANSLATION_ENHANCED_LRC_DATA = "key_translation_enhanced_lrc_data";
    /**
     * Extra 包中用于存储逐字歌词数据的 Key 值
     * <p>
     * 我们将使用此 Key 存储逐字歌词数据
     */
    private static final String KEY_ENHANCED_LRC_DATA = "key_enhanced_lrc_data";

    public SuperLyricData() {
    }

    /**
     * 是否存在歌词数据
     */
    public boolean isExistLyric() {
        return !lyric.isEmpty();
    }

    /**
     * 是否存在次要歌词数据
     */
    public boolean isExistSecondaryLyric() {
        return extra != null && extra.containsKey(KEY_SECONDARY_LYRIC);
    }

    /**
     * 是否存在次要歌词持续时间数据
     */
    public boolean isExistSecondaryLyricDelay() {
        return extra != null && extra.containsKey(KEY_SECONDARY_LYRIC_DELAY);
    }

    /**
     * 是否存在次要歌词逐字数据
     */
    public boolean isExistSecondaryLyricEnhancedLRCData() {
        return extra != null && extra.containsKey(KEY_SECONDARY_LYRIC_ENHANCED_LRC_DATA);
    }

    /**
     * 是否存在翻译数据
     */
    public boolean isExistTranslation() {
        return extra != null && extra.containsKey(KEY_TRANSLATION);
    }

    /**
     * 是否存在翻译持续时间数据
     */
    public boolean isExistTranslationDelay() {
        return extra != null && extra.containsKey(KEY_TRANSLATION_DELAY);
    }

    /**
     * 是否存在翻译逐字数据
     */
    public boolean isExistTranslationEnhancedLRCData() {
        return extra != null && extra.containsKey(KEY_TRANSLATION_ENHANCED_LRC_DATA);
    }

    /**
     * 是否存在逐字歌词数据
     */
    public boolean isExistEnhancedLRCData() {
        return extra != null && extra.containsKey(KEY_ENHANCED_LRC_DATA);
    }

    /**
     * 是否存在包名数据
     */
    public boolean isExistPackageName() {
        return !packageName.isEmpty();
    }

    /**
     * 是否存在 Delay 数据
     */
    public boolean isExistDelay() {
        return delay > 0;
    }

    /**
     * 是否存在 Base64 Icon 数据
     *
     * @deprecated
     */
    @Deprecated(since = "1.8")
    public boolean isExistBase64Icon() {
        return !base64Icon.isEmpty();
    }

    /**
     * 是否存在 MediaMetadata 数据
     */
    public boolean isExistMediaMetadata() {
        return Objects.nonNull(mediaMetadata);
    }

    /**
     * 是否存在 PlaybackState 数据
     */
    public boolean isExistPlaybackState() {
        return Objects.nonNull(playbackState);
    }

    /**
     * 是否存在附加数据
     */
    public boolean isExistExtra() {
        return Objects.nonNull(extra);
    }

    public SuperLyricData setLyric(String lyric) {
        if (Objects.isNull(lyric)) lyric = "";
        this.lyric = lyric;
        return this;
    }

    public SuperLyricData setSecondaryLyric(String secondaryLyric) {
        if (Objects.nonNull(secondaryLyric)) {
            if (this.extra == null) this.extra = new Bundle();
            this.extra.putString(KEY_SECONDARY_LYRIC, secondaryLyric);
        }
        return this;
    }

    public SuperLyricData setSecondaryLyricDelay(int delay) {
        if (this.extra == null) this.extra = new Bundle();
        this.extra.putInt(KEY_SECONDARY_LYRIC_DELAY, delay);
        return this;
    }

    public SuperLyricData setSecondaryLyricEnhancedLRCData(EnhancedLRCData[] data) {
        if (Objects.nonNull(data)) {
            if (this.extra == null) this.extra = new Bundle();
            this.extra.putParcelableArray(KEY_SECONDARY_LYRIC_ENHANCED_LRC_DATA, data);
        }
        return this;
    }

    public SuperLyricData setTranslation(String translation) {
        if (Objects.nonNull(translation)) {
            if (this.extra == null) this.extra = new Bundle();
            this.extra.putString(KEY_TRANSLATION, translation);
        }
        return this;
    }

    public SuperLyricData setTranslationDelay(int delay) {
        if (this.extra == null) this.extra = new Bundle();
        this.extra.putInt(KEY_TRANSLATION_DELAY, delay);
        return this;
    }

    public SuperLyricData setTranslationEnhancedLRCData(EnhancedLRCData[] data) {
        if (Objects.nonNull(data)) {
            if (this.extra == null) this.extra = new Bundle();
            this.extra.putParcelableArray(KEY_TRANSLATION_ENHANCED_LRC_DATA, data);
        }
        return this;
    }

    public SuperLyricData setEnhancedLRCData(EnhancedLRCData[] data) {
        if (Objects.nonNull(data)) {
            if (this.extra == null) this.extra = new Bundle();
            this.extra.putParcelableArray(KEY_ENHANCED_LRC_DATA, data);
        }
        return this;
    }

    public SuperLyricData setPackageName(String packageName) {
        if (Objects.isNull(packageName)) packageName = "";
        this.packageName = packageName;
        return this;
    }

    @Deprecated(since = "1.8")
    public SuperLyricData setBase64Icon(String base64Icon) {
        if (Objects.isNull(base64Icon)) base64Icon = "";
        this.base64Icon = base64Icon;
        return this;
    }

    public SuperLyricData setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public SuperLyricData setMediaMetadata(MediaMetadata mediaMetadata) {
        this.mediaMetadata = SuperLyricTool.removeMediaMetadataBitmap(mediaMetadata);
        return this;
    }

    public SuperLyricData setPlaybackState(PlaybackState playbackState) {
        this.playbackState = playbackState;
        return this;
    }

    public SuperLyricData setExtra(Bundle extra) {
        if (this.extra == null) this.extra = extra;
        else {
            if (extra != null) {
                this.extra.putAll(extra);
            }
        }
        return this;
    }

    @NonNull
    public String getLyric() {
        return lyric;
    }

    @Nullable
    public String getSecondaryLyric() {
        return extra != null ? extra.getString(KEY_SECONDARY_LYRIC) : null;
    }

    public int getSecondaryLyricDelay() {
        return extra != null ? extra.getInt(KEY_SECONDARY_LYRIC_DELAY) : 0;
    }

    @Nullable
    public EnhancedLRCData[] getSecondaryLyricEnhancedLRCData() {
        if (extra == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return extra.getParcelableArray(KEY_SECONDARY_LYRIC_ENHANCED_LRC_DATA, EnhancedLRCData.class);
        else
            return (EnhancedLRCData[]) extra.getParcelableArray(KEY_SECONDARY_LYRIC_ENHANCED_LRC_DATA);
    }

    @Nullable
    public String getTranslation() {
        return extra != null ? extra.getString(KEY_TRANSLATION) : null;
    }

    public int getTranslationDelay() {
        return extra != null ? extra.getInt(KEY_TRANSLATION_DELAY) : 0;
    }

    @Nullable
    public EnhancedLRCData[] getTranslationEnhancedLRCData() {
        if (extra == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return extra.getParcelableArray(KEY_TRANSLATION_ENHANCED_LRC_DATA, EnhancedLRCData.class);
        else return (EnhancedLRCData[]) extra.getParcelableArray(KEY_TRANSLATION_ENHANCED_LRC_DATA);
    }

    @Nullable
    public EnhancedLRCData[] getEnhancedLRCData() {
        if (extra == null) return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return extra.getParcelableArray(KEY_ENHANCED_LRC_DATA, EnhancedLRCData.class);
        else return (EnhancedLRCData[]) extra.getParcelableArray(KEY_ENHANCED_LRC_DATA);
    }

    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @NonNull
    @Deprecated(since = "1.8")
    public String getBase64Icon() {
        return base64Icon;
    }

    public int getDelay() {
        return delay;
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
    public Bundle getExtra() {
        return extra;
    }

    /**
     * 获取歌曲的标题，数据来自 MediaMetadata
     * <br/>
     * 请注意，蓝牙歌词状态可能使用此参数传递歌词
     */
    @NonNull
    public String getTitle() {
        if (mediaMetadata == null) return "Unknown";

        return Optional.ofNullable(
            mediaMetadata.getString(METADATA_KEY_TITLE)
        ).orElse("Unknown");
    }

    /**
     * 获取歌曲的艺术家，数据来自 MediaMetadata
     */
    @NonNull
    public String getArtist() {
        if (mediaMetadata == null) return "Unknown";

        return Optional.ofNullable(
            mediaMetadata.getString(METADATA_KEY_ARTIST)
        ).orElse("Unknown");
    }

    /**
     * 获取歌曲的专辑，数据来自 MediaMetadata
     */
    @NonNull
    public String getAlbum() {
        if (mediaMetadata == null) return "Unknown";

        return Optional.ofNullable(
            mediaMetadata.getString(METADATA_KEY_ALBUM)
        ).orElse("Unknown");
    }

    /**
     * 合并已有的 SuperLyricData 的数据
     */
    @NonNull
    public SuperLyricData merge(SuperLyricData data) {
        if (data == null) return this;

        if (data.isExistLyric())
            this.lyric = data.lyric;

        if (data.isExistPackageName())
            this.packageName = data.packageName;

        if (data.isExistDelay())
            this.delay = data.delay;

        if (data.isExistMediaMetadata())
            this.mediaMetadata = data.mediaMetadata;

        if (data.isExistPlaybackState())
            this.playbackState = data.playbackState;

        if (data.extra != null) {
            if (this.extra == null)
                this.extra = new Bundle();
            this.extra.putAll(data.extra);
        }

        if (data.isExistBase64Icon())
            this.base64Icon = data.base64Icon;

        return this;
    }

    /**
     * 方便手动封装包裹
     */
    @NonNull
    public Parcel marshall() {
        Parcel parcel = Parcel.obtain();
        writeToParcel(parcel, 0);
        return parcel;
    }

    /**
     * 解包封装并实例化
     */
    @NonNull
    public static SuperLyricData unmarshall(@NonNull Parcel parcel) {
        parcel.setDataPosition(0);
        return new SuperLyricData(parcel);
    }

    @NonNull
    @Override
    public String toString() {
        return "SuperLyricData{" +
            "lyric='" + lyric + '\'' +
            ", packageName='" + packageName + '\'' +
            ", base64Icon='" + base64Icon + '\'' +
            ", delay=" + delay +
            ", mediaMetadata=" + mediaMetadata +
            ", playbackState=" + playbackState +
            ", extra=" + extra +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SuperLyricData data)) return false;
        return delay == data.delay &&
            Objects.equals(lyric, data.lyric) &&
            Objects.equals(packageName, data.packageName) &&
            Objects.equals(base64Icon, data.base64Icon) &&
            Objects.equals(mediaMetadata, data.mediaMetadata) &&
            Objects.equals(playbackState, data.playbackState) &&
            Objects.equals(extra, data.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lyric, packageName, base64Icon, delay, mediaMetadata, playbackState, extra);
    }

    public static final Creator<SuperLyricData> CREATOR = new Creator<SuperLyricData>() {
        @NonNull
        @Override
        public SuperLyricData createFromParcel(Parcel in) {
            return new SuperLyricData(in);
        }

        @NonNull
        @Override
        public SuperLyricData[] newArray(int size) {
            return new SuperLyricData[size];
        }
    };

    private SuperLyricData(@NonNull Parcel in) {
        lyric = Optional.ofNullable(in.readString()).orElse("");
        packageName = Optional.ofNullable(in.readString()).orElse("");
        base64Icon = Optional.ofNullable(in.readString()).orElse("");
        delay = in.readInt();
        mediaMetadata = in.readParcelable(MediaMetadata.class.getClassLoader());
        playbackState = in.readParcelable(PlaybackState.class.getClassLoader());
        extra = in.readBundle(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(lyric);
        dest.writeString(packageName);
        dest.writeString(base64Icon);
        dest.writeInt(delay);
        dest.writeParcelable(mediaMetadata, flags);
        dest.writeParcelable(playbackState, flags);
        dest.writeBundle(extra);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 逐字歌词数据信息
     * <p>
     * 我们将通过此数据包实现对逐字歌词的支持
     */
    public static class EnhancedLRCData implements Parcelable {
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
         * 您依然可以使用此字段传递持续时间，我们也推荐使用 {@link EnhancedLRCData#getDelay()} 来获取持续时间
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

        public EnhancedLRCData(@NonNull String word, int startTime, int endTime) {
            if (Objects.isNull(word)) word = "";
            this.word = word;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public EnhancedLRCData(@NonNull String word, int delay) {
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
            return "EnhancedLRCData{" +
                "word='" + word + '\'' +
                ", delay=" + delay +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EnhancedLRCData that)) return false;
            return delay == that.delay &&
                startTime == that.startTime &&
                endTime == that.endTime &&
                Objects.equals(word, that.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, delay, startTime, endTime);
        }

        public static final Creator<EnhancedLRCData> CREATOR = new Creator<EnhancedLRCData>() {
            @NonNull
            @Override
            public EnhancedLRCData createFromParcel(Parcel in) {
                return new EnhancedLRCData(in);
            }

            @NonNull
            @Override
            public EnhancedLRCData[] newArray(int size) {
                return new EnhancedLRCData[size];
            }
        };

        private EnhancedLRCData(@NonNull Parcel in) {
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
}
