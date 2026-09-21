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
    implementation 'com.github.HChenX:SuperLyricApi:3.5'
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

            // 1. Get currently playing line (backward-compatible and works for single-line or full-song)
            SuperLyricLine currentLine = data.getCurrentLyric();
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

### 1. Check service availability

```java
boolean available = SuperLyricHelper.isAvailable();
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

We recommend using the **Full Payload + Delta Progress Payload** optimization strategy:

#### 3.1 On Track Switch / Initial Load (Send Full Payload)

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

    SuperLyricHelper.sendFullLyric(
        new SuperLyricData()
            .setTitle("Song Title")
            .setArtist("Artist Name")
            .setAlbum("Album Name")
            .setLyricId("song_unique_id") // Recommended unique ID for cache matching & self-healing
            .setDuration(180000L) // Total duration (ms)
            .setAllLyrics(allLines) // Full track lines
            .setCurrentLyricIndex(0) // Current line index
            .setLyric(allLines[0]) // Backward compatibility for legacy single-line receivers
    );
}
```

#### 3.2 During Playback / Line Changes (Send Lightweight Delta Payload)

During playback, there is no need to re-transmit the heavy `allLyrics` array across Binder:

```java
public static void onLineChanged(int newIndex, long currentPositionMs, SuperLyricLine currentLine) {
    SuperLyricHelper.sendLyricProgress(
        new SuperLyricData()
            .setLyricId("song_unique_id")
            .setCurrentLyricIndex(newIndex) // Current line index
            .setPosition(currentPositionMs) // Playback timestamp (ms)
            .setLyric(currentLine) // Backward compatibility
    );
}
```

### 4. Send a stop event

Call this when playback pauses or stops.

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

### 6. System playback state listener

By default, SuperLyric listens to the system's `MediaSession` events to automatically handle
playback-state change. If you prefer to manage these manually, disable it:

```java
public static void MusicAppDemo() {
    SuperLyricHelper.setSystemPlayStateListenerEnabled(false);
    // Then call sendStop(), setPlaybackState() yourself as needed.
}
```

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
| `getCurrentLyric()`              | Smartly gets the current line (priority to single line, else index). |
| `getPlayedLyrics()`              | Slices and returns list of already played history lines.             |
| `getUpcomingLyrics()`            | Slices and returns list of upcoming future lines.                    |
| `getLyricAt(int index)`          | Safely accesses lyric line at given index with bounds check.         |
| `setExtra(Bundle)`               | Custom key-value data. Merges with any existing extras.              |

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

Provides full-song caching, delta packet merging, and receiver crash-recovery self-healing.

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
