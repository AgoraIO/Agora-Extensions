# Agora(Rtmp) Streaming Kit Demo for Android

*English | [中文](README.zh.md)*

- [1. Summary](#1-summary)
- [2. Prerequisites](#2-prerequisites)
- [3. Quick Start](#3-quick-start)
  - [3.1. Obtain an App ID](#31-obtain-an-app-id)
  - [3.2. Integrate the Agora Streaming Kit](#32-integrate-the-agora-streaming-kit)
    - [3.2.1. JCenter](#321-jcenter)
    - [3.2.2. Manually](#322-manually)
  - [3.3. Integrate the Agora Video SDK](#33-integrate-the-agora-video-sdk)
    - [3.3.1. JCenter](#331-jcenter)
    - [3.3.2. Manually](#332-manually)
  - [3.4. flavor](#34-flavor)
  - [3.5. About the beauty function](#35-about-the-beauty-function)
  - [3.6. Run the Application](#36-run-the-application)
- [4. NOTICE for using Agora Streaming SDK](#4-notice-for-using-agora-streaming-sdk)
  - [4.1. APIc](#41-apic)
- [5. Resources](#5-resources)
- [6. Demo License](#6-demo-license)

## 1. Summary

The RtmpStreaming for Android Sample App is an open-source demo that will help you get live streaming integrated directly into your Android applications using the Agora Video SDK and Agora Streaming Kit.

With this sample app, you can:

- Publish camera stream to CDN
- Join / leave channel
- Mute / unmute audio
- Switch camera
- Setup resolution, frame rate and bit rate
- Camera control
  - Set focus
  - Change zoom of camera
- Snapshot an image when capturing
- Screen capture (based foreground service)
- beautifying filter(powerd by faceunity)

## 2. Prerequisites

- Android Studio 3.3 or above
- Real devices (Nexus 5X or other devices)
- Some simulators are function missing or have performance issue, so real device is the best choice

## 3. Quick Start

This section shows you how to prepare, build, and run the sample application.

### 3.1. Obtain an App ID

To build and run the sample application, get an App ID:

1. Create a developer account at [agora.io](https://dashboard.agora.io/signin/). Once you finish the signup process, you will be redirected to the Dashboard.
2. Navigate in the Dashboard tree on the left to **Projects** > **Project List**.
3. Save the **App ID** from the Dashboard for later use.
4. Update "app/build.gradle" with your App ID and replace 'xxxxxxxxxxxx'.NOTICE, App ID needs to be wrapped in double quotes.
5. You need a rtmp push server address to replace the blank space behind default_rtmp_url

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

For example:

```groovy
        ga {
            // General Availability
            dimension "dev_mode"

            resValue "string", "private_app_id", "abcdefghjy"
            buildConfigField("String", "default_rtmp_url", "\"rtmp://192.168.1.2/live/sss\"")
        }
```

### 3.2. Integrate the Agora Streaming Kit

The Streaming Kit must be integrated into the sample project before it can opened and built. There are two methods for integrating the Agora Streaming Kit into the sample project. The first method uses JCenter to automatically integrate the SDK files. The second method requires you to manually copy the SDK files to the project.

#### 3.2.1. JCenter

**Integrate the Kit Automatically Using JCenter (Recommended, and it's the way this demo uses)**

1. Clone this repository.
2. Open **app/build.gradle** and add the following line to the `dependencies` list:

```groovy
  ...
  dependencies {
      ...
      implementation 'io.agora:streamingkit:1.1.0.0'
  }
```

#### 3.2.2. Manually

**Manually copy the SDK files**

1. Download the Agora Streaming Kit from [Agora.io SDK](https://www.agora.io/en/download/).
2. Unzip the downloaded package.
3. Copy the following files from from the **libs** folder of the downloaded SDK package:

| Copy from SDK          | Copy to Project Folder           |
| ---------------------- | -------------------------------- |
| .jar file              | **/apps/libs** folder            |
| **arm64-v8a** folder   | **/app/src/main/jniLibs** folder |
| **x86** folder         | **/app/src/main/jniLibs** folder |
| **armeabi-v7a** folder | **/app/src/main/jniLibs** folder |

### 3.3. Integrate the Agora Video SDK

The SDK must be integrated into the sample project before it can opened and built. There are two methods for integrating the Agora Video SDK into the sample project. The first method uses JCenter to automatically integrate the SDK files. The second method requires you to manually copy the SDK files to the project.

#### 3.3.1. JCenter

**Integrate the SDK Automatically Using JCenter (Recommended, and it's the way this demo uses)**

1. Clone this repository.
2. Open **app/build.gradle** and add the following line to the `dependencies` list:

```groovy
  ...
  dependencies {
      ...
      implementation 'io.agora.rtc:full-sdk:3.2.1'
  }
```

#### 3.3.2. Manually

**Manually copy the SDK files**

1. Download the Agora Video SDK from [Agora.io SDK](https://www.agora.io/en/download/).
2. Unzip the downloaded SDK package.
3. Copy the following files from from the **libs** folder of the downloaded SDK package:

| Copy from SDK          | Copy to Project Folder           |
| ---------------------- | -------------------------------- |
| .jar file              | **/apps/libs** folder            |
| **arm64-v8a** folder   | **/app/src/main/jniLibs** folder |
| **x86** folder         | **/app/src/main/jniLibs** folder |
| **armeabi-v7a** folder | **/app/src/main/jniLibs** folder |

### 3.4. flavor

The demo project provides two kinds of `flavor`, std and fu

-std: standard project
-fu: Use faceunity to realize the beauty function

By default, fu is disabled. If you want to enable the beauty function, you need to set the following options in `local.properties`

```groovy
buildWithBeauty=true
```

After setting up, please re-sync the project.

1. Click the `File` menu of Android Studio
2. Click the option of `Sync Project with Gradle Files`

After the synchronization is complete, you can see fu-related build options in the sidebar of `Build Variants` on the left

### 3.5. About the beauty function

-The demo uses the VideoFilter mechanism of `Agora Streaming Kit` to integrate faceunity.
-If you want to use faceunity related functions, you need to go to [faceunity official website](https://www.faceunity.com/en) to register and get the key file `authpack.java`, and copy this file to `faceunity/src/ main/java/com/faceunity` directory
-Developers can also integrate other third-party beauty components by themselves

**NOTICE**

Demo integrates faceunity v6.6, please do not upgrade the version number, otherwise there will be compatibility issues

### 3.6. Run the Application

Open project with Android Studio, connect your Android device, build and run.

Or use `Gradle` to build and run.

## 4. NOTICE for using Agora Streaming SDK

### 4.1. APIc

## 5. Resources

- [Agora Streaming Kit official documentation](https://docs.agora.io/cn/Interactive%20Broadcast/streamingkit_android?platform=Android#%E9%9B%86%E6%88%90-streaming-kit)
- For potential issues, take a look at our [FAQ](https://docs.agora.io/cn/faq) first
- Dive into [Agora SDK Samples](https://github.com/AgoraIO) to see more tutorials
- Take a look at [Agora Use Case](https://github.com/AgoraIO-usecase) for more complicated real use case
- Repositories managed by developer communities can be found at [Agora Community](https://github.com/AgoraIO-Community)
- You can find full API documentation at [Document Center](https://docs.agora.io/en/)
- If you encounter problems during integration, you can ask question in [Stack Overflow](https://stackoverflow.com/questions/tagged/agora.io)
- You can file bugs about this sample at [issue](https://github.com/AgoraIO/Basic-Video-Broadcasting/issues)

## 6. Demo License

The MIT License (MIT)
