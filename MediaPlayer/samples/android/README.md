
READ THIS IN OTHER LANGUAGES [🇨🇳](README.zh.md). Contributions welcome!

# AgoraPlayer_Quickstart

This tutorial enables you to quickly get started in your development efforts to create an Android app with media player provided by [Agora.io](https://www.agora.io/en/). With [Agora MediaPlayer Kit](https://download.agora.io/sdk/release/Agora_Media_Player_for_Android_rel.v1.1.0.91_20200228_2154.zip?_ga=2.22269240.208564419.1583061888-277459906.1543911509) you can play most popular multimedia files and various streaming protocols. Besides that, you can publish video/audio stream playing to Agora RTC channel with Agora RTC SDK(`RtcChannelPublishHelper` is just what you need)

With this sample app, you can:

- Play multimedia files or streaming progocols.
- Play multi at the same time
- Control the playback(pause/resume/mute/volume-control and etc.).
- Publish to Agora RTC channel.


## Prerequisites

- Android Studio 3.3 or above
- Android device (e.g. Nexus 5X). A real device is recommended because some simulators have missing functionality or lack the performance necessary to run the sample.


## Quick Start

This section shows you how to prepare, build, and run the sample application.

### Obtain an App ID

To build and run the sample application, get an App ID:

1. Create a developer account at [agora.io](https://console.agora.io/signin/). Once you finish the signup process, you will be redirected to the Console.
2. Navigate in the Console tree on the left to **Projects** > **Project List**.
3. Save the **App ID** from the Console for later use.
4. Locate the file **app/src/main/res/values/strings.xml** and replace <#YOUR APP ID#> with the App ID in the console.

  ```
  <string name="agora_app_id"><#YOUR APP ID#></string>
  ```

### Integrate the Agora MediaPlayer Kit

1. Download Agora MediaPlayer Kit from [Agora.io SDK](https://www.agora.io/en/download/).
2. Unzip the downloaded package.
3. Copy the following files from from the **libs** folder of the downloaded SDK package:

Copy from SDK|Copy to Project Folder
---|---
.jar file|**/apps/lib** folder
**arm64-v8a** folder|**/app/src/main/jniLibs** folder
**x86** folder|**/app/src/main/jniLibs** folder
**armeabi-v7a** folder|**/app/src/main/jniLibs** folder

### Integrate the RtcChannelPublishHelper

1. Download `RtcChannelPublishHelper` from [GitHub/Agora-Extensions](https://github.com/AgoraIO/Agora-Extensions/releases).
2. Unzip the downloaded package.
3. Copy `RtcChannelPublishHelper.aar` under 2.9.0 to project folder `apps/lib`.

### Integrate the Agora Video SDK

The SDK must be integrated into the sample project before it can opened and built. There are two methods for integrating the Agora Video SDK into the sample project. The first method uses JCenter to automatically integrate the SDK files. The second method requires you to manually copy the SDK files to the project.

#### Method 1 - Integrate the SDK Automatically Using JCenter (Recommended)

1. Clone this repository.
2. Open **app/build.gradle** and add the following line to the `dependencies` list:

  ```
  ...
  dependencies {
      ...
      implementation 'io.agora.rtc:full-sdk:2.9.0'
  }
  ```

#### Method 2 - Manually copy the SDK files

1. Download the Agora Video SDK from [Agora.io SDK](https://www.agora.io/en/download/).
2. Unzip the downloaded SDK package.
3. Copy the following files from from the **libs** folder of the downloaded SDK package:

Copy from SDK|Copy to Project Folder
---|---
.jar file|**/apps/libs** folder
**arm64-v8a** folder|**/app/src/main/jniLibs** folder
**x86** folder|**/app/src/main/jniLibs** folder
**armeabi-v7a** folder|**/app/src/main/jniLibs** folder

### Run the Application

Open project with Android Studio, connect your Android device, build and run.

Or use `Gradle` to build and run.


## Resources

- You can find full API document at [Document Center](https://docs.agora.io/en/)
- You can file bugs about this sample at [issue](https://github.com/AgoraIO/Agora-Extensions/issues)


## License

The MIT License (MIT)
