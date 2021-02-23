package io.agora.demo.streaming.sdkwrapper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.lang.reflect.Field;

import io.agora.demo.streaming.R;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.AgoraCameraCapturer;
import io.agora.streaming.AudioFrameObserver;
import io.agora.streaming.AudioStreamConfiguration;
import io.agora.streaming.CameraSource;
import io.agora.streaming.SnapshotCallback;
import io.agora.streaming.StreamingContext;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.StreamingKit;
import io.agora.streaming.VideoFilter;
import io.agora.streaming.VideoFrameObserver;
import io.agora.streaming.VideoPreviewRenderer;
import io.agora.streaming.VideoRenderMode;
import io.agora.streaming.VideoStreamConfiguration;
import io.agora.streaming.internal.StreamingKitImpl;

public class StreamingKitWrapper {
  private static final String TAG = StreamingKitWrapper.class.getSimpleName();

  private Context mAppContext;
  private StreamingKit mStreamingKit;
  private StreamingEventHandler mEventHandler;
  private AgoraCameraCapturer mCameraCapturer;
  private VideoPreviewRenderer mPreviewRenderer;
  private StreamingContext streamingContext;
  private boolean mIsCameraFacingFront = true; // StreamingKit uses front camera by default

  public StreamingKitWrapper(Context appContext) {
    mAppContext = appContext.getApplicationContext();
  }

