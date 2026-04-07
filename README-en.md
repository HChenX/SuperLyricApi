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
- **Xposed modules** to receive that data and display lyrics anywhere in the system UI.

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
    implementation 'com.github.HChenX:SuperLyricApi:3.2'
}
```

Sync your project and the API is ready to use.

---

## 🛠 For Xposed Modules — Receiving Lyrics

Register an `ISuperLyricReceiver` to receive lyric events from any publishing music app.

```java
import java.rmi.RemoteException;

public class SuperLyricDemo {
    public static void ModuleDemo() {
        ISuperLyricReceiver.Stub receiver;

        SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
            @Override
            public void onLyric(SuperLyricData data) throws RemoteException {
                // Called each time a publisher sends a new lyric line.

                String packageName = data.getPackageName(); // Sender's package name (always present)

                // All fields below are optional — check before use.
                String title = data.getTitle();
                String artist = data.getArtist();
                String album = data.getAlbum();

                if (data.hasLyric()) {
                    SuperLyricLine lyric = data.getLyric();
                    if (lyric != null) {
                        String text = lyric.getText();
                        long startTime = lyric.getStartTime(); // ms
                        long endTime = lyric.getEndTime();   // ms
                        long delay = lyric.getDelay();     // duration = endTime - startTime

                        // Word-level (karaoke) data — may be null
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
                if (data.hasMediaMetadata()) {
                    MediaMetadata metadata = data.getMediaMetadata();
                }
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState();
                }
                if (data.hasExtra()) {
                    Bundle extra = data.getExtra();
                }
            }

            @Override
            public void onStop(SuperLyricData data) throws RemoteException {
                // Called when the publisher pauses playback or its process dies.
                String packageName = data.getPackageName();
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState();
                }
            }
        });

// Query registration status or unregister when done
        boolean registered = SuperLyricHelper.isReceiverRegistered(receiver);
        SuperLyricHelper.unregisterReceiver(receiver);
    }
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
public static void MusicAppDemo(Context context) {
    SuperLyricHelper.registerPublisher(context);
}
```

### 3. Send lyric data

```java
public static void MusicAppDemo(Context context) {
    SuperLyricHelper.sendLyric(
        new SuperLyricData()
            .setPackageName(context.getPackageName()) // Required — must match your app's package name
            .setTitle("Song Title")
            .setArtist("Artist Name")
            .setLyric(
                new SuperLyricLine(
                    "Hello world",                    // Line text
                    new SuperLyricWord[]{              // Optional word-level timing
                        new SuperLyricWord("Hello", 0, 400),
                        new SuperLyricWord("world", 400, 900),
                    },
                    0,    // Line start time (ms)
                    900   // Line end time (ms)
                )
            )
            .setSecondary(new SuperLyricLine("Secondary line", 0, 900))     // Optional
            .setTranslation(new SuperLyricLine("Translation line", 0, 900)) // Optional
            .setMediaMetadata(mediaMetadata)  // Optional; Bitmap fields are stripped automatically
            .setPlaybackState(playbackState)  // Optional
            .setExtra(extraBundle)            // Optional custom data
    );
}
```

### 4. Send a stop event

Call this when playback pauses or stops.

```java
public static void MusicAppDemo(Context context) {
    SuperLyricHelper.sendStop(
        new SuperLyricData()
            .setPackageName(context.getPackageName())
            .setPlaybackState(playbackState) // Optional
    );
}
```

### 5. Unregister (optional)

The system automatically cleans up publisher registration when your app process dies. You may also
unregister explicitly:

```java
import javax.naming.Context;

public static void MusicAppDemo(Context context) {
    SuperLyricHelper.unregisterPublisher(context);
}
```

### 6. System playback state listener

By default, SuperLyric listens to the system's `MediaSession` events to automatically handle
stop/metadata/playback-state changes on your behalf. If you prefer to manage these manually, disable
it:

```java
public static void MusicAppDemo(Context context) {
    SuperLyricHelper.setSystemPlayStateListenerEnabled(context, false);
    // Then call sendStop(), setMediaMetadata(), setPlaybackState() yourself as needed.
}
```

---

## 📦 Data Model Reference

### `SuperLyricData`

| Method                            | Description                                                                                          |
|-----------------------------------|------------------------------------------------------------------------------------------------------|
| `setPackageName(String)`          | **Required.** Your app's package name.                                                               |
| `setTitle(String)`                | Song title.                                                                                          |
| `setArtist(String)`               | Artist name.                                                                                         |
| `setAlbum(String)`                | Album name.                                                                                          |
| `setLyric(SuperLyricLine)`        | Primary lyric line.                                                                                  |
| `setSecondary(SuperLyricLine)`    | Secondary lyric line (e.g. romanization).                                                            |
| `setTranslation(SuperLyricLine)`  | Translation of the primary lyric.                                                                    |
| `setMediaMetadata(MediaMetadata)` | Song metadata. **Bitmap fields are stripped automatically** to avoid Binder transaction size limits. |
| `setPlaybackState(PlaybackState)` | Current playback state.                                                                              |
| `setExtra(Bundle)`                | Custom key-value data. Merges with any existing extras.                                              |
| `merge(SuperLyricData)`           | Non-destructively merges another `SuperLyricData` into this one (only non-null fields are applied).  |

Each field has a corresponding `hasXxx()` guard method (`hasLyric()`, `hasTitle()`, etc.) — always
check before accessing optional fields.

### `SuperLyricLine`

Represents a single line of lyrics.

| Constructor                                         | Description                  |
|-----------------------------------------------------|------------------------------|
| `SuperLyricLine(text)`                              | Text only.                   |
| `SuperLyricLine(text, startTime, endTime)`          | Text with line timing.       |
| `SuperLyricLine(text, words[], startTime, endTime)` | Text with word-level timing. |

| Method           | Returns                                     |
|------------------|---------------------------------------------|
| `getText()`      | Line text (`@NonNull`)                      |
| `getStartTime()` | Line start time in ms                       |
| `getEndTime()`   | Line end time in ms                         |
| `getDelay()`     | Line duration in ms (`endTime - startTime`) |
| `getWords()`     | Array of `SuperLyricWord`, or `null`        |

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
| `getDelay()`     | Word duration in ms    |

---

## 🌟 ProGuard / R8

It is strongly recommended to keep all API classes unobfuscated:

```proguard
-keep class com.hchen.superlyricapi.** { *; }
```

---

## 📢 Related Projects

- [SuperLyric](https://github.com/HChenX/SuperLyric) — the Xposed module that consumes this API to
  display lyrics in the system UI.

---

## 🎉 Closing

💖 **Thank you for your support — enjoy your day!** 🚀
