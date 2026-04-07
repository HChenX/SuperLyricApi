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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;

/**
 * API 助手
 *
 * @author 焕晨HChen
 */
@SuppressLint({"SoonBlockedPrivateApi", "DiscouragedPrivateApi", "PrivateApi"})
public class SuperLyricHelper {
    private static final String TAG = "SuperLyricHelper";
    private volatile static ISuperLyricManager mManager;

    private SuperLyricHelper() {
    }

    /**
     * 获取 SuperLyric 服务是否可用
     */
    public static boolean isAvailable() {
        try {
            ensureManager();
            return mManager != null;
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

    // -------------------------- 为第三方音乐软件提供的 API -----------------------------------
    // ------------------------------- 模块内请勿使用 -----------------------------------------

    /**
     * 歌曲数据更改
     * <p>
     * 请务必传递歌词和包名信息，当然您也可以传递其他参数
     */
    public static void sendLyric(@NonNull SuperLyricData data) {
        try {
            ensureManager();
            ensurePublisherRegistered(data.getPackageName());

            mManager.sendLyric(data);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 歌曲暂停
     * <p>
     * 请务必传递包名信息，当然您也可以传递其他参数
     */
    public static void sendStop(@NonNull SuperLyricData data) {
        try {
            ensureManager();
            ensurePublisherRegistered(data.getPackageName());

            mManager.sendStop(data);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 注册为发行商
     * <p>
     * 发布歌词之前请务必先注册为发行商，否则将会触发异常
     */
    public static void registerPublisher(@NonNull Context context) {
        try {
            ensureManager();
            mManager.registerPublisher(context.getPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 解除发行商注册
     * <p>
     * 您可以自行调用此方法解除注册，也可交由系统自行管理，当您的应用死亡时会自动解除注册
     */
    public static void unregisterPublisher(@NonNull Context context) {
        try {
            ensureManager();
            mManager.unregisterPublisher(context.getPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 是否已注册为发行商
     */
    public static boolean isPublisherRegistered(@NonNull Context context) {
        try {
            ensureManager();
            return mManager.isPublisherRegistered(context.getPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
        return false;
    }

    /**
     * 是否启用系统层面的播放状态监听器
     * <p>
     * 如果禁用，则请自行发布 sendStop，MediaMetadata，PlaybackState 等数据
     */
    public static void setSystemPlayStateListenerEnabled(@NonNull Context context, boolean enabled) {
        try {
            ensureManager();
            mManager.setSystemPlayStateListenerEnabled(context.getPackageName(), enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    // -------------------------- 为模块提供注册接收器的能力 -----------------------------------
    // ---------------------------- 第三方音乐软件请勿使用 ------------------------------------

    /**
     * 注册 ISuperLyricReceiver 接收器
     */
    public static void registerReceiver(@NonNull ISuperLyricReceiver receiver) {
        try {
            ensureManager();
            mManager.registerReceiver(receiver);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 注销 ISuperLyricReceiver 接收器
     */
    public static void unregisterReceiver(@NonNull ISuperLyricReceiver receiver) {
        try {
            ensureManager();
            mManager.unregisterReceiver(receiver);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 接收器是否已被注册
     */
    public static boolean isReceiverRegistered(@NonNull ISuperLyricReceiver receiver) {
        try {
            ensureManager();
            return mManager.isReceiverRegistered(receiver);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
        return false;
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
            return mediaMetadata;
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
        return mediaMetadata;
    }

    private static void ensureManager() {
        if (mManager != null) {
            return;
        }

        synchronized (SuperLyricHelper.class) {
            if (mManager != null) {
                return;
            }

            IBinder iBinder = ServiceManager.getService("super_lyric");
            mManager = ISuperLyricManager.Stub.asInterface(iBinder);
            if (mManager == null) {
                throw new IllegalStateException("SuperLyricManager not attached.");
            }

            try {
                iBinder.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        mManager = null;
                    }
                }, 0);
            } catch (RemoteException ignore) {
                mManager = null;
            }
        }
    }

    private static void ensurePublisherRegistered(@NonNull String packageName) {
        boolean isRegistered = false;
        try {
            ensureManager();
            isRegistered = mManager.isPublisherRegistered(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }

        if (!isRegistered) {
            throw new IllegalStateException("Not yet registered as a publisher.");
        }
    }
}
