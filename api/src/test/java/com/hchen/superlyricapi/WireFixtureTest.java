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

import com.hchen.superlyricapi.model.Line;
import com.hchen.superlyricapi.model.Lyric;
import com.hchen.superlyricapi.model.PlaybackState;
import com.hchen.superlyricapi.model.State;
import com.hchen.superlyricapi.model.Stop;
import com.hchen.superlyricapi.model.StopReason;
import com.hchen.superlyricapi.model.Word;
import com.hchen.superlyricapi.wire.Frame;

import org.junit.Test;

import java.util.Arrays;

/**
 * 固定线上字节向量。线上格式一经发布，这里的字节就不允许再变。
 */
public class WireFixtureTest {
    private static final String PUBLISHER = "com.example.music";

    private static final String FULL_FRAME_HEX =
        "534c59040000000100000005000000010a000000080000000446554c4c00000002090000017000000006000000010600" +
        "00000b000000076669787475726500000002060000000a00000006e6a087e9a29800000003060000000a000000066172" +
        "7469737400000004000000000503000000000001e2400000000608000001170000000209000000c90000000600000001" +
        "060000000a00000006e4b880e8a18c00000002030000000000000000000000030300000000000003e800000004080000" +
        "006a00000002090000002e0000000300000001060000000700000003e4b8800000000203000000000000000000000003" +
        "0300000000000001f4090000002e0000000300000001060000000700000003e8a18c000000020300000000000001f400" +
        "0000030300000000000003e800000005060000000900000005666972737400000006060000000a00000006726f6d616a" +
        "6909000000400000000600000001060000000a00000006e4ba8ce8a18c000000020300000000000003e8000000030300" +
        "000000000007d000000004000000000500000000060000000003090000004b0000000500000001060000000b00000007" +
        "66697874757265000000020200000001000000030300000000000005dc00000004043fa00000000000050a0000000b00" +
        "000007504c4159494e47000000040000000005060000001500000011636f6d2e6578616d706c652e6d75736963";

    private static final String STOP_FRAME_HEX =
        "534c59040000000100000005000000010a000000080000000453544f500000000200000000030000000004090000002c" +
        "0000000200000001060000000b0000000766697874757265000000020a0000000b00000007434c454152454400000005" +
        "060000001500000011636f6d2e6578616d706c652e6d75736963";

    static Lyric fixtureLyric() {
        return new Lyric("fixture", 123_456L, Arrays.asList(
            new Line("一行", 0, 1000,
                Arrays.asList(new Word("一", 0, 500), new Word("行", 500, 1000)),
                "first", "romaji"),
            new Line("二行", 1000, 2000, null, null, null)),
            "标题", "artist", null);
    }

    static Frame fixtureFrame() {
        return Frame.full(fixtureLyric(),
            new State("fixture", 1, 1500L, 1.25f, PlaybackState.PLAYING), PUBLISHER);
    }

    static Frame fixtureStopFrame() {
        return Frame.stop(new Stop("fixture", StopReason.CLEARED), PUBLISHER);
    }

    @Test
    public void fullFrameBytesAreStable() {
        assertEquals(FULL_FRAME_HEX, hex(fixtureFrame().encode()));
    }

    @Test
    public void stopFrameBytesAreStable() {
        assertEquals(STOP_FRAME_HEX, hex(fixtureStopFrame().encode()));
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            out.append(Character.forDigit((b >> 4) & 0xF, 16));
            out.append(Character.forDigit(b & 0xF, 16));
        }
        return out.toString();
    }
}
