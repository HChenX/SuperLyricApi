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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.hchen.superlyricapi.model.Line;
import com.hchen.superlyricapi.model.Lyric;
import com.hchen.superlyricapi.model.PlaybackState;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class LyricBuilderTest {

    @Test
    public void blankIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Lyric("", 1000, Collections.singletonList(new Line("a", 0, 1000))));
        assertThrows(NullPointerException.class,
            () -> new Lyric(null, 1000, Collections.singletonList(new Line("a", 0, 1000))));
    }

    @Test
    public void negativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Lyric("id", -1, Collections.singletonList(new Line("a", 0, 1000))));
    }

    @Test
    public void unknownDurationIsAllowed() {
        Lyric lyric = new Lyric("id", 0, Collections.singletonList(new Line("a", 0, 1000)));

        assertEquals(0L, lyric.getDuration());
    }

    @Test
    public void emptyLinesAreRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new Lyric("id", 1000, Collections.emptyList()));
    }

    @Test
    public void optionalMetadataIsOptional() {
        Lyric lyric = new Lyric("id", 1000, Collections.singletonList(new Line("a", 0, 1000)));

        assertNull(lyric.getTitle());
        assertNull(lyric.getArtist());
        assertEquals(1, lyric.getLineCount());
    }

    @Test
    public void indexAtPicksTheLastLineThatStarted() {
        Lyric lyric = new Lyric("id", 5000, Arrays.asList(
            new Line("a", 0, 1000),
            new Line("b", 1000, 2500),
            new Line("c", 2500, 5000)));

        assertEquals(-1, lyric.indexAt(-1));
        assertEquals(0, lyric.indexAt(0));
        assertEquals(0, lyric.indexAt(999));
        assertEquals(1, lyric.indexAt(1000));
        assertEquals(2, lyric.indexAt(4999));
    }

    @Test
    public void viewSlicesAroundTheCurrentLine() {
        Lyric lyric = new Lyric("id", 3000, Arrays.asList(
            new Line("a", 0, 1000),
            new Line("b", 1000, 2000),
            new Line("c", 2000, 3000)));

        LyricView view = new LyricView(lyric, 1, 1500, 1f, PlaybackState.PLAYING);

        assertEquals("b", view.getLine().getText());
        assertEquals(1, view.getPlayed().size());
        assertEquals("a", view.getPlayed().get(0).getText());
        assertEquals(1, view.getUpcoming().size());
        assertEquals("c", view.getUpcoming().get(0).getText());
    }
}
