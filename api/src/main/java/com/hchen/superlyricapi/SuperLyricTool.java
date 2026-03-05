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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

/**
 * API 工具
 *
 * @author 焕晨HChen
 */
@SuppressLint("SoonBlockedPrivateApi")
public class SuperLyricTool {
    private SuperLyricTool() {
    }

    /**
     * API 是否成功启用，供音乐软件检查使用
     */
    public static boolean isEnabled = false;
    /**
     * 当前 API 版本
     */
    public static int apiVersion = BuildConfig.API_VERSION;

    /**
     * Base64 转 Bitmap
     */
    @Nullable
    public static Bitmap base64ToBitmap(@NonNull String base64) {
        try {
            if (base64.isEmpty()) return null;
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Drawable 转 Base64
     *
     * @noinspection IfCanBeSwitch
     */
    @NonNull
    public static String drawableToBase64(@NonNull Drawable drawable) {
        if (drawable instanceof AdaptiveIconDrawable adaptiveIconDrawable) {
            return adaptiveIconDrawableBase64(adaptiveIconDrawable);
        }

        if (drawable instanceof BitmapDrawable bitmapDrawable) {
            return bitmapToBase64(bitmapDrawable.getBitmap());
        } else if (drawable instanceof VectorDrawable vectorDrawable) {
            return bitmapToBase64(makeDrawableToBitmap(vectorDrawable));
        }
        return "";
    }

    /**
     * Bitmap 转 Base64
     */
    @NonNull
    public static String bitmapToBase64(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @NonNull
    private static String adaptiveIconDrawableBase64(@NonNull AdaptiveIconDrawable drawable) {
        Drawable background = drawable.getBackground();
        Drawable foreground = drawable.getForeground();
        if (background != null && foreground != null) {
            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{background, foreground});
            Bitmap createBitmap = Bitmap.createBitmap(layerDrawable.getIntrinsicWidth(), layerDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            layerDrawable.draw(canvas);
            return bitmapToBase64(createBitmap);
        }
        return "";
    }

    @NonNull
    private static Bitmap makeDrawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static Field mediaMetadataBundle;
    private static final String[] keys = new String[]{
        MediaMetadata.METADATA_KEY_ART,
        MediaMetadata.METADATA_KEY_ALBUM_ART,
        MediaMetadata.METADATA_KEY_DISPLAY_ICON
    };

    static {
        try {
            // noinspection JavaReflectionMemberAccess
            mediaMetadataBundle = MediaMetadata.class.getDeclaredField("mBundle");
            mediaMetadataBundle.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    @Nullable
    static MediaMetadata removeMediaMetadataBitmap(@Nullable MediaMetadata mediaMetadata) {
        if (mediaMetadata == null) return null;
        if (mediaMetadataBundle == null) return null;

        try {
            MediaMetadata metadata = new MediaMetadata.Builder(mediaMetadata).build();
            Bundle bundle = (Bundle) mediaMetadataBundle.get(metadata);
            if (bundle == null) return null;
            for (String key : keys) {
                bundle.remove(key);
            }
            return metadata;
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    /**
     * 注册 SuperLyric 回调
     *
     * @param context    上下文信息
     * @param superLyric 回调
     */
    public static void registerSuperLyric(@NonNull Context context, @NonNull ISuperLyric.Stub superLyric) {
        Intent intent = new Intent("super_lyric");
        Bundle bundle = new Bundle();
        bundle.putBinder("super_lyric_register", superLyric);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }

    /**
     * 注销 SuperLyric 回调
     *
     * @param context    上下文信息
     * @param superLyric 回调
     */
    public static void unregisterSuperLyric(@NonNull Context context, @NonNull ISuperLyric.Stub superLyric) {
        Intent intent = new Intent("super_lyric");
        Bundle bundle = new Bundle();
        bundle.putBinder("super_lyric_unregister", superLyric);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }
}
