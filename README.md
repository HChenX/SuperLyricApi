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
public class Test {
    public void test() {
        ISuperLyricReceiver.Stub receiver;
        // 注册监听
        SuperLyricHelper.registerReceiver(receiver = new ISuperLyricReceiver.Stub() {
            @Override
            public void onLyric(SuperLyricData data) throws RemoteException {
                // 歌曲歌词变化或数据变化时会调用
                data.getLyric(); // 歌词
                data.getDelay(); // 当前歌词的持续时间
                data.getLyricWordData(); // 逐字歌词数据 (可能为 null)
                data.getPackageName(); // 发布歌词的软件
                data.getSecondaryLyric(); // 次要歌词
                data.getSecondaryLyricDelay(); // 次要歌词持续时间
                data.getSecondaryLyricWordData(); // 次要歌词逐字数据 (可能为 null)
                data.getTranslation(); // 歌词的翻译
                data.getTranslationDelay(); // 歌词翻译持续时间
                data.getTranslationWordData(); // 歌词翻译逐字数据 (可能为 null)
                data.getMediaMetadata(); // 歌曲数据 (可能为 null)
                data.getPlaybackState(); // 播放状态 (可能为 null)
                data.getExtra(); // 其他数据 (可能为 null)
                ...
            }

            @Override
            public void onStop(SuperLyricData data) throws RemoteException {
                // 歌曲暂停时会调用
                // 一般会包括以下两个数据可供使用，当然具体要看发送方提供了什么
                data.getPackageName(); // 暂停播放的软件包名
                data.getPlaybackState(); // 播放状态
                ...
            }
        });

        // 解除注册
        SuperLyricHelper.unregisterReceiver(receiver);

        // 其他 API 请参考源码注释
    }
}
```

---

## 🔧 针对音乐软件

- 使用方法：

```java
public class Test {
    public void test() {
        SuperLyricHelper.isAvailable(); // 服务是否可用
        
        SuperLyricHelper.registerPublisher(packageName); // 务必设置，否则会崩溃
        SuperLyricHelper.unregisterPublisher(packageName); // 可选，系统会自动解绑
        SuperLyricHelper.isPublisherRegistered(); // 是否已注册为发行商

        // 请注意，非常建议您设置包名，这是判断当前播放应用的唯一途径！！

        SuperLyricHelper.sendLyric(
            new SuperLyricData()
                .setLyric() // 设置歌词 (必选)
                .setDelay() // 设置当前歌词持续时间 (可选)
                .setPackageName() // 设置软件包名 (必选) // 务必设置，否则不能鉴权
                .setSecondaryLyric() // 设置次要歌词 (可选)
                .setSecondaryLyricDelay() // 设置次要歌词持续时间 (可选)
                .setSecondaryLyricWordData() // 设置次要歌词逐字数据 (可选)
                .setTranslation() // 当前歌词的翻译 (可选，有翻译则建议设置)
                .setTranslationDelay() // 当前歌词翻译的持续时间 (可选)
                .setTranslationWordData() // 当前歌词翻译的逐字数据 (可选)
                .setMediaMetadata() // 设置歌曲数据 (可选)
                .setPlaybackState() // 设置播放状态 (可选)
                .setExtra(new Bundle()) // 设置其他附加数据 (可选)
                .setLyricWordData(new SuperLyricWord[]{
                    new SuperLyricWord("T", 100, 200),
                    new SuperLyricWord("e", 300, 400),
                    new SuperLyricWord("s", 500, 600),
                    new SuperLyricWord("t", 700, 800)
                }) // 逐字歌词数据 (可选)
                ...
        ); // 发布歌词

        SuperLyricHelper.sendStop(
            new SuperLyricData()
                .setPackageName() // 设置软件包名 (必选) // 务必设置，否则不能鉴权
                .setMediaMetadata() // 设置歌曲数据 (可选)
                .setPlaybackState() // 设置播放状态 (可选)
                .setExtra(new Bundle()) // 设置其他附加数据 (可选)
                ...
        ); // 歌曲暂停

        // 其他 API 请参考源码注释
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
