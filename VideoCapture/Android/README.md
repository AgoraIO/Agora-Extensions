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
videoManager.setLocalPreviewMirror(Constant.MIRROR_MODE_ENABLED);
```
* Basic camera operations
```java
videoManager.startCapture();
videoManager.switchCamera();

// Calling stopCapture() will release the camera device
videoManager.stopCapture();
```

### Structures

#### Threading

When a CameraVideoManager is created, a camera thread is provided for lifecycle callbacks and image processing.

The thread maintains an OpenGL context and will be available as long as the thread is alive. Once created, the thread is waken up when starting to capture and goes to sleep when the capture is stopped until the thread quits.

In general, the resources allocated for this thread will be reused unless it is certain that the camera capture is no longer needed. CameraVideoManager objects do not support to destroy the thread directly. If needed, you can call the following code to quit camera thread.

```java
// Stop the camera channel
VideoModule.instance().stopChannel(ChannelManager.ChannelID.CAMERA);

// or you can choose to stop all channels.
VideoModule.instance().stopAllChannels();
```

The video channel (or channel for short) is the implementation of the camera thread. Once the camera channel is stopped, the previous CameraVideoManager instance will not be valid any more, you should recreate it.

#### Local previews

The library is designed to support flexible local preview strategies. For example, you can set as many local previews as you like (of course, if the device's performance can cover), and they can be added or removed whenever wanted.

Only SurfaceView or TextureView can be set to be previews, but it is enough for most cases. They can be used no matter whether they have already been attached to the window hierarchy or not, only if the application's logic allows that.

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

The previews will be removed automatically if they are detached from the view system, without affecting the rest of other surfaces.