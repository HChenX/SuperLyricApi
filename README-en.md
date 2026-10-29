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
<p>A Binder-based lyric transport API for Android.</p>
</div>

---

## Overview

SuperLyricApi carries lyrics between a music app and a system service.

The music app publishes one complete lyric document, including line text, per-word timing,
translation and secondary text. The system service aligns the document with playback progress and
pushes updates to receivers whenever the current line or the playback state changes.

Progress, pause state and playback speed are read from MediaSession by the system. Publishers do not
send them.

Communication uses Binder. The payload is a self-describing byte stream, so new fields do not break
existing receivers.

The API is written in Kotlin and remains fully callable from Java. Implementation types are internal
and not part of the public surface.

## Dependency

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

## Publishing

Call `publish()` once per track, and again when the track changes.

Kotlin:

```kotlin
val lines = listOf(
    Line(
        "Hello world", 0, 900,
        listOf(Word("Hello", 0, 400), Word("world", 400, 900)),
        "你好世界",
        "Ni Hao Shi Jie",
    ),
    Line("Next line", 900, 2000),
)

val doc = Lyric("song_1001", 180_000L, lines, "Title", "Artist", "Album")

when (SuperLyric.publisher().publish(doc)) {
    Publisher.PublishResult.OK -> Unit
    Publisher.PublishResult.SERVICE_UNAVAILABLE -> Unit
    Publisher.PublishResult.PAYLOAD_TOO_LARGE -> Unit
    // REJECTED and its reasons: TRACK_MISMATCH, MALFORMED, NOT_REGISTERED, QUOTA
    else -> Unit
}
```

Java:

```java
List<Line> lines = new ArrayList<>();
lines.add(new Line("Hello world", 0, 900,
        Arrays.asList(new Word("Hello", 0, 400), new Word("world", 400, 900)),
        "你好世界", "Ni Hao Shi Jie"));
lines.add(new Line("Next line", 900, 2000, null, null, null));

Lyric doc = new Lyric("song_1001", 180_000L, lines, "Title", "Artist", "Album");

Publisher.PublishResult result = SuperLyric.publisher().publish(doc);
if (result != Publisher.PublishResult.OK) {
    // SERVICE_UNAVAILABLE / PAYLOAD_TOO_LARGE / REJECTED
}
```

| Field | Requirement | Notes |
|---|---|---|
| `lyricId` | required | track identity used by receiver caches |
| `duration` | required, 0 means unknown | the server may fill it from MediaMetadata |
| `lines` with `start` and `end` | required | line timing determines the current line |
| `title`, `artist`, `album` | optional | display and server-side track matching |
| `words`, `translation`, `phonetic` | optional | omitted values degrade to plain line text |

`lyricId`, `duration` and `lines` are validated by the `Lyric` constructor; invalid values raise
`IllegalArgumentException`, while a null passed from Java is rejected by the null check first.

Clearing is handled by the system when the playing track changes. Call
`SuperLyric.publisher().clear()` only when the lyrics must be removed immediately.

## Receiving

Kotlin:

```kotlin
SuperLyric.receiver().register(object : LyricListener {
    override fun onLyric(view: LyricView) {
        val lyric = view.lyric
        val line = view.line
        val index = view.index
        val position = view.position
    }

    override fun onStop(stop: Stop, publisher: String?) {
        val reason = stop.reason
    }
})

val current = SuperLyric.receiver().latest()

SuperLyric.receiver().unregisterAll()
```

Java:

```java
SuperLyric.receiver().register(new LyricListener() {
    @Override
    public void onLyric(LyricView view) {
        Lyric doc = view.getLyric();
        Line line = view.getLine();
        int index = view.getIndex();
    }

    @Override
    public void onStop(Stop stop, String publisher) {
        // STOPPED / ERROR / PROCESS_DIED / CLEARED; pause does not call onStop
    }
});
```

Callbacks run either on a binder thread or on the library worker thread (the latter for frames
delivered by an asynchronous self-heal). Exceptions thrown from a callback are caught and logged,
and do not affect other receivers.

If a session is playing at registration time, one full state frame arrives immediately; otherwise
nothing is pushed.

`latest()` pulls the terminal state of the most recent session: a view while playing, otherwise the
reason goes into `lastStop()` and the call returns null. A new lyric clears `lastStop()`.

`position` is a snapshot taken when the update was pushed. For continuous updates, extrapolate
locally with `SystemClock.elapsedRealtime()` and `speed`.

## API

| Type | Description |
|---|---|
| `SuperLyric` | Entry point: `isAvailable()`, `serverApiVersion()`, `publisher()`, `receiver()` |
| `Publisher` | `publish(Lyric)`, `clear()` |
| `Receiver` | `register(LyricListener)`, `unregister(listener)`, `unregisterAll()`, `latest()`, `lastStop()` |
| `LyricListener` | `onLyric(LyricView)`, `onStop(Stop)` |
| `LyricView` | Merged view: document, current line, position, speed, playback state, publisher |
| `Lyric`, `Line`, `Word` | Read-only lyric models |
| `State`, `Stop` | State and stop events produced by the system |
| `PublishResult`, `RegisterResult` | Call results |

## Compatibility

Version 4.0 rewrites the source API and keeps the wire protocol compatible.

Each generation has its own service name (`super_lyric.v4` and `super_lyric`), and either side can
be mixed:

| Client | Server | Result |
|---|---|---|
| 4.0 | 4.0 | full feature set |
| 4.0 | 3.x | payloads are converted to and from 3.x format; state is null (unknown), publisher comes from the 3.x callback |
| 3.x | 4.0 | the server emits 3.x payloads |
| 3.x | 3.x | unchanged |

The client tells generations apart by service name: `super_lyric.v4` means 4.x. Within a
generation, use `SuperLyric.serverApiVersion()`; it returns 0 for 3.x or an unavailable service.

| Version | Semantics |
|---|---|
| 40 | every frame carries publisher; pause is expressed by State only; `PublishResult` includes codes 4 to 7; `duration` 0 means unknown; `registerListener` returns a result code |

## Adding a Field

Add a field annotated with `@Wire` to a model class. The codec, AIDL and compatibility layer need
no changes.

```kotlin
@field:Wire(7)
var romanization: String? = null
    private set
```

Field ids are unique within a class, may only be added, and are never changed or reused. When
adding an id, update `api/src/test/resources/wire-ids.txt`; `SchemaIdTest` verifies the snapshot.

## Build and Test

```bash
./gradlew :api:test
./gradlew :api:assembleRelease :app:assembleDebug
```

The AAR ships its own `consumer-rules.pro`; no manual ProGuard rules are required.

## Related

- [SuperLyric](https://github.com/HChenX/SuperLyric)
