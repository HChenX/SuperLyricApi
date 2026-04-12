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
    implementation 'com.github.HChenX:SuperLyricApi:3.4'
}
```

同步项目后即可使用该 API。

---

## 🛠 Xposed 模块使用指南 — 接收歌词

注册 `ISuperLyricReceiver` 用以接收歌词事件。

```java
public static void ModuleDemo() {
    ISuperLyricReceiver.Stub receiver;
    SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
        @Override
        public void onLyric(String publisher, SuperLyricData data) throws RemoteException {
            // 每次发布者发送新的歌词行时调用
            // 以下所有字段均为可选 — 使用前请检查
            String title = data.getTitle();
            String artist = data.getArtist();
            String album = data.getAlbum();

            if (data.hasLyric()) {
                SuperLyricLine lyric = data.getLyric();
                if (lyric != null) {
                    String text = lyric.getText();
                    long startTime = lyric.getStartTime(); // 毫秒
                    long endTime = lyric.getEndTime(); // 毫秒
                    long delay = lyric.getDelay(); // 持续时间 = endTime - startTime

                    // 逐字（卡拉OK）数据 — 可能为 null
                    SuperLyricWord[] words = lyric.getWords();
                    if (words != null) {
                        for (SuperLyricWord word : words) {
                            String wordText = word.getWord();
                            long wordStartTime = word.getStartTime();
                            long wordEndTime = word.getEndTime();
                        }
                    }
                }
            }

            if (data.hasSecondary()) {
                SuperLyricLine secondary = data.getSecondary();
            }
            if (data.hasTranslation()) {
                SuperLyricLine translation = data.getTranslation();
            }

            if (data.hasExtra()) {
                Bundle extra = data.getExtra();
            }

            data.getBase64Icon(); // Base64 Icon
        }

        @Override
        public void onStop(String publisher, SuperLyricData data) throws RemoteException {
            // 当发布者暂停播放或其进程终止时调用
        }
    });

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

```java
public static void MusicAppDemo() {
    SuperLyricHelper.sendLyric(
        new SuperLyricData()
            .setTitle("歌曲标题")
            .setArtist("艺术家名称")
            .setLyric(
                new SuperLyricLine(
                    "你好世界", // 歌词文本
                    new SuperLyricWord[]{ // 可选的逐字数据
                        new SuperLyricWord("你好", 0, 400),
                        new SuperLyricWord("世界", 400, 900),
                    },
                    0, // 行开始时间（毫秒）
                    900 // 行结束时间（毫秒）
                )
            )
            .setSecondary(new SuperLyricLine("副歌词行", 0, 900)) // 可选
            .setTranslation(new SuperLyricLine("翻译行", 0, 900)) // 可选
            .setExtra(extraBundle) // 可选
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

| 方法                               | 描述                         |
|----------------------------------|----------------------------|
| `setTitle(String)`               | 歌曲标题。                      |
| `setArtist(String)`              | 艺术家名称。                     |
| `setAlbum(String)`               | 专辑名称。                      |
| `setLyric(SuperLyricLine)`       | 主歌词行。                      |
| `setSecondary(SuperLyricLine)`   | 副歌词行（例如罗马音）。               |
| `setTranslation(SuperLyricLine)` | 主歌词的翻译。                    |
| `setExtra(Bundle)`               | 自定义键值对数据。会与任何已有的 extra 合并。 |

每个字段都有对应的 `hasXxx()` 检查方法（如 `hasLyric()`、`hasTitle()` 等）—— 访问可选字段前请始终进行检查。

### `SuperLyricLine`

表示单行歌词。

| 构造函数                                                | 描述         |
|-----------------------------------------------------|------------|
| `SuperLyricLine(text)`                              | 仅文本。       |
| `SuperLyricLine(text, startTime, endTime)`          | 文本及行时间。    |
| `SuperLyricLine(text, words[], startTime, endTime)` | 文本及逐字及行时间。 |

| 方法               | 返回值                            |
|------------------|--------------------------------|
| `getText()`      | 行文本（`@NonNull`）                |
| `getStartTime()` | 行开始时间（毫秒）                      |
| `getEndTime()`   | 行结束时间（毫秒）                      |
| `getWords()`     | `SuperLyricWord` 数组，可能为 `null` |

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