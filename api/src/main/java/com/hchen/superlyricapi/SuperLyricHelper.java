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

    /**
     * {@link #getServerApiVersion()} 在服务端过旧、不认识该查询时的返回值
     */
    public static final int API_VERSION_UNKNOWN = -1;

    private SuperLyricHelper() {
    }

    /**
     * 获取 SuperLyric 服务是否可用
     */
    public static boolean isAvailable() {
        try {
            ensureManager();
            return mManager != null;
        } catch (Throwable ignore) {
            return false;
        }
    }

    /**
     * 获取**本库编译时**的 API 版本号
     */
    public static int getApiVersion() {
        return BuildConfig.API_VERSION;
    }

    /**
     * 查询**服务端（SuperLyric 模块）实际实现**的 API 版本号
     *
     * @return 服务端实现的版本号；服务端过旧（无此方法）时返回 {@link #API_VERSION_UNKNOWN}
     */
    public static int getServerApiVersion() {
        try {
            ensureManager();
            return mManager.getServerApiVersion();
        } catch (Throwable t) {
            // 旧版服务端没有这个事务号，调用会抛异常，此处无从得知其版本
            return API_VERSION_UNKNOWN;
        }
    }

    // -------------------------- 为第三方音乐软件提供的 API -----------------------------------
    // ------------------------------- 模块内请勿使用 -----------------------------------------

    /**
     * 发布整首歌词数据 —— **唯一的发布入口**
     * <p>
     * 在歌曲初始化、切歌或歌词首次解析完毕时调用一次即可，数据应当携带完整的
     * `allLyrics`。其中两个字段是**必填**的：
     * <ul>
     *   <li>{@code lyricId} —— 会话标识，也是接收端缓存自愈的键</li>
     *   <li>{@code duration} —— 服务端时钟插值的锚；缺失时服务端会尝试从系统元数据补齐</li>
     * </ul>
     * <p>
     * <b>播放进度与行切换不需要由应用发布。</b> SuperLyric 的服务端时钟会自行推算播放位置、
     * 判断当前行，并在跨行时构造轻量增量包广播给接收端。应用侧重复推送只会造成两份时钟互相干扰
     */
    public static void sendLyric(@NonNull SuperLyricData data) {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            ensurePublisherRegistered(manager);
            warnIfIncomplete(data);
            manager.sendLyric(data);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 关键字段缺失时逐条告警
     */
    private static void warnIfIncomplete(@NonNull SuperLyricData data) {
        if (!data.hasAllLyrics()) {
            Log.w(TAG, "sendLyric: allLyrics is missing or empty, receivers cannot build a full view");
        }
        if (!data.hasLyricId()) {
            Log.w(TAG, "sendLyric: lyricId is missing, receiver caches fall back to title/artist matching");
        }
        if (!data.hasDuration()) {
            Log.w(TAG, "sendLyric: duration is missing, progress cannot be interpolated until the system metadata supplies one");
        }
        if (!data.hasTitle()) {
            Log.w(TAG, "sendLyric: title is missing, the track cannot be identified or matched");
        }
    }

    /**
     * 结束当前发布会话：清除本应用已发布的歌词，并令接收端收到 {@code onStop}
     * <p>
     * <b>集成方通常不需要主动调用。</b> SuperLyric 监听系统 `MediaSession`，播放器进入
     * `STOPPED` / `NONE` / `ERROR` 时会自动完成上述动作
     * <p>
     * 它仍然有实际用处的场景是「**切歌时立刻清场**」——需要马上抹掉上一首的歌词、
     * 而不是等系统状态回调时使用
     */
    public static void sendStop(@NonNull SuperLyricData data) {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            ensurePublisherRegistered(manager);
            manager.sendStop(data);
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
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            manager.registerPublisher();
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
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            manager.unregisterPublisher();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 是否已注册为发行商
     */
    public static boolean isPublisherRegistered() {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return false;

        try {
            return manager.isPublisherRegistered();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
        return false;
    }

    // setSystemPlayStateListenerEnabled 的 Java helper 已移除：SuperLyric 恒常监听系统
    // MediaSession，该开关不再对集成方开放。

    // -------------------------- 为模块提供注册接收器的能力 -----------------------------------
    // ---------------------------- 第三方音乐软件请勿使用 ------------------------------------

    /**
     * 注册 ISuperLyricReceiver 接收器
     */
    public static void registerReceiver(@NonNull ISuperLyricReceiver receiver) {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            manager.registerReceiver(receiver);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 注销 ISuperLyricReceiver 接收器
     */
    public static void unregisterReceiver(@NonNull ISuperLyricReceiver receiver) {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return;

        try {
            manager.unregisterReceiver(receiver);
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }
    }

    /**
     * 接收器是否已被注册
     */
    public static boolean isReceiverRegistered(@NonNull ISuperLyricReceiver receiver) {
        ISuperLyricManager manager = managerOrNull();
        if (manager == null) return false;

        try {
            return manager.isReceiverRegistered(receiver);
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

    @Nullable
    private static ISuperLyricManager managerOrNull() {
        try {
            ensureManager();
            return mManager;
        } catch (Throwable t) {
            Log.w(TAG, "SuperLyric service unavailable: " + t.getMessage());
            return null;
        }
    }

    private static void ensurePublisherRegistered(@NonNull ISuperLyricManager manager) {
        boolean isRegistered = false;
        try {
            isRegistered = manager.isPublisherRegistered();
        } catch (RemoteException e) {
            Log.e(TAG, "SuperLyricManager RemoteException!!", e);
        }

        if (!isRegistered) {
            throw new IllegalStateException("Not yet registered as a publisher.");
        }
    }
}
