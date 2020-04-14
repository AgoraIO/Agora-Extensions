
READ THIS IN OTHER LANGUAGES [🇨🇳](README.zh.md). Contributions welcome!

# AgoraPlayer_Quickstart

This tutorial enables you to quickly get started in your development efforts to create an iOS app with media player provided by [Agora.io](https://www.agora.io/en/). With [Agora MediaPlayer Kit](https://download.agora.io/sdk/release/Agora_Media_Player_for_iOS_rel.v1.1.0.8352_20200228_2230.zip?_ga=2.104458880.1681401152.1583073151-1045091424.1511772004) you can play most popular multimedia files and various streaming protocols. Besides that, you can publish video/audio stream playing to Agora RTC channel with Agora RTC SDK(`RtcChannelPublishHelper` is just what you need)

With this sample app, you can:

- Play multimedia files or streaming progocols.
- Play multi at the same time
- Control the playback(pause/resume/mute/volume-control and etc.).
- Publish to Agora RTC channel.


## Prerequisites

- Xcode 9.0+  or above
- iOS 10.0 +
- Recommend Real-machine running the App.


## Quick Start

This section shows you how to prepare, build, and run the sample application.

### Obtain an App ID

To build and run the sample application, get an App ID:

1. Create a developer account at [agora.io](https://console.agora.io/signin/). Once you finish the signup process, you will be redirected to the Console.
2. Navigate in the Console tree on the left to **Projects** > **Project List**.
3. Save the **App ID** from the Console for later use.
4. Locate the file **AgoraPlayer_Quickstart_Mac/ViewController.mm or AgoraPlayer_Quickstart_iOS/ViewControlle.mm** and replace <#YOUR APP ID#> with the App ID in the console.

  ```
  _rtcEnginekit = [AgoraRtcEngineKit sharedEngineWithAppId:<#YOUR_APPID#> delegate:self];
  ```

### Integrate the Agora MediaPlayer Kit

1. Download Agora MediaPlayer Kit from [Agora.io SDK](https://www.agora.io/en/download/).
2. Unzip the downloaded package.
3. Copy the following files from from the **libs** folder of the downloaded SDK package:

Copy from SDK|Copy to Project Folder
---|---
.libs file|**Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/ios** folder
.libs file|**Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/mac** folder
**AgoraRtcEngineKit.framework** folder|**Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/ios or Agora-Extensions/MediaPlayer/helper/apple/RtcChannelPublishPlugin/mac** folder

### Integrate the RtcChannelPublishHelper

1. Download `RtcChannelPublishHelper` from [GitHub/Agora-Extensions](https://github.com/AgoraIO/Agora-Extensions/releases).
2. Unzip the downloaded package.
3. Copy `RtcChannelPublishHelper files` under 2.9.0 to project folder `Project's path`.

### Integrate the Agora Video SDK
```
   https://docs.agora.io/cn/Interactive%20Broadcast/start_live_ios?platform=iOS
```

### Run the Application

Open project with Xcode, connect your iOS device, build and run.

Or use `Archive` to build and run.


## Resources

- You can find full API document at [Document Center](https://docs.agora.io/en/)
- You can file bugs about this sample at [issue](https://github.com/AgoraIO/Agora-Extensions/issues)


## License

The MIT License (MIT)
