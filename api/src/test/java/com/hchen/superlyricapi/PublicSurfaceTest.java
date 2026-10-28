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
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验公开类型集合。新增公开类型时必须显式更新此处的清单。
 */
public class PublicSurfaceTest {
    private static final File JAVA_MAIN = new File("src/main/java/com/hchen/superlyricapi");
    private static final File KOTLIN_MAIN = new File("src/main/kotlin/com/hchen/superlyricapi");
    private static final String ROOT_PACKAGE = "com.hchen.superlyricapi";

    private static final Pattern JAVA_PUBLIC_TYPE = Pattern.compile(
        "(?m)^public\\s+(?:final\\s+|abstract\\s+)?(?:@interface|class|interface|enum)\\s+(\\w+)");

    private static final Pattern KOTLIN_PUBLIC_TYPE = Pattern.compile(
        "(?m)^(?:data\\s+|sealed\\s+|open\\s+|abstract\\s+|fun\\s+)*" +
            "(?:class|object|interface|enum\\s+class|annotation\\s+class)\\s+(\\w+)");

    private static final Set<String> EXPECTED = Set.of(
        // 客户端入口
        ROOT_PACKAGE + ".SuperLyric",
        ROOT_PACKAGE + ".Publisher",
        ROOT_PACKAGE + ".Receiver",
        ROOT_PACKAGE + ".LyricListener",
        ROOT_PACKAGE + ".LyricView",
        // 3.x 数据模型，服务端使用
        ROOT_PACKAGE + ".SuperLyricData",
        ROOT_PACKAGE + ".SuperLyricLine",
        ROOT_PACKAGE + ".SuperLyricWord",
        // 歌词模型
        ROOT_PACKAGE + ".model.Lyric",
        ROOT_PACKAGE + ".model.Line",
        ROOT_PACKAGE + ".model.Word",
        ROOT_PACKAGE + ".model.State",
        ROOT_PACKAGE + ".model.Stop",
        ROOT_PACKAGE + ".model.PlaybackState",
        ROOT_PACKAGE + ".model.StopReason",
        // 线上格式，服务端使用
        ROOT_PACKAGE + ".wire.Frame",
        ROOT_PACKAGE + ".wire.Wire",
        ROOT_PACKAGE + ".wire.WireFormatException",
        ROOT_PACKAGE + ".legacy.LegacyV37"
    );

    @Test
    public void onlyAllowlistedTypesArePublic() throws IOException {
        assumeTrue(KOTLIN_MAIN.isDirectory());

        Set<String> actual = new TreeSet<>();
        for (File root : new File[]{JAVA_MAIN, KOTLIN_MAIN}) {
            for (File file : sourceFiles(root)) {
                boolean kotlin = file.getName().endsWith(".kt");
                Matcher matcher = (kotlin ? KOTLIN_PUBLIC_TYPE : JAVA_PUBLIC_TYPE).matcher(read(file));
                while (matcher.find()) {
                    actual.add(packageOf(root, file) + "." + matcher.group(1));
                }
            }
        }

        assertEquals(new TreeSet<>(EXPECTED), actual);
    }

    private static String packageOf(File root, File file) {
        Path parent = root.toPath().relativize(file.toPath()).getParent();
        if (parent == null) {
            return ROOT_PACKAGE;
        }
        return ROOT_PACKAGE + "." + parent.toString().replace('\\', '.').replace('/', '.');
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private static List<File> sourceFiles(File dir) {
        List<File> out = new ArrayList<>();
        collect(dir, out);
        return out;
    }

    private static void collect(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, out);
            } else if (child.getName().endsWith(".java") || child.getName().endsWith(".kt")) {
                out.add(child);
            }
        }
    }
}
