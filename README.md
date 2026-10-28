<div align="center">
<h1>SuperLyricApi</h1>

![stars](https://img.shields.io/github/stars/HChenX/SuperLyricApi?style=flat)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/SuperLyricApi)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/SuperLyricApi)](https://github.com/HChenX/SuperLyricApi/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/SuperLyricApi)](https://github.com/HChenX/SuperLyricApi/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/SuperLyricApi?style=flat)
![language](https://img.shields.io/badge/language-kotlin-purple)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
<p>基于 Binder 的 Android 歌词传输接口。</p>
</div>

---

## 概述

SuperLyricApi 在音乐应用与系统服务之间传递歌词。

音乐应用发布一次完整歌词，包含行文本、逐字时间、翻译与副歌词。系统服务负责对齐播放进度，并在歌词行或播放状态变化时向接收端推送。

播放进度、暂停状态与倍速由系统从 MediaSession 读取，发布方不参与。

通信基于 Binder，数据结构为自描述的字节流，新增字段不影响已有的接收端。

API 由 Kotlin 实现，Java 调用方式不变；内部实现使用 internal 可见性，不对外暴露。

## 添加依赖

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
    implementation 'com.github.HChenX:SuperLyricApi:4.0'
}
```

## 发布端

一首歌调用一次 `publish()`，切歌时再次调用。

Kotlin：

```kotlin
val lines = listOf(
    Line(
        "你好世界", 0, 900,
        listOf(Word("你好", 0, 400), Word("世界", 400, 900)),
        "Hello World",
        "Ni Hao Shi Jie",
    ),
    Line("下一句歌词", 900, 2000),
)

val doc = Lyric("song_1001", 180_000L, lines, "歌曲标题", "艺术家名称", "专辑名称")

when (SuperLyric.publisher().publish(doc)) {
    Publisher.PublishResult.OK -> Unit
    Publisher.PublishResult.SERVICE_UNAVAILABLE -> Unit
    Publisher.PublishResult.PAYLOAD_TOO_LARGE -> Unit
    Publisher.PublishResult.REJECTED -> Unit
}
```

Java：

```java
List<Line> lines = new ArrayList<>();
lines.add(new Line("你好世界", 0, 900,
        Arrays.asList(new Word("你好", 0, 400), new Word("世界", 400, 900)),
        "Hello World", "Ni Hao Shi Jie"));
lines.add(new Line("下一句歌词", 900, 2000, null, "Next Line", null));

Lyric doc = new Lyric("song_1001", 180_000L, lines, "歌曲标题", "艺术家名称", "专辑名称");

Publisher.PublishResult result = SuperLyric.publisher().publish(doc);
if (result != Publisher.PublishResult.OK) {
    // SERVICE_UNAVAILABLE / PAYLOAD_TOO_LARGE / REJECTED
}
```

| 字段 | 要求 | 说明 |
|---|---|---|
| `lyricId` | 必填 | 曲目标识，接收端按此区分缓存 |
| `duration` | 必填，大于 0 | 用于把歌词时间轴对齐到播放进度 |
| `lines`，每行 `start` 与 `end` | 必填 | 行时间决定当前行的计算 |
| `title`、`artist`、`album` | 选填 | 用于显示与服务端曲目校验 |
| `words`、`translation`、`secondary` | 选填 | 缺省时按整行或无翻译处理 |

`lyricId`、`duration`、`lines` 由 `Lyric` 构造器校验，取值非法时抛出 `IllegalArgumentException`（Java 传入 null 时由空值检查先行拦截）。

切歌时无需清理旧数据，系统检测到播放曲目变化后会自行处理。仅在需要立即清除时才调用 `SuperLyric.publisher().clear()`。

## 接收端

Kotlin：

```kotlin
SuperLyric.receiver().register(object : LyricListener {
    override fun onLyric(view: LyricView) {
        val lyric = view.lyric
        val line = view.line
        val index = view.index
        val position = view.position
        val speed = view.speed
        val state = view.state
    }

    override fun onStop(stop: Stop) {
        val reason = stop.reason
    }
})

val current = SuperLyric.receiver().latest()

SuperLyric.receiver().unregister()
```

Java：

```java
SuperLyric.receiver().register(new LyricListener() {
    @Override
    public void onLyric(LyricView view) {
        Lyric doc = view.getLyric();
        Line line = view.getLine();
        int index = view.getIndex();
        long position = view.getPosition();

        List<Line> played = view.getPlayed();
        List<Line> upcoming = view.getUpcoming();
    }

    @Override
    public void onStop(Stop stop) {
        // PAUSED / STOPPED / ERROR / PROCESS_DIED / CLEARED
    }
});

LyricView current = SuperLyric.receiver().latest();
SuperLyric.receiver().unregister();
```

回调在 binder 线程执行。回调中抛出的异常会被捕获并记录，不影响其他接收端。

`position` 是推送时刻的快照。需要连续更新时，可结合 `SystemClock.elapsedRealtime()` 与 `speed` 在本地推算。

## API

| 类型 | 说明 |
|---|---|
| `SuperLyric` | 入口，提供 `isAvailable()`、`publisher()`、`receiver()` |
| `Publisher` | `publish(Lyric)`、`clear()` |
| `Receiver` | `register(LyricListener)`、`unregister()`、`latest()` |
| `LyricListener` | `onLyric(LyricView)`、`onStop(Stop)` |
| `LyricView` | 合成后的视图：歌词文档、当前行、进度、倍速、播放状态 |
| `Lyric`、`Line`、`Word` | 歌词数据模型，只读 |
| `State`、`Stop` | 系统下发的状态与停止事件 |
| `PublishResult`、`RegisterResult` | 调用结果 |

## 兼容性

4.0 重写了源码 API，线上协议保持兼容。

新旧接口各使用独立的服务名（`super_lyric.v4` 与 `super_lyric`），可以任意组合：

| 客户端 | 服务端 | 结果 |
|---|---|---|
| 4.0 | 4.0 | 完整功能 |
| 4.0 | 3.x | 发布时转换为 3.x 格式，接收时转换回 4.0 模型；无倍速与停止原因 |
| 3.x | 4.0 | 服务端按 3.x 格式下发 |
| 3.x | 3.x | 不变 |

客户端通过 `super_lyric.v4` 是否存在判断服务端能力，不使用版本号。

## 扩展字段

在模型类上添加带 `@Wire` 注解的字段即可，无需修改编解码、AIDL 或兼容层：

```kotlin
@field:Wire(7)
var romanization: String? = null
    private set
```

字段号在同一类内唯一，只能新增，不能修改或复用。新增字段时需同步更新 `api/src/test/resources/wire-ids.txt`，`SchemaIdTest` 会校验这份快照。

## 构建与测试

```bash
./gradlew :api:test
./gradlew :api:assembleRelease :app:assembleDebug
```

AAR 自带 `consumer-rules.pro`，使用方无需手动添加 ProGuard 规则。

## 相关项目

- [SuperLyric](https://github.com/HChenX/SuperLyric)
