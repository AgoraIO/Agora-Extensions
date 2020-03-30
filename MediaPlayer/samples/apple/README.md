# AgoraPlayer_Quickstart


这个开源示例项目演示了如何快速集成 iOS/macOS 平台的媒体播放器组件:[Agora MediaPlayer Kit](https://download.agora.io/sdk/release/Agora_Media_Player_for_iOS_rel.v1.1.0.8352_20200228_2230.zip?_ga=2.104458880.1681401152.1583073151-1045091424.1511772004)，实现媒体资源的本地播放，以及通过 Agora 视频 SDK 推送到远端播放的功能。

在这个示例项目中包含了以下功能：

- 播放本地和点播的视频文件以及播放控制；
- 同时播放多个媒体资源
- 本地播放视频的同时，推送视频到远端播放；

## 环境准备

- xCode 9.0+
- iOS 10.0+
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 运行示例程序

这个段落主要讲解了如何编译和运行实例程序。

### 创建 Agora 账号并获取 App ID

在编译和启动实例程序前，您需要首先获取一个可用的 App ID:
1. 在[agora.io](https://dashboard.agora.io/signin/)创建一个开发者账号
2. 前往后台页面，点击左部导航栏的 **项目 > 项目列表** 菜单
3. 复制后台的 **App ID** 并备注，稍后启动应用时会用到它


5. 将 AppID 填写进 
  ```
 _rtcEnginekit = [AgoraRtcEngineKit sharedEngineWithAppId:YOUR_APP_ID delegate:self];

  ```

### 集成 媒体播放器组件 SDK

	- 在 [Agora.io SDK](https://www.agora.io/cn/download/)，下载最新版 Agora MediaPlayer Kit，然后解压。

  - 按以下对应关系将 **libs** 目录的内容复制到项目内。
                       
      SDK 目录|项目目录
      ---|---
      .libs file|**Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/ios** folder
      **AgoraRtcEngineKit.framework** folder|**Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/ios** folder
### 集成 RtcChannelPublishHelper 插件包

  - 复制 Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin 中的 RtcChannelPublishPlugin 到本项目的目录下(此 sample 默认集成好的)

### 集成 Agora 视频 SDK
 ```
    https://docs.agora.io/cn/Interactive%20Broadcast/start_live_ios?platform=iOS
 ```
 
### 启动应用程序

用 Xcode 打开该项目，连上设备，编译并运行。

## 联系我们

- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 如果在集成中遇到问题, 你可以到 [开发者社区](https://dev.agora.io/cn/) 提问
- 如果有售前咨询问题, 可以拨打 400 632 6626，或加入官方 Q 群 12742516 提问
- 如果需要售后技术支持, 你可以在 [Agora Dashboard](https://console.agora.io/) 提交工单
- 如果发现了示例代码的 bug, 欢迎提交 [issue](https://github.com/AgoraIO/Agora-Extensions/issues)

## 代码许可

The MIT License (MIT)
