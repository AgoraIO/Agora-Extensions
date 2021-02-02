# Agora(Rtmp) Streaming Kit Demo for Android

*[English](README.md) | 中文*

- [1. 概述](#1-概述)
- [2. 环境准备](#2-环境准备)
- [3. 运行示例程序](#3-运行示例程序)
  - [3.1. 创建Agora账号并获取AppId](#31-创建agora账号并获取appid)
  - [3.2. 集成 Agora Streaming Kit](#32-集成-agora-streaming-kit)
    - [3.2.1. 通过JCenter集成](#321-通过jcenter集成)
    - [3.2.2. 手动集成](#322-手动集成)
  - [3.3. 集成 Agora 视频 SDK](#33-集成-agora-视频-sdk)
    - [3.3.1. 通过JCenter集成](#331-通过jcenter集成)
    - [3.3.2. 手动集成](#332-手动集成)
  - [3.4. 风味（flavor）](#34-风味flavor)
  - [3.5. 关于美颜功能](#35-关于美颜功能)
  - [3.6. 启动应用程序](#36-启动应用程序)
- [4. 联系我们](#4-联系我们)
- [5. Demo代码许可](#5-demo代码许可)

## 1. 概述

这个开源示例项目演示了如何快速集成 Agora 推流组件包 和 Agora 视频 SDK，实现单主播直推 CDN，以及切换到声网频道进行连麦直播和旁路推流。

在这个示例项目中包含了以下功能：

- 摄像头视频流直推 CDN
- 加入和离开声网频道；
- 静音和解除静音；
- 切换前置摄像头和后置摄像头；
- 选择分辨率、码率和帧率；
- 摄像头控制相关功能
  - 设置焦点
  - 摄像头缩放
- 截图
- 实时屏幕采集推流（基于前台服务）
- 美颜（基于faceunity）

## 2. 环境准备

- Android Studio 3.3 +
- 真实 Android 设备 (Nexus 5X 或者其它设备)
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

## 3. 运行示例程序

这个段落主要讲解了如何编译和运行实例程序。

### 3.1. 创建Agora账号并获取AppId

在编译和启动实例程序前，您需要首先获取一个可用的App ID:

1. 在[agora.io](https://dashboard.agora.io/signin/)创建一个开发者账号
2. 前往后台页面，点击左部导航栏的 **项目 > 项目列表** 菜单
3. 复制后台的 **App ID** 并备注，稍后启动应用时会用到它
4. 将 AppID 填写进 "app/build.gradle", 替换掉xxxxxxxxxxxx部分。注意，appid需要双引号包裹。
5. 你需要一个rtmp推流服务器地址来替换default_rtmp_url后面空白处

```groovy
        ga {
            // General Availability
            dimension "dev_mode"

            // Get your own App ID at https://dashboard.agora.io/
            // PLEASE KEEP THIS App ID IN SAFE PLACE
            // you must give one to replace xxxxxxxxxxxx
            resValue "string", "private_app_id", xxxxxxxxxxxx

            // default rtmp pushing-url
            // you must give one like "\"xxxxx_url\""
            buildConfigField("String", "default_rtmp_url", "\"\"")
        }

```

举例：

```groovy
        ga {
            // General Availability
            dimension "dev_mode"

            resValue "string", "private_app_id", "abcdefghjy"
            buildConfigField("String", "default_rtmp_url", "\"rtmp://192.168.1.2/live/sss\"")
        }
```

### 3.2. 集成 Agora Streaming Kit

集成方式有以下两种：

#### 3.2.1. 通过JCenter集成

**推荐方式，本开源项目也是采用这种方式**

在项目对应的模块的 `app/build.gradle` 文件的依赖属性中加入通过 JCenter 自动集成 Agora 推流 Kit 的地址：

```groovy
  ...
  dependencies {
      ...
      implementation 'io.agora:streamingkit:1.1.0.0'
  }
```

#### 3.2.2. 手动集成

在 [Agora.io SDK](https://www.agora.io/cn/download/) 下载 **Agora 推流 Kit**并解压，按以下对应关系将 **libs** 目录的内容复制到项目内。

| SDK目录                | 项目目录                         |
| ---------------------- | -------------------------------- |
| .jar file              | **/apps/libs** folder            |
| **arm64-v8a** folder   | **/app/src/main/jniLibs** folder |
| **x86** folder         | **/app/src/main/jniLibs** folder |
| **armeabi-v7a** folder | **/app/src/main/jniLibs** folder |

### 3.3. 集成 Agora 视频 SDK

集成方式有以下两种：

#### 3.3.1. 通过JCenter集成

**推荐方式，Demo 也是采用这种方式**

在项目对应的模块的 `app/build.gradle` 文件的依赖属性中加入通过 JCenter 自动集成 Agora 视频 SDK 的地址：

```groovy
    implementation 'io.agora.rtc:full-sdk:3.2.1'
```

#### 3.3.2. 手动集成

在 [Agora.io SDK](https://www.agora.io/cn/download/) 下载 **视频通话 + 直播 SDK**并解压，按以下对应关系将 **libs** 目录的内容复制到项目内。

| SDK目录                | 项目目录                         |
| ---------------------- | -------------------------------- |
| .jar file              | **/apps/libs** folder            |
| **arm64-v8a** folder   | **/app/src/main/jniLibs** folder |
| **x86** folder         | **/app/src/main/jniLibs** folder |
| **armeabi-v7a** folder | **/app/src/main/jniLibs** folder |

若需要需要使用C++头文件，可以在解压SDK后将其中的 **libs**/**include** 文件夹下的 ***.h** 复制到本项目的 **app**/**src**/**main**/**cpp**/**agora** 下。

### 3.4. 风味（flavor）

demo工程提供了两种`flavor`，std 和 fu

- std：标准的工程
- fu：使用faceunity实现了美颜功能

默认情况下，fu是被禁用的。如果您想开启美颜功能，需要在`local.properties`中设置如下选项

```groovy
buildWithBeauty=true
```

设置完成后，请重新同步工程。

1. 点击Android Studio的`File`菜单
2. 点击`Sync Project with Gradle Files`选项

同步完成后，您可以在左侧`Build Variants`侧边栏看到fu相关编译选项

### 3.5. 关于美颜功能

- Demo使用`Agora Streaming Kit`的VideoFilter机制集成faceunity。
- 如果您想使用faceunity相关功能，需要到[faceunity官网](https://www.faceunity.com/)注册并申请密钥文件`authpack.java`, 并将此文件复制到```faceunity/src/main/java/com/faceunity```目录下
- 开发者也可自行集成其他第三方美颜组件

**NOTICE**

Demo集成的是faceunity 6.6版本，请不要升级版本号，否则会有兼容问题

### 3.6. 启动应用程序

用 Android Studio 打开该项目，连上设备，编译并运行。

也可以使用 `Gradle` 直接编译运行。

## 4. 联系我们

- [Agora Streaming Kit官方文档](https://docs.agora.io/cn/Interactive%20Broadcast/streamingkit_android?platform=Android#%E9%9B%86%E6%88%90-streaming-kit)
- 如果你遇到了困难，可以先参阅[常见问题](https://docs.agora.io/cn/faq)
- 如果你想了解更多官方示例，可以参考[官方SDK示例](https://github.com/AgoraIO)
- 如果你想了解声网SDK在复杂场景下的应用，可以参考[官方场景案例](https://github.com/AgoraIO-usecase)
- 如果你想了解声网的一些社区开发者维护的项目，可以查看[社区](https://github.com/AgoraIO-Community)
- 完整的 API 文档见 [文档中心](https://docs.agora.io/cn/)
- 若遇到问题需要开发者帮助，你可以到 [开发者社区](https://rtcdeveloper.com/) 提问
- 如果发现了示例代码的 bug，欢迎提交 [issue](https://github.com/AgoraIO/Basic-Video-Broadcasting/issues)

## 5. Demo代码许可

The MIT License (MIT)
