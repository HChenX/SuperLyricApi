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
<p>基于 Binder 的歌词传输 Api</p>
</div>

---

## 概述

SuperLyricApi 使用 Binder 通信将音乐软件歌词数据传递至客户端。

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

极致的轻便：发布歌词数据仅需调用一次 `publish()`，直至下次切歌时再次调用。

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

val doc = Lyric("song_1001", "歌曲标题", "艺术家名称", "专辑名称", 180_000L, lines)

when (SuperLyric.publisher().publish(doc)) {
    Publisher.PublishResult.OK -> Unit
    Publisher.PublishResult.SERVICE_UNAVAILABLE -> Unit
    Publisher.PublishResult.PAYLOAD_TOO_LARGE -> Unit
    // REJECTED 及细分原因：TRACK_MISMATCH、MALFORMED、NOT_REGISTERED、QUOTA
    else -> Unit
}
```

Java：

```java
List<Line> lines = new ArrayList<>();
lines.add(new Line("你好世界", 0, 900,
        Arrays.asList(new Word("你好", 0, 400), new Word("世界", 400, 900)),
        "Hello World", "Ni Hao Shi Jie"));
lines.add(new Line("下一句歌词", 900, 2000, null, "Next Line", null));

Lyric doc = new Lyric("song_1001", "歌曲标题", "艺术家名称", "专辑名称", 180_000L, lines);

Publisher.PublishResult result = SuperLyric.publisher().publish(doc);
if (result != Publisher.PublishResult.OK) {
    // 0 OK / 1 SERVICE_UNAVAILABLE / 2 PAYLOAD_TOO_LARGE / 3..7 各类 REJECTED
}
```

| 字段 | 要求 | 说明 |
|---|---|---|
| `lyricId` | 必填 | 曲目标识，接收端按此区分缓存 |
| `title` | 必填，不能为空白 | 用于显示与服务端曲目校验 |
| `artist`、`album` | 必填参数，可传空串 | 空串表示未知 |
| `duration` | 必填，0 表示未知 | 服务端可补齐 |
| `lines`，每行 `start` 与 `end` | 必填 | 行时间决定当前行的计算 |
| `words`、`translation`、`phonetic` | 选填 | 缺省时按整行或无翻译处理 |

`lyricId`、`duration`、`lines` 由 `Lyric` 构造器校验，`title` 由 `publish()` 校验，取值非法时抛出 `IllegalArgumentException`。`duration` 为 0 表示未知，服务端会尝试补齐。

切歌时无需清理旧数据，系统检测到播放曲目变化后会自行处理。仅在需要立即清除时才调用 `SuperLyric.publisher().clear()`。

## 接收端

简洁优雅：只需一行代码即可注册使用。

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
        val publisher = stop.publisher
    }
})

val current = SuperLyric.receiver().latest()

SuperLyric.receiver().unregisterAll()
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
        // stop.getReason() 与 stop.getPublisher()；暂停不触发 onStop
    }
});

LyricView current = SuperLyric.receiver().latest();
SuperLyric.receiver().unregisterAll();
```
注：

支持注册多个不同的接收器。

回调可能在 binder 线程或库内部工作线程执行，请注意不要直接进行 UI 操作。

注册时若服务端正在播放，会立即收到一帧完整状态；没有在播会话则不会主动推送。

`latest()` 主动拉取最近一次会话的终态。`lastStop()` 主动拉取最近一次的会话结束终态，收到新的歌词后会自动清空。

`position` 是推送时刻的快照。需要连续更新时，可结合 `SystemClock.elapsedRealtime()` 与 `speed` 在本地推算。

## API

| 类型 | 说明 |
|---|---|
| `SuperLyric` | 入口，提供 `isAvailable()`、`serverApiVersion()`、`publisher()`、`receiver()` |
| `Publisher` | `publish(Lyric)`、`clear()` |
| `Receiver` | `register(LyricListener)`、`unregister(listener)`、`unregisterAll()`、`latest()`、`lastStop()` |
| `LyricListener` | `onLyric(LyricView)`、`onStop(Stop)` |
| `LyricView` | 合成后的视图：歌词文档、当前行、进度、倍速、播放状态、发布方 |
| `Lyric`、`Line`、`Word` | 歌词数据模型，只读 |
| `State`、`Stop` | 系统下发的状态与停止事件 |
| `PublishResult`、`RegisterResult` | 调用结果 |

## 兼容性

4.0 与 3.x 的线上协议互通，源码 API 不兼容。

新旧接口各使用独立的服务名（`super_lyric.v4` 与 `super_lyric`），可以任意组合：

| 客户端 | 服务端 | 结果 |
|---|---|---|
| 4.0 | 4.0 | 完整功能 |
| 4.0 | 3.x | 发布时转换为 3.x 格式，接收时转换回 4.0 模型；state 为 null（未知），publisher 取 3.x 回调携带的包名 |
| 3.x | 4.0 | 服务端按 3.x 格式下发 |
| 3.x | 3.x | 不变 |

客户端用服务名判断代际：存在 `super_lyric.v4` 即为 4.x。同一代内部的语义差异用 `SuperLyric.serverApiVersion()` 判断（3.x 或不可用时返回 0）。

## 扩展字段

以下步骤面向库维护者。`Wire` 注解是 internal 的，使用方无法也不应给模型加字段。

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
