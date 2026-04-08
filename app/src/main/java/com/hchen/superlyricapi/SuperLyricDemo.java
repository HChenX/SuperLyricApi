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
import android.os.RemoteException;

/**
 * 示例
 *
 * @author 焕晨HChen
 */
public class SuperLyricDemo {
    public static void ModuleDemo() {
        ISuperLyricReceiver.Stub receiver;
        SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
            @Override
            public void onLyric(String publisher, SuperLyricData data) throws RemoteException {
                // 每次发布者发送新的歌词行时调用
                // 以下所有字段均为可选 — 使用前请检查
                String title = data.getTitle();
                String artist = data.getArtist();
                String album = data.getAlbum();

                if (data.hasLyric()) {
                    SuperLyricLine lyric = data.getLyric();
                    if (lyric != null) {
                        String text = lyric.getText();
                        long startTime = lyric.getStartTime(); // 毫秒
                        long endTime = lyric.getEndTime(); // 毫秒
                        long delay = lyric.getDelay(); // 持续时间 = endTime - startTime

                        // 逐字（卡拉OK）数据 — 可能为 null
                        SuperLyricWord[] words = lyric.getWords();
                        if (words != null) {
                            for (SuperLyricWord word : words) {
                                String wordText = word.getWord();
                                long wordStartTime = word.getStartTime();
                                long wordEndTime = word.getEndTime();
                            }
                        }
                    }
                }

                if (data.hasSecondary()) {
                    SuperLyricLine secondary = data.getSecondary();
                }
                if (data.hasTranslation()) {
                    SuperLyricLine translation = data.getTranslation();
                }

                if (data.hasMediaMetadata()) {
                    MediaMetadata metadata = data.getMediaMetadata();
                }
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState();
                }
                if (data.hasExtra()) {
                    Bundle extra = data.getExtra();
                }

                data.getBase64Icon(); // Base64 Icon
            }

            @Override
            public void onStop(String publisher, SuperLyricData data) throws RemoteException {
                // 当发布者暂停播放或其进程终止时调用
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState();
                }
            }
        });

        // 查询注册状态或完成后取消注册
        boolean registered = SuperLyricHelper.isReceiverRegistered(receiver);
        SuperLyricHelper.unregisterReceiver(receiver);
    }

    public static void MusicAppDemo() {
        SuperLyricHelper.isAvailable(); // 服务是否可用
        SuperLyricHelper.getApiVersion(); // API 版本

        SuperLyricHelper.registerPublisher(); // 注册本应用为发布者，必须调用，否则会抛错
        SuperLyricHelper.unregisterPublisher(); // 主动注销发布者身份，可自行调用，也可交给系统自行控制
        SuperLyricHelper.isPublisherRegistered(); // 是否已经注册为发布者

        SuperLyricHelper.sendLyric(
            new SuperLyricData()
                .setTitle("歌曲标题")
                .setArtist("艺术家名称")
                .setLyric(
                    new SuperLyricLine(
                        "你好世界", // 歌词文本
                        new SuperLyricWord[]{ // 可选的逐字数据
                            new SuperLyricWord("你好", 0, 400),
                            new SuperLyricWord("世界", 400, 900),
                        },
                        0, // 行开始时间（毫秒）
                        900 // 行结束时间（毫秒）
                    )
                )
                .setSecondary(new SuperLyricLine("副歌词行", 0, 900)) // 可选
                .setTranslation(new SuperLyricLine("翻译行", 0, 900)) // 可选
                .setMediaMetadata(null) // 可选；Bitmap 字段会被自动剥离
                .setPlaybackState(null) // 可选
                .setExtra(null) // 可选
        );

        SuperLyricHelper.sendStop(
            new SuperLyricData()
                .setPlaybackState(null) // PlaybackState 数据
        );

        SuperLyricHelper.setSystemPlayStateListenerEnabled(false);
        // 然后根据需要自行调用 sendStop()、setPlaybackState()。
    }
}
