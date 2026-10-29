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

import com.hchen.superlyricapi.model.Line;
import com.hchen.superlyricapi.model.Lyric;
import com.hchen.superlyricapi.model.PlaybackState;
import com.hchen.superlyricapi.model.Stop;
import com.hchen.superlyricapi.model.Word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SuperLyricApi 使用示例。
 *
 * @author 焕晨HChen
 */
public class SuperLyricDemo {
    /**
     * 接收端示例。
     */
    public static void receiverDemo() {
        SuperLyric.receiver().register(new LyricListener() {
            @Override
            public void onLyric(LyricView view) {
                // view 已是合成后的完整状态
                Lyric doc = view.getLyric();
                String title = doc.getTitle();
                String artist = doc.getArtist();
                long duration = doc.getDuration();
                int total = doc.getLineCount();

                long position = view.getPosition();
                float speed = view.getSpeed();
                // PLAYING / PAUSED / BUFFERING / STOPPED / ERROR
                PlaybackState state = view.getState();

                // 当前行
                Line line = view.getLine();
                if (line != null) {
                    String text = line.getText();
                    long start = line.getStart();
                    long end = line.getEnd();
                    String translation = line.getTranslation();
                    String phonetic = line.getPhonetic();

                    List<Word> words = line.getWords(); // 可能为 null
                    if (words != null) {
                        for (Word word : words) {
                            String w = word.getText();
                            long ws = word.getStart();
                            long we = word.getEnd();
                        }
                    }
                }

                // 整首歌词，以及当前行前后的切片
                List<Line> all = doc.getLines();
                int index = view.getIndex();
                List<Line> played = view.getPlayed();
                List<Line> upcoming = view.getUpcoming();
            }

            @Override
            public void onStop(Stop stop, String publisher) {
                // 原因：PAUSED / STOPPED / ERROR / PROCESS_DIED / CLEARED
            }
        });

        // 冷启动时主动获取
        LyricView current = SuperLyric.receiver().latest();

        SuperLyric.receiver().unregisterAll();
    }

    /**
     * 发布端示例。
     */
    public static void publisherDemo() {
        if (!SuperLyric.isAvailable()) {
            return;
        }

        List<Line> lines = new ArrayList<>();
        lines.add(new Line("你好世界", 0, 900,
            Arrays.asList(new Word("你好", 0, 400), new Word("世界", 400, 900)),
            "Hello World",
            "Ni Hao Shi Jie"));
        lines.add(new Line("下一句歌词", 900, 2000, null, "Next Line", null));

        // id、duration、lines 为必填项，其余为选填项
        Lyric doc = new Lyric("song_1001", 180_000L, lines, "歌曲标题", "艺术家名称", "专辑名称");

        Publisher.PublishResult result = SuperLyric.publisher().publish(doc);
        if (result != Publisher.PublishResult.OK) {
            // 处理 SERVICE_UNAVAILABLE、PAYLOAD_TOO_LARGE、REJECTED
        }

        // 切歌时再次调用 publish；仅在需要立即清除时调用 clear()
        // SuperLyric.publisher().clear();
    }
}
