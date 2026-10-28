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
package com.hchen.superlyricapi.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.hchen.superlyricapi.SuperLyricData;
import com.hchen.superlyricapi.model.Line;
import com.hchen.superlyricapi.model.Lyric;
import com.hchen.superlyricapi.model.StopReason;
import com.hchen.superlyricapi.model.Word;
import com.hchen.superlyricapi.wire.Frame;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings("removal")
public class LegacyProjectTest {

    private static Lyric sample() {
        return new Lyric("song-1", 2000L, Arrays.asList(
            new Line("first", 0, 900),
            new Line("second", 900, 2000,
                Collections.singletonList(new Word("sec", 900, 1000)),
                "翻译", "romaji")),
            "Title", "Artist", "Album");
    }

    @Test
    public void projectFillsTheLegacyContract() {
        SuperLyricData data = LegacyV37.project(sample());

        assertEquals("song-1", data.getLyricId());
        assertEquals(2000L, data.getDuration());
        assertEquals(2, data.getAllLyricsCount());
        // 旧接收端只认单行通道，必须填
        assertNotNull(data.getLyric());
        assertEquals("first", data.getLyric().getText());
        // 旧端 played/upcoming 依赖这个下标
        assertEquals(0, data.getCurrentLyricIndex());
        // 行内翻译/副歌词走 sidecar，写端会自动取
        assertEquals("romaji", data.getAllLyrics()[1].getSecondary());
        assertEquals("翻译", data.getAllLyrics()[1].getTranslation());
    }

    @Test
    public void liftRebuildsTheDocument() {
        Frame frame = LegacyV37.lift(LegacyV37.project(sample()));

        assertNotNull(frame);
        assertEquals(Frame.Kind.FULL, frame.getKind());
        assertEquals(2, frame.getDoc().getLineCount());
        assertEquals("Artist", frame.getDoc().getArtist());
        assertEquals(1, frame.getDoc().getLines().get(1).getWords().size());
        assertEquals("romaji", frame.getDoc().getLines().get(1).getPhonetic());
    }

    @Test
    public void liftMapsDeltasToState() {
        SuperLyricData delta = new SuperLyricData()
            .setLyricId("song-1")
            .setCurrentLyricIndex(1)
            .setPosition(1500L);

        Frame frame = LegacyV37.lift(delta);

        assertEquals(Frame.Kind.STATE, frame.getKind());
        assertEquals(1, frame.getState().getIndex());
        assertEquals(1500L, frame.getState().getPosition());
    }

    @Test
    public void liftMapsAnEmptyPayloadToStop() {
        Frame frame = LegacyV37.lift(new SuperLyricData());

        assertEquals(Frame.Kind.STOP, frame.getKind());
        assertEquals(StopReason.STOPPED, frame.getStop().getReason());
    }

    @Test
    public void liftIgnoresNull() {
        assertNull(LegacyV37.lift(null));
    }
}