  public void init(@NonNull StreamingEventHandler eventHandler) {
    mEventHandler =  eventHandler;

//    VideoEncoderConfiguration.VideoDimensions videoDimensions =
//        PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(mAppContext)];
    VideoEncoderConfiguration.VideoDimensions videoDimensions = new VideoEncoderConfiguration.VideoDimensions(
            PrefManager.getResolutionWidth(mAppContext),
            PrefManager.getResolutionHeight(mAppContext));


    VideoStreamConfiguration videoStreamConfig = new VideoStreamConfiguration(
        videoDimensions.width, videoDimensions.height,
        PrefManager.VIDEO_FRAMERATES[PrefManager.getVideoFramerateIndex(mAppContext)].getValue(),
        PrefManager.VIDEO_BITRATES[PrefManager.getVideoBitrateIndex(mAppContext)],
        PrefManager.VIDEO_ORIENTATION_MODES[PrefManager.getVideoOrientationModeIndex(mAppContext)],
            PrefManager.getPushStreamMirrorMode(mAppContext));

    AudioStreamConfiguration audioStreamConfig = new AudioStreamConfiguration(
        PrefManager.AUDIO_SAMPLE_RATES[PrefManager.getAudioSampleRateIndex(mAppContext)],
        PrefManager.AUDIO_TYPES[PrefManager.getAudioTypeIndex(mAppContext)],
        PrefManager.AUDIO_BITRATES[PrefManager.getAudioBitrateIndex(mAppContext)]
    );

    streamingContext = new StreamingContext(mEventHandler,
        mAppContext.getString(R.string.private_app_id), mAppContext, videoStreamConfig, audioStreamConfig);

//    int streamType = PrefManager.STREAM_TYPES[PrefManager.getStreamTypeIndex(mAppContext)];
//    if (streamType == PrefManager.StreamType.TYPE_AUDIO_ONLY) {
//        streamingContext.enableVideoStreaming = false;
//    } else if (streamType == PrefManager.StreamType.TYPE_VIDEO_ONLY) {
//        streamingContext.enableAudioStreaming = false;
//    }

    try {
      if (PrefManager.IS_DEV_DEBUG) {
        Field f_lib = StreamingKitImpl.class.getDeclaredField("LIB_NAME");
        f_lib.setAccessible(true);
        f_lib.set(null, "streaming_kit_shared-jni");
      }
      Log.i(TAG, "Streaming Kit SDK version: " + StreamingKit.getSdkVersion());
      mStreamingKit = StreamingKit.create(streamingContext);
      int logFilterIndex = PrefManager.getLogFilterIndex(mAppContext);
      mStreamingKit.setLogFilter(PrefManager.LOG_FILTERS[logFilterIndex]);
      mStreamingKit.setLogFile(PrefManager.getLogPath(mAppContext));
      /*if (PrefManager.IS_DEV_DEBUG) {
        mStreamingKit.setLogFilter(0xFFFFFFFF);
      }*/
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void destroy() {
    Log.d(TAG, "destroy");
    StreamingKit.destroy();
    mStreamingKit = null;
    mEventHandler = null;
  }

  @UiThread
  public void setPreview(SurfaceView view) {
    if (view == null) {
      if (mPreviewRenderer == null) return;
      mPreviewRenderer.setView(null);
    } else {
      Log.i(TAG, "setPreview view: " + view);
      if (mPreviewRenderer == null) {
        mPreviewRenderer = mStreamingKit.getVideoPreviewRenderer();
      }
      mPreviewRenderer.setView(view);
      mPreviewRenderer.setRenderMode(VideoRenderMode.RENDER_MODE_FIT);
      mPreviewRenderer.setMirrorMode(PrefManager.VIDEO_MIRROR_MODES[PrefManager.getLocalViewMirrorIndex(mAppContext)]);
    }
  }

  public StreamingKit impl() {
    return mStreamingKit;
  }

  public void enableAudioRecording(boolean enabled) {
    Log.i(TAG, "enableAudioRecording: " + enabled);
    mStreamingKit.enableAudioRecording(enabled);
  }

  public void enableVideoCapturing(boolean enabled) {
    Log.i(TAG, "enableVideoCapturing: " + enabled);
    mStreamingKit.enableVideoCapturing(enabled);
  }

  public void startStreaming() {
    Log.d(TAG, "startStreaming");
    String rtmpUrl = PrefManager.getRtmpUrl(mAppContext);
    Log.i(TAG, "startStreaming url: " + rtmpUrl);
    mStreamingKit.startStreaming(rtmpUrl);
  }

  public void stopStreaming() {
    Log.i(TAG, "stopStreaming");
    mStreamingKit.stopStreaming();
  }

  public void muteAudioStream(boolean muted) {
    Log.i(TAG, "muteAudioStream: " + muted);
    mStreamingKit.muteAudioStream(muted);
  }

  public void muteVideoStream(boolean muted) {
    Log.i(TAG, "muteVideoStream: " + muted);
    mStreamingKit.muteVideoStream(muted);
  }

  public int switchCamera() {
    Log.i(TAG, "switchCamera");
    int ret = mStreamingKit.switchCamera();
    if (ret == 0) {
      mIsCameraFacingFront = !mIsCameraFacingFront;
    }
    return ret;
  }

  public int switchCameraSource() {
    Log.i(TAG, "switchCameraSource");
    if (mCameraCapturer == null) {
      mCameraCapturer = mStreamingKit.getCameraCapture();
      if (mCameraCapturer == null) {
        Log.e(TAG, "switchCameraSource: failed to get camera capturer");
        return -1;
      }
    }

    int current = mCameraCapturer.getCameraSource();
    int ret;
    if (current == CameraSource.CAMERA_FRONT) {
      ret = mCameraCapturer.setCameraSource(CameraSource.CAMERA_BACK);
    } else {
      ret = mCameraCapturer.setCameraSource(CameraSource.CAMERA_FRONT);
    }
    mIsCameraFacingFront = (mCameraCapturer.getCameraSource() == CameraSource.CAMERA_FRONT);
    return ret;
  }

  public int switchResolution(int width, int height) {
    Log.i(TAG, "switchResolution");

    return mStreamingKit.switchResolution(width, height);
  }

  public int snapshot(SnapshotCallback callback) {
    Log.i(TAG, "switchResolution");

    return mStreamingKit.snapshot(callback);
  }

  public boolean isCameraFacingFront() {
    return mIsCameraFacingFront;
  }

  public int registerAudioFrameObserver(AudioFrameObserver observer) {
    Log.i(TAG, "registerAudioFrameObserver: " + observer);
    return mStreamingKit.registerAudioFrameObserver(observer);
  }

  public void unregisterAudioFrameObserver(AudioFrameObserver observer) {
    Log.i(TAG, "unregisterAudioFrameObserver: " + observer);
    mStreamingKit.unregisterAudioFrameObserver(observer);
  }

  public int registerVideoFrameObserver(VideoFrameObserver observer) {
    Log.i(TAG, "registerVideoFrameObserver: " + observer);
    return mStreamingKit.registerVideoFrameObserver(observer);
  }

  public void unregisterVideoFrameObserver(VideoFrameObserver observer) {
    Log.i(TAG, "unregisterVideoFrameObserver: " + observer);
    mStreamingKit.unregisterVideoFrameObserver(observer);
  }

  public boolean addVideoFilter(VideoFilter videoFilter) {
    Log.i(TAG, "addVideoFilter: " + videoFilter);
    return mStreamingKit.addVideoFilter(videoFilter);
  }

  public boolean removeVideoFilter(VideoFilter videoFilter) {
    Log.i(TAG, "removeVideoFilter: " + videoFilter);
    return mStreamingKit.removeVideoFilter(videoFilter);
  }

  public synchronized AgoraCameraCapturer getCameraCapturer() {
    if (mCameraCapturer == null) {
      mCameraCapturer = mStreamingKit.getCameraCapture();
      if (mCameraCapturer == null) {
        Log.e(TAG, "failed to get camera capturer");
        return null;
      }
    }
    return mCameraCapturer;
  }

  public int setZoom(float zoomValue){
    Log.d(TAG, "setZoom value: " + zoomValue);
    return mCameraCapturer.setZoom(zoomValue);
  }

  public float getMaxZoom(){
    float maxZoomValue = mCameraCapturer.getMaxZoom();
    Log.d(TAG, "max zoom value: " + maxZoomValue);
    return maxZoomValue;
  }

  public boolean isFocusSupported(){
    Log.d(TAG, "is focus support");
    return mCameraCapturer.isFocusSupported();
  }

  public int setFocus(float x, float y){
    Log.d(TAG, "set focus, x = " + x + ", y = " + y);
    return mCameraCapturer.setFocus(x, y);
  }

  public boolean isAutoFaceFocusSupported(){
    Log.d(TAG, "is auto face focus support");
    return mCameraCapturer.isAutoFaceFocusSupported();
  }

  public int setAutoFaceFocus(boolean enable){
    Log.d(TAG, "set auto face focus: " + enable);
    return mCameraCapturer.setAutoFaceFocus(enable);
  }

  public int startScreenCapture(Intent intent, int width, int height){
    return mStreamingKit.startScreenCapture(intent, width, height);
  }

  public void stopScreenCapture(){
    mStreamingKit.stopScreenCapture();
  }

  public static String getSdkVersion(){
    if (PrefManager.IS_DEV_DEBUG) {
      Field f_lib = null;
      try {
        f_lib = StreamingKitImpl.class.getDeclaredField("LIB_NAME");
        f_lib.setAccessible(true);
        f_lib.set(null, "streaming_kit_shared-jni");
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    return StreamingKit.getSdkVersion();
  }


}
