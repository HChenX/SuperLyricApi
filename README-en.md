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
<p>A lightweight Binder-based API for broadcasting and receiving real-time lyrics on Android.</p>
</div>

---

## ✨ Overview

SuperLyricApi provides a clean, minimal interface that allows:

- **Music apps** to publish real-time lyric data (text, word-level timing, translation, playback
  state, etc.) to a system-level service.
- **Xposed modules** can receive these lyric data from system services.

Communication is handled entirely over Binder via AIDL, making it fast, process-safe, and compatible
with Xposed-based module architectures.

---

## ✨ Add the Dependency

Add the JitPack repository and the library dependency to your project:

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
    implementation 'com.github.HChenX:SuperLyricApi:3.7'
}
```

Sync your project and the API is ready to use.

---

## 🛠 For Xposed Modules — Receiving Lyrics

Register an `ISuperLyricReceiver` to receive lyric events. It is recommended to use
`SuperLyricCache.resolve(data)` to automatically merge delta packets with full-song cached lyrics
and support seamless crash recovery.

```java
public static void ModuleDemo() {
    ISuperLyricReceiver.Stub receiver;
    SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
        @Override
        public void onLyric(String publisher, SuperLyricData rawData) throws RemoteException {
            // Recommended: Use SuperLyricCache.resolve to seamlessly reconstruct full-song lyrics from delta packets
            SuperLyricData data = SuperLyricCache.resolve(rawData);
            if (data == null) return;

            String title = data.getTitle();
            String artist = data.getArtist();
            String album = data.getAlbum();
            long duration = data.getDuration(); // Total track duration (ms)
            long position = data.getPosition(); // Current playback position (ms)

            // 1. Current line (single-line channel: the current line in a delta, the first line in a full payload)
            SuperLyricLine currentLine = data.getLyric();
            if (currentLine != null) {
                String text = currentLine.getText();
                long startTime = currentLine.getStartTime(); // ms
                long endTime = currentLine.getEndTime(); // ms
                String translation = currentLine.getTranslation(); // Line translation
                String secondary = currentLine.getSecondary(); // Secondary/romanization text

                // Word-level (karaoke) timing
                SuperLyricWord[] words = currentLine.getWords();
                if (words != null) {
                    for (SuperLyricWord word : words) {
                        String wordText = word.getWord();
                        long wordStartTime = word.getStartTime();
                        long wordEndTime = word.getEndTime();
                    }
                }
            }

            // 2. Access entire song lyrics (for full-screen / multi-line / scrolling list UI)
            if (data.hasAllLyrics()) {
                SuperLyricLine[] allLines = data.getAllLyrics(); // All lines of the song
                int currentIndex = data.getCurrentLyricIndex(); // Index of the currently playing line

                // 3. Instant slicing: played vs. upcoming lines
                List<SuperLyricLine> played = data.getPlayedLyrics(); // Already played history lines
                List<SuperLyricLine> upcoming = data.getUpcomingLyrics(); // Upcoming lines
            }

            if (data.hasExtra()) {
                Bundle extra = data.getExtra();
            }
        }

        @Override
        public void onStop(String publisher, SuperLyricData data) throws RemoteException {
            // Called when the publisher pauses playback or its process dies
        }
    });

    // Active Query: Pull currently playing full lyrics on receiver cold-start or floating window launch
    SuperLyricData currentPlaying = SuperLyricHelper.getLatestLyric();

    // Query registration status or unregister when done
    boolean registered = SuperLyricHelper.isReceiverRegistered(receiver);
    SuperLyricHelper.unregisterReceiver(receiver);
}
```

---

## 🔧 For Music Apps — Publishing Lyrics

Music apps must register as a **publisher** before sending any data.

### 1. Check service availability and version

```java
boolean available = SuperLyricHelper.isAvailable();

