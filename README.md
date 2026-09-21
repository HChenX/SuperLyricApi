<div align="center">
<h1>SuperLyricApi</h1>

![stars](https://img.shields.io/github/stars/HChenX/SuperLyricApi?style=flat)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/SuperLyricApi)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/SuperLyricApi)](https://github.com/HChenX/SuperLyricApi/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/SuperLyricApi)](https://github.com/HChenX/SuperLyricApi/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/SuperLyricApi?style=flat)
![language](https://img.shields.io/badge/language-java-purple)
![language](https://img.shields.io/badge/language-aidl-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
<p>基于 Binder 的轻量级 Android API，用于广播和接收实时歌词。</p>
</div>

---

## ✨ 概述

SuperLyricApi 提供了简洁、轻量的接口，使得：

- **音乐应用** 可以将实时歌词数据（文本、逐字时间、翻译、播放状态等）发布到系统级服务。
- **Xposed 模块** 可以从系统服务中接收这些歌词数据。

所有通信均通过 AIDL 基于 Binder 完成，快速且进程安全，并与基于 Xposed 的模块架构完美兼容。

---

## ✨ 添加依赖

将 JitPack 仓库和库依赖添加到您的项目中：

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle (app module)
dependencies {
    implementation 'com.github.HChenX:SuperLyricApi:3.5'
}
```

同步项目后即可使用该 API。

---

## 🛠 Xposed 模块使用指南 — 接收歌词

注册 `ISuperLyricReceiver` 用以接收歌词事件。推荐使用 `SuperLyricCache.resolve(data)`
一键自动处理全量与增量包合成及断档自愈。

```java
public static void ModuleDemo() {
    ISuperLyricReceiver.Stub receiver;
    SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
        @Override
        public void onLyric(String publisher, SuperLyricData rawData) throws RemoteException {
            // 推荐：使用 SuperLyricCache.resolve 自动合成增量包与全量歌词，并支持被杀重启自愈
            SuperLyricData data = SuperLyricCache.resolve(rawData);
            if (data == null) return;

            String title = data.getTitle();
            String artist = data.getArtist();
            String album = data.getAlbum();
            long duration = data.getDuration(); // 歌曲总时长（毫秒）
            long position = data.getPosition(); // 当前播放进度（毫秒）

            // 1. 获取当前播放的单行歌词（向下兼容单行场景与全曲切片）
            SuperLyricLine currentLine = data.getCurrentLyric();
            if (currentLine != null) {
                String text = currentLine.getText();
                long startTime = currentLine.getStartTime();
                long endTime = currentLine.getEndTime();
                String translation = currentLine.getTranslation(); // 当前行翻译
                String secondary = currentLine.getSecondary(); // 当前行罗马音/副歌词

                // 逐字（卡拉OK）数据
                SuperLyricWord[] words = currentLine.getWords();
                if (words != null) {
                    for (SuperLyricWord word : words) {
                        String wordText = word.getWord();
                        long wordStart = word.getStartTime();
                        long wordEnd = word.getEndTime();
                    }
                }
            }

            // 2. 获取整首歌曲的所有歌词数据（支持列表渲染、多行滚动展示）
            if (data.hasAllLyrics()) {
                SuperLyricLine[] allLines = data.getAllLyrics(); // 整首歌所有行
                int currentIndex = data.getCurrentLyricIndex(); // 当前行在整首歌曲中的下标

                // 3. 点位切片：瞬间获取已播放完毕和尚未播放的歌词行
                List<SuperLyricLine> played = data.getPlayedLyrics(); // 已播放历史行
                List<SuperLyricLine> upcoming = data.getUpcomingLyrics(); // 未播放未来行
            }

            if (data.hasExtra()) {
                Bundle extra = data.getExtra();
            }
        }

        @Override
        public void onStop(String publisher, SuperLyricData data) throws RemoteException {
            // 当发布者暂停播放或其进程终止时调用
        }
    });

    // 主动拉取：若模块冷启动或悬浮窗开启，可直接主动获取当前播放歌曲的全量数据
    SuperLyricData currentPlaying = SuperLyricHelper.getLatestLyric();

    // 查询注册状态或完成后取消注册
    boolean registered = SuperLyricHelper.isReceiverRegistered(receiver);
    SuperLyricHelper.unregisterReceiver(receiver);
}
```

---

## 🔧 音乐应用使用指南 — 发布歌词

音乐应用在发送任何数据前必须注册为**发布者**。

### 1. 检查服务可用性

```java
boolean available = SuperLyricHelper.isAvailable();
```

### 2. 注册为发布者

在应用启动时调用一次（例如在 `onCreate` 中）。如果跳过此步骤，发布操作将抛出
`IllegalStateException`。

```java
public static void MusicAppDemo() {
    SuperLyricHelper.registerPublisher();
}
```

### 3. 发送歌词数据

推荐使用 **全量包 + 增量进度包** 的传输优化策略：

#### 3.1 切歌或歌词初次载入（发送全量包）

```java
public static void MusicAppDemo() {
    // 构造整首歌曲的所有行
    SuperLyricLine[] allLines = new SuperLyricLine[]{
        new SuperLyricLine("你好世界", new SuperLyricWord[]{
            new SuperLyricWord("你好", 0, 400),
            new SuperLyricWord("世界", 400, 900),
        }, "Hello World", "Ni Hao Shi Jie", 0, 900),
        new SuperLyricLine("下一句歌词", null, "Next Line", null, 900, 2000),
    };

    SuperLyricHelper.sendFullLyric(
        new SuperLyricData()
            .setTitle("歌曲标题")
            .setArtist("艺术家名称")
            .setAlbum("专辑名称")
            .setLyricId("song_unique_id") // 推荐设置唯一标识，便于接收端缓存与自愈
            .setDuration(180000L) // 歌曲总时长（毫秒）
            .setAllLyrics(allLines) // 整首歌曲所有行
            .setCurrentLyricIndex(0) // 当前播放行下标
            .setLyric(allLines[0]) // 兼容旧版本接收端
    );
}
```

#### 3.2 播放中切行或进度推进（发送极轻量增量包）

播放中切行时无需重复传输庞大的 `allLyrics` 数组，仅需发送轻量点位更新：

```java
public static void onLineChanged(int newIndex, long currentPositionMs, SuperLyricLine currentLine) {
    SuperLyricHelper.sendLyricProgress(
        new SuperLyricData()
            .setLyricId("song_unique_id")
            .setCurrentLyricIndex(newIndex) // 当前行下标
            .setPosition(currentPositionMs) // 当前毫秒进度
            .setLyric(currentLine) // 兼容旧版接收端
    );
}
```

### 4. 发送停止事件

当播放暂停或停止时调用此方法。

```java
public static void MusicAppDemo() {
    SuperLyricHelper.sendStop(
        new SuperLyricData()
    );
}
```

### 5. 取消注册（可选）

系统会在应用进程终止时自动清理发布者注册。您也可以主动取消注册：

```java
public static void MusicAppDemo() {
    SuperLyricHelper.unregisterPublisher();
}
```

### 6. 系统播放状态监听器

默认情况下，SuperLyric 会监听系统的 `MediaSession` 事件，自动代为处理播放状态变化。如果您希望手动管理这些事件，可以禁用此功能：

```java
public static void MusicAppDemo() {
    SuperLyricHelper.setSystemPlayStateListenerEnabled(false);
    // 然后根据需要自行调用 sendStop()、setPlaybackState()。
}
```

---

## 📦 数据模型参考

### `SuperLyricData`

| 方法                               | 描述                                   |
|----------------------------------|--------------------------------------|
| `setTitle(String)`               | 歌曲标题。                                |
| `setArtist(String)`              | 艺术家名称。                               |
| `setAlbum(String)`               | 专辑名称。                                |
| `setLyric(SuperLyricLine)`       | 主歌词行（单行模式或当前行）。                      |
| `setSecondary(SuperLyricLine)`   | 副歌词行（例如罗马音）。                         |
| `setTranslation(SuperLyricLine)` | 主歌词的翻译。                              |
| `setAllLyrics(SuperLyricLine[])` | 设置整首歌曲的所有歌词行。                        |
| `getAllLyrics()`                 | 获取整首歌曲的所有歌词行。                        |
| `getAllLyricsList()`             | 以 `List<SuperLyricLine>` 形式获取整首歌所有行。 |
| `getAllLyricsCount()`            | 获取整首歌总行数。                            |
| `setCurrentLyricIndex(int)`      | 设置当前正在播放的行下标（从 0 开始）。                |
| `getCurrentLyricIndex()`         | 获取当前播放行下标。                           |
| `setDuration(long)`              | 设置歌曲总时长（毫秒）。                         |
| `getDuration()`                  | 获取歌曲总时长（毫秒）。                         |
| `setPosition(long)`              | 设置当前播放进度毫秒时间戳。                       |
| `getPosition()`                  | 获取当前播放进度毫秒时间戳。                       |
| `setLyricId(String)`             | 设置歌词唯一指纹标识（用于缓存自愈与增量包合成）。            |
| `getLyricId()`                   | 获取歌词唯一标识。                            |
| `getCurrentLyric()`              | 智能获取当前正在播放的歌词行（优先取单行，若无则从全量中提取）。     |
| `getPlayedLyrics()`              | 提取已播放完毕的历史歌词行切片列表。                   |
| `getUpcomingLyrics()`            | 提取未播放的未来歌词行切片列表。                     |
| `getLyricAt(int index)`          | 安全获取指定下标的歌词行。                        |
| `setExtra(Bundle)`               | 自定义键值对数据。会与任何已有的 extra 合并。           |

每个字段都有对应的 `hasXxx()` 检查方法（如 `hasAllLyrics()`、`hasDuration()` 等）—— 访问可选字段前请始终进行检查。

### `SuperLyricLine`

表示单行歌词。

| 构造函数                                                                        | 描述            |
|-----------------------------------------------------------------------------|---------------|
| `SuperLyricLine(text)`                                                      | 仅文本。          |
| `SuperLyricLine(text, startTime, endTime)`                                  | 文本及行时间。       |
| `SuperLyricLine(text, words[], startTime, endTime)`                         | 文本、逐字及行时间。    |
| `SuperLyricLine(text, words[], translation, startTime, endTime)`            | 文本、逐字、翻译及行时间。 |
| `SuperLyricLine(text, words[], translation, secondary, startTime, endTime)` | 包含全部信息的完整行。   |

| 方法                     | 返回值                           | 描述           |
|------------------------|-------------------------------|--------------|
| `getText()`            | `String`（`@NonNull`）          | 行文本          |
| `getStartTime()`       | `long`                        | 行开始时间（毫秒）    |
| `getEndTime()`         | `long`                        | 行结束时间（毫秒）    |
| `getWords()`           | `SuperLyricWord[]`，可能为 `null` | 逐字（卡拉OK）数组   |
| `getTranslation()`     | `String`，可能为 `null`           | 当前行翻译文本      |
| `getSecondary()`       | `String`，可能为 `null`           | 当前行副歌词/罗马音   |
| `getTranslationLine()` | `SuperLyricLine`，可能为 `null`   | 包装为单行对象的翻译歌词 |
| `getSecondaryLine()`   | `SuperLyricLine`，可能为 `null`   | 包装为单行对象的副歌词  |

### `SuperLyricCache`

提供全量歌词缓存、增量包智能合成及被杀重启自愈功能。

| 方法                                      | 描述                                                |
|-----------------------------------------|---------------------------------------------------|
| `SuperLyricCache.resolve(incomingData)` | 传入收到的数据包，自动完成增量合成或自愈拉取，返回完备的 `SuperLyricData` 视图。 |
| `SuperLyricCache.put(data)`             | 将全量数据手动放入缓存。                                      |
| `SuperLyricCache.get(lyricId)`          | 获取缓存的全量数据。                                        |
| `SuperLyricCache.clear()`               | 清空本地缓存。                                           |

### `SuperLyricWord`

表示歌词行内的单个单词，用于卡拉 OK 风格的逐字高亮。

| 构造函数                                       | 描述  |
|--------------------------------------------|-----|
| `SuperLyricWord(word, startTime, endTime)` | 逐字。 |

| 方法               | 返回值              |
|------------------|------------------|
| `getWord()`      | 单词文本（`@NonNull`） |
| `getStartTime()` | 单词开始时间（毫秒）       |
| `getEndTime()`   | 单词结束时间（毫秒）       |

---

## 🌟 ProGuard / R8

强烈建议保持所有 API 类不被混淆：

```proguard
-keep class com.hchen.superlyricapi.* {*;}
```

---

## 📢 歌词获取器

- [SuperLyric](https://github.com/HChenX/SuperLyric)

---

## 🎉 结语

💖 **感谢您的支持，Enjoy your day!** 🚀