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

import android.os.Bundle;
import android.os.RemoteException;

import java.util.List;

/**
 * SuperLyricApi 示例代码
 *
 * @author 焕晨HChen
 */
public class SuperLyricDemo {
    /**
     * Xposed 模块 / 消费端（如桌面歌词、状态栏歌词、灵动岛、智能手表等）示例
     */
    public static void ModuleDemo() {
        ISuperLyricReceiver.Stub receiver;
        SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
            @Override
            public void onLyric(String publisher, SuperLyricData rawData) throws RemoteException {
                // 推荐：使用 SuperLyricCache.resolve 自动处理全量缓存、增量包合成及模块被杀重启自愈
                SuperLyricData data = SuperLyricCache.resolve(rawData);
                if (data == null) {
                    return;
                }

                // 歌曲基础信息
                String title = data.getTitle();
                String artist = data.getArtist();
                String album = data.getAlbum();
                long duration = data.getDuration(); // 歌曲总时长（毫秒）
                long position = data.getPosition(); // 当前播放进度毫秒点位

                // 1. 获取当前正在播放的单行歌词（兼容单行模式与全曲模式）
                SuperLyricLine currentLine = data.getCurrentLyric();
                if (currentLine != null) {
                    String text = currentLine.getText();
                    long startTime = currentLine.getStartTime(); // 毫秒
                    long endTime = currentLine.getEndTime(); // 毫秒
                    String translation = currentLine.getTranslation(); // 当前行翻译文本
                    String secondary = currentLine.getSecondary(); // 当前行副歌词/罗马音

                    // 逐字（卡拉 OK）数据 — 可能为 null
                    SuperLyricWord[] words = currentLine.getWords();
                    if (words != null) {
                        for (SuperLyricWord word : words) {
                            String wordText = word.getWord();
                            long wordStartTime = word.getStartTime();
                            long wordEndTime = word.getEndTime();
                        }
                    }
                }

                // 2. 获取整首歌曲的所有歌词数据（适用于全屏歌词、多行滚动展示）
                if (data.hasAllLyrics()) {
                    SuperLyricLine[] allLines = data.getAllLyrics(); // 整首歌的所有歌词行
                    int currentIndex = data.getCurrentLyricIndex(); // 当前行在整首歌中的下标 (0-based)
                    int totalCount = data.getAllLyricsCount(); // 总行数

                    // 3. 点位切片：零开销直接提取已播放完毕和尚未播放的行
                    List<SuperLyricLine> playedLines = data.getPlayedLyrics(); // 已播放历史行列表
                    List<SuperLyricLine> upcomingLines = data.getUpcomingLyrics(); // 未播放未来行列表

                    // 安全按索引获取特定行
                    SuperLyricLine line2 = data.getLyricAt(2);
                }

                // 自定义附加数据
                if (data.hasExtra()) {
                    Bundle extra = data.getExtra();
                }
            }

            @Override
            public void onStop(String publisher, SuperLyricData data) throws RemoteException {
                // 当发布者暂停播放或其进程终止时调用
            }
        });

        // 主动拉取：若模块/悬浮窗冷启动，可直接主动获取当前正在播放曲目的全量数据（无需等待切歌或下一行）
        SuperLyricData currentPlaying = SuperLyricHelper.getLatestLyric();

        // 查询注册状态或完成后取消注册
        boolean registered = SuperLyricHelper.isReceiverRegistered(receiver);
        SuperLyricHelper.unregisterReceiver(receiver);
    }

    /**
     * 音乐应用 / Hook 发布端示例
     */
    public static void MusicAppDemo() {
        SuperLyricHelper.isAvailable(); // 服务是否可用
        SuperLyricHelper.getApiVersion(); // API 版本

        SuperLyricHelper.registerPublisher(); // 注册本应用为发布者，必须调用，否则会抛错
        SuperLyricHelper.unregisterPublisher(); // 主动注销发布者身份，可自行调用，也可交给系统自行控制
        SuperLyricHelper.isPublisherRegistered(); // 是否已经注册为发布者

        // ======================== 传输优化策略 ========================

        // 1. 歌曲初始化 / 切歌（发送全量包 Full Payload）
        SuperLyricLine[] allLines = new SuperLyricLine[]{
            new SuperLyricLine("你好世界", new SuperLyricWord[]{
                new SuperLyricWord("你好", 0, 400),
                new SuperLyricWord("世界", 400, 900),
            }, "Hello World", "Ni Hao Shi Jie", 0, 900),
            new SuperLyricLine("下一句歌词", null, "Next Line", null, 900, 2000),
        };

        SuperLyricHelper.sendFullLyric(
            new SuperLyricData()
                .setTitle("歌曲标题")
                .setArtist("艺术家名称")
                .setAlbum("专辑名称")
                .setLyricId("song_1001") // 推荐设置唯一标识，便于接收端缓存与自愈
                .setDuration(180000L) // 歌曲总时长（毫秒）
                .setPosition(0L) // 当前播放进度（毫秒）
                .setAllLyrics(allLines) // 整首歌曲的所有行
                .setCurrentLyricIndex(0) // 当前播放行下标
                .setLyric(allLines[0]) // 向下兼容旧版单行接收端
                .setExtra(null) // 可选
        );

        // 2. 播放进行中换行 / 进度更新（发送轻量增量包 Delta Payload）
        // 关键优化：allLyrics 为 null，体积仅数百字节，避免 Binder 缓冲区溢出
        SuperLyricHelper.sendLyricProgress(
            new SuperLyricData()
                .setLyricId("song_1001")
                .setCurrentLyricIndex(1) // 当前行下标
                .setPosition(950L) // 当前播放毫秒点位
                .setLyric(allLines[1]) // 向下兼容旧版接收端
        );

        // 3. 停止播放
        SuperLyricHelper.sendStop(
            new SuperLyricData()
        );

        SuperLyricHelper.setSystemPlayStateListenerEnabled(false);
        // 然后根据需要自行调用 sendStop()、setPlaybackState()。
    }
}
