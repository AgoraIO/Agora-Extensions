package io.agora.demo.streaming;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.AudioFrameObserver;
import io.agora.streaming.AudioStreamConfiguration;
import io.agora.streaming.StreamingContext;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.StreamingKit;
import io.agora.streaming.VideoFilter;
import io.agora.streaming.VideoFrameObserver;
import io.agora.streaming.VideoPreviewRenderer;
import io.agora.streaming.VideoRenderMode;
import io.agora.streaming.VideoStreamConfiguration;

public class StreamingKitWrapper {
  private static final String TAG = StreamingKitWrapper.class.getSimpleName();

  private Context mAppContext;
  private StreamingKit mStreamingKit;
  private StreamingEventHandler mEventHandler;
  private VideoPreviewRenderer mPreviewRenderer;
  private boolean mIsCameraFacingFront = true; // StreamingKit uses front camera by default

  public StreamingKitWrapper(Context appContext) {
     mAppContext = appContext.getApplicationContext();
  }

  public void init(@NonNull StreamingEventHandler eventHandler) {
    mEventHandler =  eventHandler;

    VideoEncoderConfiguration.VideoDimensions videoDimensions =
        PrefManager.getVideoDimensions();

    VideoStreamConfiguration videoStreamConfig = new VideoStreamConfiguration(
        videoDimensions.width, videoDimensions.height,
        PrefManager.getVideoFramerate(),
        PrefManager.getVideoBitrate(),
        PrefManager.getVideoOrientationMode());

    AudioStreamConfiguration audioStreamConfig = new AudioStreamConfiguration(
        PrefManager.getAudioSampleRate(),
        PrefManager.getAudioType(),
        PrefManager.getAudioBitrate()
    );

    StreamingContext streamingContext = new StreamingContext(mEventHandler,
        PrefManager.getAppID(), mAppContext, videoStreamConfig, audioStreamConfig);

    try {
      mStreamingKit = StreamingKit.create(streamingContext);
      mStreamingKit.setLogFilter(PrefManager.getLogFilter());
      mStreamingKit.setLogFile(PrefManager.getLogPath());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void destroy() {
    StreamingKit.destroy();
    mStreamingKit = null;
    mEventHandler = null;
  }

  @UiThread
  public void setPreview(SurfaceView view) {
    Log.i(TAG, "setPreview view: " + view);
    if (view == null) {
      if (mPreviewRenderer == null) return;
      mPreviewRenderer.setView(null);
    } else {
      if (mPreviewRenderer == null) {
        mPreviewRenderer = mStreamingKit.getVideoPreviewRenderer();
      }
      mPreviewRenderer.setView(view);
      mPreviewRenderer.setRenderMode(VideoRenderMode.RENDER_MODE_HIDDEN);
      mPreviewRenderer.setMirrorMode(PrefManager.getMirrorModeLocal());
    }
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
    String rtmpUrl = PrefManager.getRtmpUrl();
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
}