# AgoraPlayer_Quickstart

这个开源示例项目演示了如何快速集成 Android 平台的媒体播放器组件:[Agora MediaPlayer Kit](https://download.agora.io/sdk/release/Agora_Media_Player_for_Android_rel.v1.1.0.91_20200228_2154.zip?_ga=2.22269240.208564419.1583061888-277459906.1543911509)，实现媒体资源的本地播放，以及通过 Agora 视频 SDK 推送到远端播放的功能。

在这个示例项目中包含了以下功能：

- 播放本地和点播的视频文件以及播放控制；
- 同时播放多个媒体资源
- 本地播放视频的同时，推送视频到远端播放；

## 环境准备

- Android Studio 3.3+
- 真实 Android 设备 (Nexus 5X 或者其它设备)
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 运行示例程序

这个段落主要讲解了如何编译和运行实例程序。

### 创建 Agora 账号并获取 App ID

在编译和启动实例程序前，您需要首先获取一个可用的 App ID:
1. 在[agora.io](https://dashboard.agora.io/signin/)创建一个开发者账号
2. 前往后台页面，点击左部导航栏的 **项目 > 项目列表** 菜单
3. 复制后台的 **App ID** 并备注，稍后启动应用时会用到它


5. 将 AppID 填写进 "app/src/main/res/values/strings.xml"
  ```
  <string name="agora_app_id"><#YOUR APP ID#></string>
  ```

### 集成 媒体播放器组件 SDK

	- 在 [Agora.io SDK](https://www.agora.io/cn/download/)，下载最新版 Agora MediaPlayer Kit，然后解压。

  - 按以下对应关系将 **libs** 目录的内容复制到项目内。
                       
      SDK 目录|项目目录
      ---|---
      .jar file|**/apps/lib** folder
      **arm64-v8a** folder|**/app/src/main/jniLibs** folder
      **x86** folder|**/app/src/main/jniLibs** folder
      **armeabi-v7a** folder|**/app/src/main/jniLibs** folder

### 集成 RtcChannelPublishHelper 插件包

	- 前往本项目的 https://github.com/AgoraIO/Agora-Extensions/releases 页面，下载最新的 RtcChannelPublishHelper，然后解压。

	- 选择 Android 平台 2.9.0 版本的 RtcChannelPublishHelper

  - 复制 RtcChannelPublishHelper.aar 到本项目的 `apps/lib` 目录下

### 集成 Agora 视频 SDK

集成方式有以下两种：
  - 首选集成方式：
    - 在项目对应的模块的 `app/build.gradle` 文件的依赖属性中加入通过 JCenter 自动集成 Agora 视频 SDK 的地址：
      ```
      implementation 'io.agora.rtc:full-sdk:2.9.0'
      ```
      (如果要在自己的应用中集成 Agora 视频 SDK，添加链接地址是最重要的一步。）
    - 在 [Agora.io SDK](https://www.agora.io/cn/download/) 下载 **视频通话 + 直播 SDK**，解压后将其中的 **libs**/**include** 文件夹下的 ***.h** 复制到本项目的 **app**/**src**/**main**/**cpp**/**agora** 下。
  - 次选集成方式：
    - 在 [Agora.io SDK](https://www.agora.io/cn/download/) 下载 **视频通话 + 直播 SDK**并解压，按以下对应关系将 **libs** 目录的内容复制到项目内。
      
      SDK 目录|项目目录
      ---|---
      .jar file|**/apps/lib** folder
      **arm64-v8a** folder|**/app/src/main/jniLibs** folder
      **x86** folder|**/app/src/main/jniLibs** folder
      **armeabi-v7a** folder|**/app/src/main/jniLibs** folder


### 启动应用程序

用 Android Studio 打开该项目，连上设备，编译并运行。

也可以使用 `Gradle` 直接编译运行。


## 联系我们

- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 如果在集成中遇到问题, 你可以到 [开发者社区](https://dev.agora.io/cn/) 提问
- 如果有售前咨询问题, 可以拨打 400 632 6626，或加入官方Q群 12742516 提问
- 如果需要售后技术支持, 你可以在 [Agora Dashboard](https://console.agora.io/) 提交工单
- 如果发现了示例代码的 bug, 欢迎提交 [issue](https://github.com/AgoraIO/Agora-Extensions/issues)

## 代码许可

The MIT License (MIT)
