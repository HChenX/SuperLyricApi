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

- 引入本 API 可使您简易的获取或发送歌词。
- 本 API 可供模块获取歌词使用或音乐软件主动发送歌词使用。

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
    implementation 'com.github.HChenX:SuperLyricApi:2.2' // 引入依赖
}
```

- 同步项目并下载完成后即可使用本 API。

---

## 🛠 针对模块使用

- 使用方法：

```java
public class Test {
    public void test() {
        Context context = null; // 假设上下文

        ISuperLyric.Stub iSuperLyric;
        // 注册监听
        SuperLyricTool.registerSuperLyric(context, iSuperLyric = new ISuperLyric.Stub() {
            @Override
            public void onSuperLyric(SuperLyricData data) throws RemoteException {
                // 歌曲歌词变化或数据变化时会调用
                data.getLyric(); // 歌词
                data.getDelay(); // 当前歌词的持续时间 (0 值代表无效)
                data.getPackageName(); // 发布歌词的软件
                data.getMediaMetadata(); // 歌曲数据 (可能为 null)
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
        SuperLyricTool.unregisterSuperLyric(context, iSuperLyric);

        // 其他 API 请参考源码注释
    }
}
```

- 几句简单代码即可实现！

---

## 🔧 针对音乐软件

- 使用方法：

```java
public class Test {
    public void test() {
        SuperLyricTool.isEnabled; // 是否被激活

        // 请注意，非常建议您设置包名，这是判断当前播放应用的唯一途径！！

        SuperLyricPush.onSuperLyric(
            new SuperLyricData()
                .setLyric() // 设置歌词 (必选)
                .setPackageName() // 设置软件包名 (必选)
                .setDelay() // 设置当前歌词持续时间
                .setMediaMetadata() // 设置歌曲数据
                .setPlaybackState() // 设置播放状态
                .setExtra(new Bundle()) // 设置其他附加数据
                ...
        ); // 发布歌词

        SuperLyricPush.onStop(
            new SuperLyricData()
                .setPackageName() // 设置软件包名 (必选)
                .setMediaMetadata() // 设置歌曲数据
                .setPlaybackState() // 设置播放状态
                .setExtra(new Bundle()) // 设置其他附加数据
                ...
        ); // 发布歌曲暂停

        // 其他 API 请参考源码注释
    }
}
```

- 然后在歌词获取器内勾选您的音乐应用即可。

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
