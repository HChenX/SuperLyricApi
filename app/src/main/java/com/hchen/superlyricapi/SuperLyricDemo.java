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
                // 发送方发送数据时会调用此方法
                // 以下全部数据需要调用方主动传递，否则可能为 null

                String title = data.getTitle();
                String artist = data.getArtist();
                String album = data.getAlbum();

                if (data.hasLyric()) {
                    SuperLyricLine lyric = data.getLyric(); // 歌词数据
                    if (lyric != null) {
                        String text = lyric.getText();
                        long startTime = lyric.getStartTime();
                        long endTime = lyric.getEndTime();
                        long delay = lyric.getDelay();
                    }
                }

                if (data.hasSecondary()) {
                    SuperLyricLine secondary = data.getSecondary(); // 次要歌词，用法同上
                }
                if (data.hasTranslation()) {
                    SuperLyricLine translation = data.getTranslation(); // 翻译，用法同上
                }

                if (data.hasMediaMetadata()) {
                    MediaMetadata metadata = data.getMediaMetadata(); // MediaMetadata 数据
                }
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState(); // PlaybackState 数据
                }

                if (data.hasExtra()) {
                    Bundle extra = data.getExtra(); // 附加数据
                }

                data.getBase64Icon(); // Base64 Icon
            }

            @Override
            public void onStop(String publisher, SuperLyricData data) throws RemoteException {
                // 暂停歌曲或发送方死亡时会调用此方法
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState(); // PlaybackState 数据
                }
            }
        });

        SuperLyricHelper.isReceiverRegistered(receiver); // 是否已经注册
        SuperLyricHelper.unregisterReceiver(receiver); // 解除注册
    }

    public static void MusicAppDemo() {
        SuperLyricHelper.isAvailable(); // 服务是否可用
        SuperLyricHelper.getApiVersion(); // API 版本

        SuperLyricHelper.registerPublisher(); // 注册本应用为发布者，必须调用，否则会抛错
        SuperLyricHelper.unregisterPublisher(); // 主动注销发布者身份，可自行调用，也可交给系统自行控制
        SuperLyricHelper.isPublisherRegistered(); // 是否已经注册为发布者

        SuperLyricHelper.sendLyric(
            new SuperLyricData()
                .setTitle("") // 设置歌曲标题
                .setArtist("") // 设置歌曲艺术家
                // 设置当前歌词
                .setLyric(
                    new SuperLyricLine(
                        "lyric",
                        // 设置逐字数据
                        new SuperLyricWord[]{
                            new SuperLyricWord(
                                "l",
                                100,
                                200
                            ),
                            // 省略
                        },
                        100,
                        600
                    )
                )
                // 设置当前次要歌词
                .setSecondary(
                    new SuperLyricLine(
                        "secondary",
                        100,
                        600
                    )
                )
                // 设置当前翻译
                .setTranslation(
                    new SuperLyricLine(
                        "translation",
                        100,
                        600
                    )
                )
                .setMediaMetadata(null) // MediaMetadata 数据
                .setPlaybackState(null) // PlaybackState 数据
                .setExtra(null) // 设置附加数据
                .setBase64Icon("") // Base64 Icon
        );

        SuperLyricHelper.sendStop(
            new SuperLyricData()
                .setPlaybackState(null) // PlaybackState 数据
        );
    }
}
