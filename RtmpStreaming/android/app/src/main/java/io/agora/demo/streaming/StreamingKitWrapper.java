package io.agora.demo.streaming;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.lang.reflect.Field;

import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.AudioFrameObserver;
import io.agora.streaming.AudioStreamConfiguration;
import io.agora.streaming.VideoFrameObserver;
import io.agora.streaming.VideoRenderMode;
import io.agora.streaming.StreamingContext;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.StreamingKit;
import io.agora.streaming.VideoFilter;
import io.agora.streaming.VideoPreviewRenderer;
import io.agora.streaming.VideoStreamConfiguration;
import io.agora.streaming.internal.StreamingKitImpl;

public class StreamingKitWrapper {
  private static final String TAG = StreamingKitWrapper.class.getSimpleName();

  private Context mAppContext;
  private Handler mWorkHandler;
  private StreamingKit mStreamingKit;
  private StreamingEventHandler mEventHandler;
  private VideoPreviewRenderer mPreviewRenderer;
  private boolean mIsCameraFacingFront = true; // StreamingKit uses front camera by default

  public StreamingKitWrapper(Context appContext, @NonNull Handler handler) {
    mAppContext = appContext.getApplicationContext();
    mWorkHandler = handler;
  }

  public void init(@NonNull StreamingEventHandler eventHandler) {
    mEventHandler =  eventHandler;

    VideoEncoderConfiguration.VideoDimensions videoDimensions =
        PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(mAppContext)];

    VideoStreamConfiguration videoStreamConfig = new VideoStreamConfiguration(
        videoDimensions.width, videoDimensions.height,
        PrefManager.VIDEO_FRAMERATES[PrefManager.getVideoFramerateIndex(mAppContext)].getValue(),
        PrefManager.VIDEO_BITRATES[PrefManager.getVideoBitrateIndex(mAppContext)],
        PrefManager.VIDEO_ORIENTATION_MODES[PrefManager.getVideoOrientationModeIndex(mAppContext)]);

    AudioStreamConfiguration audioStreamConfig = new AudioStreamConfiguration(
        PrefManager.AUDIO_SAMPLE_RATES[PrefManager.getAudioSampleRateIndex(mAppContext)],
        PrefManager.AUDIO_TYPES[PrefManager.getAudioTypeIndex(mAppContext)],
        PrefManager.AUDIO_BITRATES[PrefManager.getAudioBitrateIndex(mAppContext)]
    );

    StreamingContext streamingContext = new StreamingContext(mEventHandler,
        mAppContext.getString(R.string.private_app_id), mAppContext, videoStreamConfig, audioStreamConfig);

    try {
      if (PrefManager.IS_DEV_DEBUG) {
        Field f_lib = StreamingKitImpl.class.getDeclaredField("LIB_NAME");
        f_lib.setAccessible(true);
        f_lib.set(null, "streaming_kit_shared-jni");
      }
      mStreamingKit = StreamingKit.create(streamingContext);
      mStreamingKit.setLogFilter(PrefManager.LOG_FILTERS[PrefManager.getLogFilterIndex(mAppContext)]);
      mStreamingKit.setLogFile(PrefManager.getLogPath(mAppContext));
      if (PrefManager.IS_DEV_DEBUG) {
        mStreamingKit.setLogFilter(0xFFFFFFFF);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deinit() {
    mWorkHandler.post(new Runnable() {
      @Override
      public void run() {
        StreamingKit.destroy();
      }
    });
    mStreamingKit = null;
    mEventHandler = null;
  }

  @UiThread
  public void setPreviewRenderer(SurfaceView view) {
    Log.i(TAG, "setPreviewRenderer view: " + view);
    if (view == null) {
      if (mPreviewRenderer == null) return;
      mPreviewRenderer.setView(null);
    } else {
      if (mPreviewRenderer == null) {
        mPreviewRenderer = mStreamingKit.getVideoPreviewRenderer();
      }
      mPreviewRenderer.setView(view);
      mPreviewRenderer.setRenderMode(VideoRenderMode.RENDER_MODE_HIDDEN);
      mPreviewRenderer.setMirrorMode(PrefManager.VIDEO_MIRROR_MODES[PrefManager.getMirrorLocalIndex(mAppContext)]);
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