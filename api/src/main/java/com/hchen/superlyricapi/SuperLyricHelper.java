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
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
     * 发布歌词数据
     */
    public static void sendLyric(@NonNull SuperLyricData data) {
        try {
            ensureManager();
            ensurePublisherRegistered();

            mManager.sendLyric(data);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 发布全量歌曲歌词数据（全量包）
     * <p>
     * 建议仅在歌曲初始化、切歌或歌词首次解析完毕时调用
     */
    public static void sendFullLyric(@NonNull SuperLyricData fullData) {
        sendLyric(fullData);
    }

    /**
     * 发布当前播放点位或行索引更新（增量包）
     * <p>
     * 会自动清空全量歌词数组（以降低 Binder 传输负担），仅保留点位索引、进度及向下兼容的单行数据
     */
    public static void sendLyricProgress(@NonNull SuperLyricData progressData) {
        if (progressData.hasAllLyrics()) {
            SuperLyricData delta = new SuperLyricData()
                .setTitle(progressData.getTitle())
                .setArtist(progressData.getArtist())
                .setAlbum(progressData.getAlbum())
                .setLyricId(progressData.getLyricId())
                .setDuration(progressData.getDuration())
                .setPosition(progressData.getPosition())
                .setCurrentLyricIndex(progressData.getCurrentLyricIndex())
                .setLyric(progressData.getCurrentLyric())
                .setExtra(progressData.getExtra());
            if (progressData.hasTranslation()) {
                delta.setTranslation(progressData.getTranslation());
            }
            if (progressData.hasSecondary()) {
                delta.setSecondary(progressData.getSecondary());
            }
            sendLyric(delta);
        } else {
            sendLyric(progressData);
        }
    }

    /**
     * 发布状态暂停
     */
    public static void sendStop(@NonNull SuperLyricData data) {
        try {
            ensureManager();
            ensurePublisherRegistered();

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
    public static void registerPublisher() {
        try {
            ensureManager();
            mManager.registerPublisher();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 解除发行商注册
     * <p>
     * 您可以自行调用此方法解除注册，也可交由系统自行管理，当您的应用死亡时会自动解除注册
     */
    public static void unregisterPublisher() {
        try {
            ensureManager();
            mManager.unregisterPublisher();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 是否已注册为发行商
     */
    public static boolean isPublisherRegistered() {
        try {
            ensureManager();
            return mManager.isPublisherRegistered();
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
    public static void setSystemPlayStateListenerEnabled(boolean enabled) {
        try {
            ensureManager();
            mManager.setSystemPlayStateListenerEnabled(enabled);
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

    /**
     * 获取当前正在播放曲目的最新歌词数据（支持主动拉取全量歌词与播放状态）
     * <p>
     * 常用于：
     * <ul>
     *   <li>接收端（如桌面悬浮窗、状态栏）冷启动时主动获取当前曲目的全量数据；</li>
     *   <li>接收端收到增量包发现本地缓存缺失时，主动向系统服务拉取补全。</li>
     * </ul>
     *
     * @return 当前播放的 {@link SuperLyricData}；若无播放或服务不可用则返回 {@code null}
     */
    @Nullable
    public static SuperLyricData getLatestLyric() {
        try {
            ensureManager();
            return mManager.getLatestLyric();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        } catch (Throwable e) {
            Log.w(TAG, "Failed to get latest lyric from SuperLyricManager", e);
        }
        return null;
    }

    // ---------------------------- 内部 API ------------------------------------
    // ---------------------------- 请勿修改 -------------------------------------

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

    private static void ensurePublisherRegistered() {
        boolean isRegistered = false;
        try {
            ensureManager();
            isRegistered = mManager.isPublisherRegistered();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }

        if (!isRegistered) {
            throw new IllegalStateException("Not yet registered as a publisher.");
        }
    }
}
