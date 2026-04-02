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
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

/**
 * API 工具
 *
 * @author 焕晨HChen
 */
@SuppressLint({"SoonBlockedPrivateApi", "DiscouragedPrivateApi", "PrivateApi"})
public class SuperLyricHelper {
    private volatile static ISuperLyricPublisher mPublisher;

    private SuperLyricHelper() {
    }

    /**
     * 获取 SuperLyric 服务是否可用
     */
    public static boolean isAvailable() {
        try {
            ensurePublisher();
            return mPublisher != null;
        } catch (IllegalStateException ignore) {
            return false;
        }
    }

    /**
     * 获取当前 API 版本
     */
    public static int getApiVersion() {
        return BuildConfig.API_VERSION;
    }

    /**
     * 歌曲数据更改
     * <p>
     * 请务必传递歌词和包名信息，当然您也可以传递其他参数
     */
    public static void sendLyric(@NonNull SuperLyricData data) {
        try {
            ensurePublisher();
            mPublisher.sendLyric(data);
        } catch (RemoteException e) {
            // 永远不会触发异常，因为来自系统框架的 binder 不应会死亡
        }
    }

    /**
     * 歌曲暂停
     * <p>
     * 请务必传递包名信息，当然您也可以传递其他参数
     */
    public static void sendStop(@NonNull SuperLyricData data) {
        try {
            ensurePublisher();
            mPublisher.sendStop(data);
        } catch (RemoteException e) {
            // 永远不会触发异常，因为来自系统框架的 binder 不应会死亡
        }
    }

    // -------------------------- 为第三方音乐软件提供的 API -----------------------------------
    // ------------------------------- 模块内请勿使用 -----------------------------------------

    /**
     * 是否启用系统层面的播放状态监听器
     * <p>
     * 如果禁用，则请自行发布 sendStop，MediaMetadata，PlaybackState 等数据
     */
    public static void setSystemPlayStateListenerEnabled(@NonNull Context context, boolean enabled) {
        try {
            ensurePublisher();
            mPublisher.setSystemPlayStateListenerEnabled(context.getPackageName(), enabled);
        } catch (RemoteException ignore) {
            // 永远不会触发异常，因为来自系统框架的 binder 不应会死亡
        }
    }

    // -------------------------- 为模块提供注册接收器的能力 -----------------------------------
    // ---------------------------- 第三方音乐软件请勿使用 ------------------------------------

    /**
     * 注册 ISuperLyricReceiver 回调
     *
     * @param receiver 回调
     */
    public static void registerReceiver(@NonNull ISuperLyricReceiver.Stub receiver) {
        try {
            ensurePublisher();
            mPublisher.registerReceiver(receiver);
        } catch (RemoteException ignore) {
            // 永远不会触发异常，因为来自系统框架的 binder 不应会死亡
        }
    }

    /**
     * 注销 ISuperLyricReceiver 回调
     *
     * @param receiver 回调
     */
    public static void unregisterReceiver(@NonNull ISuperLyricReceiver.Stub receiver) {
        try {
            ensurePublisher();
            mPublisher.unregisterReceiver(receiver);
        } catch (RemoteException ignore) {
            // 永远不会触发异常，因为来自系统框架的 binder 不应会死亡
        }
    }

    // ---------------------------- 内部 API ------------------------------------
    // ---------------------------- 请勿修改 -------------------------------------

    private static Field mMediaMetadataBundle;
    private static final String[] KEYS = new String[]{
        MediaMetadata.METADATA_KEY_ART,
        MediaMetadata.METADATA_KEY_ALBUM_ART,
        MediaMetadata.METADATA_KEY_DISPLAY_ICON
    };

    static {
        try {
            // noinspection JavaReflectionMemberAccess
            mMediaMetadataBundle = MediaMetadata.class.getDeclaredField("mBundle");
            mMediaMetadataBundle.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    @Nullable
    static MediaMetadata removeMediaMetadataBitmap(@Nullable MediaMetadata mediaMetadata) {
        if (mMediaMetadataBundle == null || mediaMetadata == null) {
            return null;
        }

        try {
            MediaMetadata metadata = new MediaMetadata.Builder(mediaMetadata).build();
            Bundle bundle = (Bundle) mMediaMetadataBundle.get(metadata);
            if (bundle == null) return null;
            for (String key : KEYS) {
                bundle.remove(key);
            }
            return metadata;
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    private synchronized static void ensurePublisher() {
        if (mPublisher != null) {
            return;
        }

        IBinder iBinder = ServiceManager.getService("super_lyric");
        mPublisher = ISuperLyricPublisher.Stub.asInterface(iBinder);
        if (mPublisher == null) {
            throw new IllegalStateException("Publisher not registered.");
        }
    }
}