// Optional: check that the module installed on the device matches this library
int libraryVersion = SuperLyricHelper.getApiVersion();      // version this library was built from
int serverVersion = SuperLyricHelper.getServerApiVersion(); // version the service implements
// serverVersion == SuperLyricHelper.API_VERSION_UNKNOWN (-1) means the service is too old to answer
```

### 2. Register as a publisher

Call this once when your app starts (e.g., in `onCreate`). Publishing will throw an
`IllegalStateException` if this step is skipped.

```java
public static void MusicAppDemo() {
    SuperLyricHelper.registerPublisher();
}
```

### 3. Send lyric data

**A music app only needs to publish the full payload.** Playback progress and line switching are
driven by SuperLyric's own service-side clock, which builds the lightweight delta packets and
broadcasts them to receivers whenever the current line changes. The app should not push progress
itself.

#### 3.1 On Track Switch / Initial Load (Publish the Full Payload)

```java
public static void MusicAppDemo() {
    // Construct all lyric lines for the song
    SuperLyricLine[] allLines = new SuperLyricLine[]{
        new SuperLyricLine("Hello world", new SuperLyricWord[]{
            new SuperLyricWord("Hello", 0, 400),
            new SuperLyricWord("world", 400, 900),
        }, "你好世界", "Ni Hao Shi Jie", 0, 900),
        new SuperLyricLine("Next lyric line", null, "下一句歌词", null, 900, 2000),
    };

    SuperLyricHelper.sendLyric(
        new SuperLyricData()
            .setTitle("Song Title")
            .setArtist("Artist Name")
            .setAlbum("Album Name")
            .setLyricId("song_unique_id") // Required: session identity, and the key receivers cache on
            .setDuration(180000L) // Required: the anchor the service-side clock interpolates from (repaired from system metadata when absent)
            .setAllLyrics(allLines) // Full track lines
            .setCurrentLyricIndex(0) // Current line index
            .setLyric(allLines[0]) // Backward compatibility for legacy single-line receivers
    );
}
```

#### 3.2 Line Changes / Progress During Playback

**Nothing to do.** Once the full payload is received, SuperLyric derives the playback position from
its own clock, tracks the current line, and broadcasts a delta packet to receivers on every line
change. Pushing progress from the app as well would put two clocks in competition.

### 4. Send a stop event

**Not needed by default.** SuperLyric listens to the system `MediaSession` through
`SystemPlayStateListener` and broadcasts a stop to receivers automatically once the player reaches
`STOPPED` / `NONE` / `ERROR`. Leave that listener enabled.

You only need to send it yourself if you disabled the listener in §6:

```java
public static void MusicAppDemo() {
    SuperLyricHelper.sendStop(
        new SuperLyricData()
    );
}
```

### 5. Unregister (optional)

The system automatically cleans up publisher registration when your app process dies. You may also
unregister explicitly:

```java
public static void MusicAppDemo() {
    SuperLyricHelper.unregisterPublisher();
}
```

### 6. About system playback state

SuperLyric **always** listens to the system's `MediaSession`: it synchronises playback state and
metadata, and broadcasts a stop to receivers when playback ends.

**There is nothing for an integrator to configure** — the per-app switch that used to disable this
listener is no longer exposed.

---

## 📦 Data Model Reference

### `SuperLyricData`

| Method                           | Description                                                          |
|----------------------------------|----------------------------------------------------------------------|
| `setTitle(String)`               | Song title.                                                          |
| `setArtist(String)`              | Artist name.                                                         |
| `setAlbum(String)`               | Album name.                                                          |
| `setLyric(SuperLyricLine)`       | Primary lyric line (single-line mode or current line).               |
| `setSecondary(SuperLyricLine)`   | Secondary lyric line (e.g. romanization).                            |
| `setTranslation(SuperLyricLine)` | Translation of the primary lyric.                                    |
| `setAllLyrics(SuperLyricLine[])` | Sets all lyric lines for the full song.                              |
| `getAllLyrics()`                 | Gets all lyric lines for the song.                                   |
| `getAllLyricsList()`             | Gets all lyric lines as a `List<SuperLyricLine>`.                    |
| `getAllLyricsCount()`            | Gets total line count.                                               |
| `setCurrentLyricIndex(int)`      | Sets 0-based currently playing line index.                           |
| `getCurrentLyricIndex()`         | Gets current line index.                                             |
| `setDuration(long)`              | Sets total track duration in ms.                                     |
| `getDuration()`                  | Gets total track duration in ms.                                     |
| `setPosition(long)`              | Sets current playback timestamp in ms.                               |
| `getPosition()`                  | Gets current playback timestamp in ms.                               |
| `setLyricId(String)`             | Sets unique lyric fingerprint ID for caching & self-healing.         |
| `getLyricId()`                   | Gets unique lyric ID.                                                |
| `getPlayedLyrics()`              | Slices and returns list of already played history lines.             |
| `getUpcomingLyrics()`            | Slices and returns list of upcoming future lines.                    |
| `getLyricAt(int index)`          | Safely accesses lyric line at given index with bounds check.         |
| `setExtra(Bundle)`               | Custom key-value data. Merges with any existing extras.              |
| `isFullPayload()`                | Whether this packet is a full payload (carries the whole lyric).     |
| `isDeltaPayload()`               | Whether this packet is a delta payload (index and current line only).|
| `isStopPayload()`                | Whether this packet is a stop/empty payload (no full lyric, no current line). |

Each field has a corresponding `hasXxx()` guard method (`hasAllLyrics()`, `hasDuration()`, etc.) —
always
check before accessing optional fields.

### `SuperLyricLine`

Represents a single line of lyrics.

| Constructor                                                                 | Description                        |
|-----------------------------------------------------------------------------|------------------------------------|
| `SuperLyricLine(text)`                                                      | Text only.                         |
| `SuperLyricLine(text, startTime, endTime)`                                  | Text with line timing.             |
| `SuperLyricLine(text, words[], startTime, endTime)`                         | Text with word-level timing.       |
| `SuperLyricLine(text, words[], translation, startTime, endTime)`            | Text, words, translation & timing. |
| `SuperLyricLine(text, words[], translation, secondary, startTime, endTime)` | Complete line with all fields.     |

| Method                 | Returns                              | Description                      |
|------------------------|--------------------------------------|----------------------------------|
| `getText()`            | `String` (`@NonNull`)                | Line text                        |
| `getStartTime()`       | `long`                               | Line start time in ms            |
| `getEndTime()`         | `long`                               | Line end time in ms              |
| `getWords()`           | Array of `SuperLyricWord`, or `null` | Word-level timing array          |
| `getTranslation()`     | `String`, or `null`                  | Line translation text            |
| `getSecondary()`       | `String`, or `null`                  | Line secondary/romanization text |
| `getTranslationLine()` | `SuperLyricLine`, or `null`          | Wrapped translation line object  |
| `getSecondaryLine()`   | `SuperLyricLine`, or `null`          | Wrapped secondary line object    |

### `SuperLyricCache`

Provides thread-safe LRU-based full-song caching, delta packet merging, and receiver crash-recovery self-healing without abrupt cache flushes.

| Method                                  | Description                                                                        |
|-----------------------------------------|------------------------------------------------------------------------------------|
| `SuperLyricCache.resolve(incomingData)` | Passes incoming data, automatically merges delta or recovers, returning full view. |
| `SuperLyricCache.put(data)`             | Manually puts full song into cache.                                                |
| `SuperLyricCache.get(lyricId)`          | Retrieves cached full song data.                                                   |
| `SuperLyricCache.clear()`               | Clears cache.                                                                      |

### `SuperLyricWord`

Represents a single word within a lyric line, used for karaoke-style word highlighting.

| Constructor                                | Description       |
|--------------------------------------------|-------------------|
| `SuperLyricWord(word, startTime, endTime)` | Word with timing. |

| Method           | Returns                |
|------------------|------------------------|
| `getWord()`      | Word text (`@NonNull`) |
| `getStartTime()` | Word start time in ms  |
| `getEndTime()`   | Word end time in ms    |

---

## 🌟 ProGuard / R8

It is strongly recommended to keep all API classes unobfuscated:

```proguard
-keep class com.hchen.superlyricapi.* {*;}
```

---

## 📢 Lyrics Finder

- [SuperLyric](https://github.com/HChenX/SuperLyric)

---

## 🎉 Closing

💖 **Thank you for your support — enjoy your day!** 🚀
