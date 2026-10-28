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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查模块内部的依赖约束。
 */
public class ArchTest {
    private static final File JAVA_MAIN = new File("src/main/java/com/hchen/superlyricapi");
    private static final File KOTLIN_MAIN = new File("src/main/kotlin/com/hchen/superlyricapi");

    @Test
    public void modelStaysFreeOfLegacyAndParcel() throws IOException {
        assumeTrue(KOTLIN_MAIN.isDirectory());
        File model = new File(KOTLIN_MAIN, "model");
        assertNoText(model, "com.hchen.superlyricapi.legacy");
        assertNoText(model, "android.os.Parcel");
        assertNoText(model, "android.os.Bundle");
    }

    @Test
    public void wireStaysFreeOfLegacy() throws IOException {
        assumeTrue(KOTLIN_MAIN.isDirectory());
        File wire = new File(KOTLIN_MAIN, "wire");
        assertNoText(wire, "com.hchen.superlyricapi.legacy");
        assertNoText(wire, "com.hchen.superlyricapi.SuperLyricData");
    }

    @Test
    public void v4TransportNeverTouchesTheLegacyInterface() throws IOException {
        File file = new File(KOTLIN_MAIN, "internal/V4Transport.kt");
        assumeTrue(file.isFile());
        String source = read(file);
        assertFalse(source.contains("ISuperLyricManager"));
        assertFalse(source.contains("SuperLyricData"));
    }

    @Test
    public void legacyInterfaceIsOnlyReferencedByItsTransport() throws IOException {
        assumeTrue(KOTLIN_MAIN.isDirectory());
        List<File> offenders = new ArrayList<>();
        for (File root : new File[]{JAVA_MAIN, KOTLIN_MAIN}) {
            for (File file : sourceFiles(root)) {
                if (!file.getName().equals("LegacyTransport.kt") && read(file).contains("ISuperLyricManager")) {
                    offenders.add(file);
                }
            }
        }
        assertTrue("only LegacyTransport may reference the 3.x interface: " + offenders, offenders.isEmpty());
    }

    private static void assertNoText(File dir, String needle) throws IOException {
        for (File file : sourceFiles(dir)) {
            assertFalse(file.getName() + " must not reference " + needle, read(file).contains(needle));
        }
    }

    private static String read(File file) throws IOException {
        // Files.readString 需要 API 33，单元测试中改用 readAllBytes
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
