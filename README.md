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
<p>Super Lyric Api</p>
</div>

---

## ✨ API 介绍

- 本 API 提供了简洁的接口供模块获取歌词或第三方音乐软件发布歌词使用

---

## ✨ 导入依赖

- 添加依赖：

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' } // 添加 JitPack 库
    }
}

dependencies {
    implementation 'com.github.HChenX:SuperLyricApi:3.2' // 引入依赖
}
```

- 同步项目后即可使用本 API。

---

## 🛠 针对模块使用

- 使用方法：

```java
import java.rmi.RemoteException;public class SuperLyricDemo {
    public static void ModuleDemo() {
        ISuperLyricReceiver.Stub receiver;

        SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
            @Override
            public void onLyric(String publisher, SuperLyricData data) throws RemoteException {
                // 发送方发送数据时会调用此方法
                // 以下全部数据需要调用方主动传递，否则可能为 null

                String title = data.getTitle();
                String artist = data.getArtist();
                String album = data.getAlbum();

                if (data.hasLyric()) {
                    SuperLyricLine lyric = data.getLyric(); // 歌词数据
                    if (lyric != null) {
                        String text = lyric.getText();
                        long startTime = lyric.getStartTime();
                        long endTime = lyric.getEndTime();
                        long delay = lyric.getDelay();
                    }
                }

                if (data.hasSecondary()) {
                    SuperLyricLine secondary = data.getSecondary(); // 次要歌词，用法同上
                }
                if (data.hasTranslation()) {
                    SuperLyricLine translation = data.getTranslation(); // 翻译，用法同上
                }

                if (data.hasMediaMetadata()) {
                    MediaMetadata metadata = data.getMediaMetadata(); // MediaMetadata 数据
                }
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState(); // PlaybackState 数据
                }

                if (data.hasExtra()) {
                    Bundle extra = data.getExtra(); // 附加数据
                }

                data.getBase64Icon(); // Base64 Icon
            }

            @Override
            public void onStop(String publisher, SuperLyricData data) throws RemoteException {
                // 暂停歌曲或发送方死亡时会调用此方法
                if (data.hasPlaybackState()) {
                    PlaybackState state = data.getPlaybackState(); // PlaybackState 数据
                }
            }
        });

        SuperLyricHelper.isReceiverRegistered(receiver); // 是否已经注册
        SuperLyricHelper.unregisterReceiver(receiver); // 解除注册
    }
}
```

---

## 🔧 针对音乐软件

- 使用方法：

```java
public class SuperLyricDemo {
    public static void MusicAppDemo() {
        SuperLyricHelper.isAvailable(); // 服务是否可用

        SuperLyricHelper.registerPublisher(); // 注册本应用为发布者，必须调用，否则会抛错
        SuperLyricHelper.unregisterPublisher(); // 主动注销发布者身份，可自行调用，也可交给系统自行控制
        SuperLyricHelper.isPublisherRegistered(); // 是否已经注册为发布者

        SuperLyricHelper.sendLyric(
            new SuperLyricData()
                .setTitle("") // 设置歌曲标题
                .setArtist("") // 设置歌曲艺术家
                // 设置当前歌词
                .setLyric(
                    new SuperLyricLine(
                        "lyric",
                        // 设置逐字数据
                        new SuperLyricWord[]{
                            new SuperLyricWord(
                                "l",
                                100,
                                200
                            ),
                            // 省略
                        },
                        100,
                        600
                    )
                )
                // 设置当前次要歌词
                .setSecondary(
                    new SuperLyricLine(
                        "secondary",
                        100,
                        600
                    )
                )
                // 设置当前翻译
                .setTranslation(
                    new SuperLyricLine(
                        "translation",
                        100,
                        600
                    )
                )
                .setMediaMetadata(null) // MediaMetadata 数据
                .setPlaybackState(null) // PlaybackState 数据
                .setExtra(null) // 设置附加数据
                .setBase64Icon("") // Base64 Icon
        );

        SuperLyricHelper.sendStop(
            new SuperLyricData()
                .setPlaybackState(null) // PlaybackState 数据
        );
    }
}
```

---

## 🌟 混淆配置

```text
// 不建议混淆本 API
-keep class com.hchen.superlyricapi.* {*;}
```

---

## 📢 歌词获取器

- [SuperLyric](https://github.com/HChenX/SuperLyric)

## 🎉结尾

💖 **感谢您的支持，Enjoy your day!** 🚀
