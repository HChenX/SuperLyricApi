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
package com.hchen.superlyricapi.wire;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.hchen.superlyricapi.model.Line;
import com.hchen.superlyricapi.model.Lyric;
import com.hchen.superlyricapi.model.PlaybackState;
import com.hchen.superlyricapi.model.State;
import com.hchen.superlyricapi.model.Stop;
import com.hchen.superlyricapi.model.StopReason;

import org.junit.Test;

import java.util.Arrays;

public class FrameTest {

    private static Lyric sampleLyric() {
        return new Lyric("song-1", 180_000L, Arrays.asList(
            new Line("first", 0, 1000),
            new Line("second", 1000, 2000)),
            "Title", "Artist", "Album");
    }

    @Test
    public void fullFrameSurvivesTheWire() {
        State state = new State("song-1", 1, 1500L, 1.25f, PlaybackState.PLAYING);
        byte[] bytes = Frame.full(sampleLyric(), state).encode();

        Frame decoded = Frame.decode(bytes);

        assertEquals(Frame.Kind.FULL, decoded.getKind());
        assertNotNull(decoded.getDoc());
        assertEquals("song-1", decoded.getDoc().getId());
        assertEquals(2, decoded.getDoc().getLineCount());
        assertEquals("second", decoded.getDoc().getLines().get(1).getText());
        assertEquals(1.25f, decoded.getState().getSpeed(), 0.0001f);
        assertEquals(PlaybackState.PLAYING, decoded.getState().getState());
    }

    @Test
    public void stopFrameCarriesItsReason() {
        Frame decoded = Frame.decode(Frame.stop(new Stop("song-1", StopReason.CLEARED)).encode());

        assertEquals(Frame.Kind.STOP, decoded.getKind());
        assertEquals(StopReason.CLEARED, decoded.getStop().getReason());
    }

    @Test
    public void stateFrameDoesNotCarryADocument() {
        Frame decoded = Frame.decode(Frame.state(
            new State("song-1", 3, 5000L, 1f, PlaybackState.PAUSED)).encode());

        assertEquals(Frame.Kind.STATE, decoded.getKind());
        assertEquals(3, decoded.getState().getIndex());
        assertEquals(PlaybackState.PAUSED, decoded.getState().getState());
        assertNull(decoded.getDoc());
    }

    @Test
    public void badMagicIsRejected() {
        byte[] bytes = Frame.stop(new Stop("x", StopReason.STOPPED)).encode();
        bytes[0] = (byte) 0xFF;

        assertThrows(WireFormatException.class, () -> Frame.decode(bytes));
    }

    @Test
    public void shortBufferIsRejected() {
        assertThrows(WireFormatException.class, () -> Frame.decode(new byte[]{1, 2, 3}));
    }
}
