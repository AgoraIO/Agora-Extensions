## VideoCapture

Android VideoCapture is a helper library for using Android system cameras easily. It helps to choose the right camera API and maintains OpenGL context.

### QuickStart

* Create a camera manager
```java
// The second parameter is a preprocessor for image
// pre processing, pass null if not used.
CameraVideoManager videoManager = new CameraVideoManager(this, null);
```

* Set camera parameters
```java
// Captured picture size, frame rate and facing should be set before the capture started.
// Facing is not needed when switching cameras.
videoManager.setPictureSize(640, 480);
videoManager.setFrameRate(24);
videoManager.setFacing(Constant.CAMERA_FACING_FRONT);

SurfaceView surfaceView = new SurfaceView(this);
videoManager.setLocalPreview(surfaceView);

// The mirror mode is MIRROR_MODE_AUTO by default
videoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);
```
* Basic camera operations
```java
videoManager.startCapture();
videoManager.switchCamera();

// Calling stopCapture() will release the camera device
videoManager.stopCapture();
```

### How it works

#### Threading

When a `CameraVideoManager` is created, a camera thread is provided for lifecycle callbacks and image processing.

The thread maintains an OpenGL context and will be available as long as the thread is alive. Once created, the thread is waken up when starting to capture and goes to sleep when the capture is stopped until the thread quits.

In general, the resources allocated for this thread will be reused unless it is certain that the camera capture is no longer needed. `CameraVideoManager` objects do not support to destroy the thread directly. If needed, you can call the following code to quit camera thread.

```java
// Stop the camera channel
VideoModule.instance().stopChannel(ChannelManager.ChannelID.CAMERA);

// or you can choose to stop all channels.
VideoModule.instance().stopAllChannels();
```

The video channel (or channel for short) is the implementation of the camera thread. Once the camera channel is stopped, the previous `CameraVideoManager` instance will not be valid any more, you should recreate it.

#### Local previews

The library is designed to support flexible local preview strategies. For example, you can set as many local previews as you like (of course, if the device's performance can cover), and they can be added or removed whenever wanted.

Only `SurfaceView` or `TextureView` can be set to be previews, but it is enough for most cases. They can be used no matter whether they have already been attached to the window hierarchy or not, only if the application's logic allows that.

Local previews can be replaced, it may be useful when there are interactions between different surfaces.

```java
// Set multiple local previews
SurfaceView surfaceView = new SurfaceView(this);
videoManager.setLocalPreview(surfaceView);

TextureView textureView = new TextureView(this);
videoManager.setLocalPreview(textureView);

// Repeatedly setting the same surface object affects nothing
videoManager.setLocalPreview(surfaceView);
```

```java
// If a preview surface is given an identifier, it can be replaced.
// Even the old surface still stays in the view hierarchy, the 
// preview content will not be drawn onto the old surface any more. 
SurfaceView surfaceView = new SurfaceView(this);
videoManager.setLocalPreview(surfaceView, "User1");

// Create a TextureView to replace the old SurfaceView with the same identifier
TextureView textureView = new TextureView(this);
videoManager.setLocalPreview(textureView, "User1");
```

The previews will be removed automatically if they are detached from the view system, without affecting the other surfaces.

#### Mirror Mode

For now the mirror mode only works for local previews. The definition of mirroring is consistent with the system camera. By default, images from front-facing system camera is mirrored and it looks like seeing into a mirror; images from the back-facing camera is not mirrored.

```java
// If want to use system camera setting, use MIRROR_MODE_AUTO
videoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_AUTO);

// If want to set mirror mode for both front and back facing cameras
videoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_ENABLED);
```

Note: currently the mirror mode is not reset when switching cameras.

#### Start / Stop Capture

The `startCapture` / `stopCapture` APIs are independent of any Android life cycles. Users should design their own way to control the camera behavior.

The `tartCapture` method opens a camera and starts the camera preview, while `stopCapture` stops preview and release the camera resource.

#### Pre processor

The mechanism `IPreprocessor` gives users a way to interrupt and do some pre-processing before the images are rendered. A video channel only supported one pre-processor and it cannot be reset unless the channel is recreated.

The pre-processor should be initialized at the starting phase of any video channel, and then users can obtain the `IPreprocessor` instance and cast to whatever class they have actually implemented.

```java
// Customize a pre processor by implementing the IPreprocessor interface
class AgoraPreprocessor implements IPreprocessor {

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        return null;
    }

    @Override
    public void initPreprocessor() {

    }

    @Override
    public void enablePreProcess(boolean enabled) {

    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {

    }
}

// And you can create an object and then pass to the CameraVideoManager constructor
AgoraPreprocessor agoraProcessor = new AgoraPreprocessor();
CameraVideoManager videoManager = new CameraVideoManager(this, agoraProcessor); 

// And you can obtain the pre-processor instance like this way
AgoraPreprocessor preProcessor = (AgoraPreprocessor) videoManager.getPreprocessor();
```

Method `initPreprocessor()` is called when the video channel is initialized. When a frame capture is completed, it will be passed into `onPreProcessFrame()` in the format of a VideoCaptureFrame along with some important information in it.

Also, because the video channel is actually an OpenGL thread, a channel context is given for processing images and also releasing the pre-processor.

The image processing can be paused at any time using `enablePreProcess()`, but what is actually doing when it is paused / resumed should be implemented according to the users' needs.