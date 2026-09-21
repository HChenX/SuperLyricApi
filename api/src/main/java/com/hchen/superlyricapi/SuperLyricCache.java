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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 歌词缓存与增量合成管理器
 * <p>
 * 配合全量/增量传输优化机制使用：
 * <ul>
 *   <li>自动缓存包含全量歌词（{@link SuperLyricData#hasAllLyrics()}）的数据包；</li>
 *   <li>对后续收到的增量/点位数据包（Delta Payload）执行智能合成，无缝还原为包含全曲所有行的完备视图；</li>
 *   <li>当接收端被杀重启发生缓存未命中时，自动通过 {@link SuperLyricHelper#getLatestLyric()} 向系统服务拉取补偿，实现透明自愈。</li>
 * </ul>
 *
 * @author 焕晨HChen
 */
public final class SuperLyricCache {
    private static final int MAX_CACHE_SIZE = 30;
    private static final Map<String, SuperLyricData> sLyricCache = new ConcurrentHashMap<>();
    private static final Map<String, String> sMetaToIdIndex = new ConcurrentHashMap<>();

    private SuperLyricCache() {
    }

    /**
     * 将传入的歌词数据包与缓存进行合成
     * <ul>
     *   <li>若为全量数据包，则自动写入缓存并直接返回；</li>
     *   <li>若为增量数据包，则自动结合缓存补齐全量行列表（{@code allLyrics}）与曲目元数据；</li>
     *   <li>若本地缓存未命中（例如模块进程被杀重启），则自动向系统服务拉取最新的全量歌词完成自愈并合成。</li>
     * </ul>
     *
     * @param incoming 接收到的歌词数据
     * @return 经过合成后的完整 {@link SuperLyricData} 对象；若无法补齐则原样返回
     */
    @Nullable
    public static SuperLyricData resolve(@Nullable SuperLyricData incoming) {
        if (incoming == null) {
            return null;
        }

        // 1. 若本身已是全量包，直接刷新缓存并返回
        if (incoming.hasAllLyrics()) {
            put(incoming);
            return incoming;
        }

        // 2. 增量包：尝试从本地缓存中查找全量数据
        SuperLyricData cached = null;
        if (incoming.hasLyricId()) {
            cached = sLyricCache.get(incoming.getLyricId());
        }

        if (cached == null && incoming.hasTitle()) {
            String fallbackKey = buildMetaKey(incoming.getTitle(), incoming.getArtist());
            String lyricId = sMetaToIdIndex.get(fallbackKey);
            if (lyricId != null) {
                cached = sLyricCache.get(lyricId);
            }
        }

        // 3. 缓存未命中
        // 自动向常驻系统服务拉取当前正在播放的全量数据进行自愈
        if (cached == null) {
            try {
                SuperLyricData remoteLatest = SuperLyricHelper.getLatestLyric();
                if (remoteLatest != null && remoteLatest.hasAllLyrics()) {
                    put(remoteLatest);
                    // 如果拉取到的全量数据与当前增量包匹配（ID 或 标题一致）
                    if ((incoming.hasLyricId() && incoming.getLyricId().equals(remoteLatest.getLyricId()))
                        || (incoming.hasTitle() && incoming.getTitle().equals(remoteLatest.getTitle()))) {
                        cached = remoteLatest;
                    }
                }
            } catch (Throwable ignore) {
                // 服务端不可用或版本不支持主动拉取时平稳降级
            }
        }

        // 4. 若依然未找到全量缓存，则无法合成，原样降级返回
        if (cached == null) {
            return incoming;
        }

        // 5. 将增量包的点位状态与缓存的全量歌词进行安全合成
        SuperLyricData resolved = new SuperLyricData();

        // 基础元数据（优先使用 incoming，若无则使用 cached）
        resolved.setTitle(incoming.hasTitle() ? incoming.getTitle() : cached.getTitle());
        resolved.setArtist(incoming.hasArtist() ? incoming.getArtist() : cached.getArtist());
        resolved.setAlbum(incoming.hasAlbum() ? incoming.getAlbum() : cached.getAlbum());
        resolved.setLyricId(incoming.hasLyricId() ? incoming.getLyricId() : cached.getLyricId());

        // 时长与进度
        resolved.setDuration(incoming.hasDuration() ? incoming.getDuration() : cached.getDuration());
        resolved.setPosition(incoming.hasPosition() ? incoming.getPosition() : cached.getPosition());

        // 全量歌词引用
        resolved.setAllLyrics(cached.getAllLyrics());
        resolved.setCurrentLyricIndex(incoming.getCurrentLyricIndex());

        // 单行当前歌词（优先取 incoming；若无则自动根据 index 从 allLyrics 中提取）
        if (incoming.hasLyric()) {
            resolved.setLyric(incoming.getLyric());
        } else {
            SuperLyricLine currentLine = resolved.getLyricAt(resolved.getCurrentLyricIndex());
            if (currentLine != null) {
                resolved.setLyric(currentLine);
            }
        }

        // 翻译与副歌词
        if (incoming.hasTranslation()) {
            resolved.setTranslation(incoming.getTranslation());
        } else {
            SuperLyricLine currentLine = resolved.getCurrentLyric();
            if (currentLine != null && currentLine.hasTranslation()) {
                resolved.setTranslation(currentLine.getTranslationLine());
            }
        }

        if (incoming.hasSecondary()) {
            resolved.setSecondary(incoming.getSecondary());
        } else {
            SuperLyricLine currentLine = resolved.getCurrentLyric();
            if (currentLine != null && currentLine.hasSecondary()) {
                resolved.setSecondary(currentLine.getSecondaryLine());
            }
        }

        if (incoming.hasExtra()) {
            resolved.setExtra(incoming.getExtra());
        } else if (cached.hasExtra()) {
            resolved.setExtra(cached.getExtra());
        }

        return resolved;
    }

    /**
     * 手动将全量歌词数据存入缓存
     */
    public static void put(@NonNull SuperLyricData data) {
        if (!data.hasAllLyrics()) {
            return;
        }

        if (sLyricCache.size() >= MAX_CACHE_SIZE) {
            // 超出容量时简单清空最早条目或直接清理
            sLyricCache.clear();
            sMetaToIdIndex.clear();
        }

        String lyricId = data.getLyricId();
        if (lyricId == null || lyricId.isEmpty()) {
            lyricId = buildMetaKey(data.getTitle(), data.getArtist());
            data.setLyricId(lyricId);
        }

        sLyricCache.put(lyricId, data);
        if (data.hasTitle()) {
            sMetaToIdIndex.put(buildMetaKey(data.getTitle(), data.getArtist()), lyricId);
        }
    }

    /**
     * 获取指定 ID 的缓存数据
     */
    @Nullable
    public static SuperLyricData get(@Nullable String lyricId) {
        if (lyricId == null) {
            return null;
        }
        return sLyricCache.get(lyricId);
    }

    /**
     * 清空本地所有歌词缓存
     */
    public static void clear() {
        sLyricCache.clear();
        sMetaToIdIndex.clear();
    }

    /**
     * 获取当前缓存中的曲目数量
     */
    public static int size() {
        return sLyricCache.size();
    }

    @NonNull
    private static String buildMetaKey(@Nullable String title, @Nullable String artist) {
        return (title != null ? title.trim() : "") + "##" + (artist != null ? artist.trim() : "");
    }
}
