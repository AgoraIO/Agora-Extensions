package io.agora.demo.streaming.sdkwrapper;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.agora.base.AudioFrame;
import io.agora.base.internal.video.RendererCommon;
import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.YuvHelper;
import io.agora.demo.streaming.R;
import io.agora.demo.streaming.utils.FileUtil;
import io.agora.demo.streaming.utils.PrefManager;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.streaming.AudioFrameObserver;
import io.agora.streaming.VideoFrameObserver;

public class RtcEngineWrapper implements AudioFrameObserver, VideoFrameObserver {
  private static final String TAG = RtcEngineWrapper.class.getSimpleName();

  private Context mAppContext;
  private RtcEngine mRtcEngine;
  private MyVideoSource mVideoSource = new MyVideoSource();

  private final String mPublishUrl;
  private boolean mMuteLocalAudio = false;
  private boolean mMuteLocalVideo = false;

  public RtcEngineWrapper(Context context) {
    mAppContext = context.getApplicationContext();
    mPublishUrl = PrefManager.getRtmpUrl(context) + (PrefManager.IS_SIMUL_TEST ? "1" : "");
  }

  public void create(IRtcEngineEventHandler rtcEventHandler) {
    try {
      mRtcEngine = RtcEngine.create(mAppContext, mAppContext.getString(R.string.private_app_id),
          rtcEventHandler);
      mRtcEngine.setLogFile(FileUtil.getLogFilePath(mAppContext, "agora-rtc.log"));
      if (PrefManager.IS_DEV_DEBUG) {
        mRtcEngine.setParameters("{\"rtc.log_filter\": 65535}");
      }
      mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
      mRtcEngine.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
      mRtcEngine.enableVideo();
    } catch (Exception e) {
      e.printStackTrace();
    }
    configVideo();
  }

  public void destroy() {
    RtcEngine.destroy();
    mRtcEngine = null;
  }

  public void setExternalAudioSource(boolean enabled) {
    Log.i(TAG, "setExternalAudioSource enabled: " + enabled);
    if (enabled) {
      // audio sample rate of audio data callback from Streaming Kit is fixed at 44.1KHz
      final int sampleRate = 44100;
      final int channels = PrefManager.getAudioType(mAppContext);
      mRtcEngine.setExternalAudioSource(true, sampleRate, channels);
    } else {
      mRtcEngine.setExternalAudioSource(false, 0, 0);
    }
  }

  public void setExternalVideoSource(boolean enabled) {
    Log.i(TAG, "setVideoSource enabled: " + enabled);
    if (enabled) {
      mRtcEngine.setVideoSource(mVideoSource);
    } else {
      mRtcEngine.setVideoSource(null);
    }
  }

  public void muteLocalAudioStream(boolean muted) {
    Log.i(TAG, "muteLocalAudioStream muted: " + muted);
    mMuteLocalAudio = muted;
    mRtcEngine.muteLocalAudioStream(muted);
  }

  public void muteLocalVideoStream(boolean muted) {
    Log.i(TAG, "muteLocalVideoStream muted: " + muted);
    mMuteLocalVideo = muted;
    mRtcEngine.muteLocalVideoStream(muted);
  }

  public void setupRemoteVideo(VideoCanvas videoCanvas) {
    Log.i(TAG, "setupRemoteVideo view: " + videoCanvas.view + " renderMode: "
        + videoCanvas.renderMode + " uid: " + videoCanvas.uid + " mirrorMode: "
        + videoCanvas.mirrorMode);
    mRtcEngine.setupRemoteVideo(videoCanvas);
  }

  public void joinChannel(String channelName) {
    Log.i(TAG, "joinChannel: " + channelName);

    // ensure audio is muted as-is
    if (mMuteLocalAudio) {
      mRtcEngine.muteLocalAudioStream(true);
    }

    // ensure video is muted as-is
    if (mMuteLocalVideo) {
      mRtcEngine.muteLocalVideoStream(true);
    }

    // acoustic echo cancellation
    mRtcEngine.setParameters("{\"che.audio.external.to.apm\":true}");

    mRtcEngine.joinChannel(null, channelName, "", 0);
  }

  public void leaveChannel() {
    Log.i(TAG, "leaveChannel");
    mRtcEngine.leaveChannel();
  }

  public int setLiveTranscoding(List<Integer> uidList, int width, int height, int videoBitrate,
      int videoFramerate) {
    Log.i(TAG, "setLiveTranscoding");
    LiveTranscoding liveTranscoding = new LiveTranscoding();
    liveTranscoding.width = width;
    liveTranscoding.height = height;
    liveTranscoding.videoBitrate = videoBitrate;
    liveTranscoding.videoFramerate = videoFramerate;
    ArrayList<LiveTranscoding.TranscodingUser> transcodingUsers = cdnLayout(uidList, width, height);
    liveTranscoding.setUsers(transcodingUsers);
    return mRtcEngine.setLiveTranscoding(liveTranscoding);
  }

  private static ArrayList<LiveTranscoding.TranscodingUser> cdnLayout(List<Integer> uidList,
      int canvasWidth, int canvasHeight) {
    final int viewWidth =
        (uidList.size() <= 1) ? canvasWidth : (canvasWidth / 2);
    final int viewHeight =
        (uidList.size() <= 2) ? canvasHeight : (canvasHeight / ((uidList.size() - 1) / 2 + 1));

    ArrayList<LiveTranscoding.TranscodingUser> users = new ArrayList<>(uidList.size());
    for (int index = 0; index < uidList.size(); index++) {
      int uid = uidList.get(index);
      LiveTranscoding.TranscodingUser tmpUser = new LiveTranscoding.TranscodingUser();
      tmpUser.uid = uid;
      tmpUser.x = (index % 2) * viewWidth;
      tmpUser.y = (index >> 1) * viewHeight;
      tmpUser.width = viewWidth;
      tmpUser.height = viewHeight;
      tmpUser.zOrder = index;
      tmpUser.audioChannel = 0;
      tmpUser.alpha = 1f;
      users.add(tmpUser);
    }
    return users;
  }

  public int addPublishStreamUrl() {
    Log.i(TAG, "addPublishStreamUrl: " + mPublishUrl);
    return mRtcEngine.addPublishStreamUrl(mPublishUrl, false);
  }

  public void removePublishStreamUrl() {
    Log.i(TAG, "removePublishStreamUrl: " + mPublishUrl);
    mRtcEngine.removePublishStreamUrl(mPublishUrl);
  }

  private void configVideo() {
    Log.i(TAG, "configVideo");
    VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
//        PrefManager.VIDEO_DIMENSIONS[PrefManager.getVideoDimensionsIndex(mAppContext)],
        new VideoEncoderConfiguration.VideoDimensions(
                                    PrefManager.getResolutionWidth(mAppContext),
                                    PrefManager.getResolutionHeight(mAppContext)),
        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
        VideoEncoderConfiguration.STANDARD_BITRATE,
        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
    );
    mRtcEngine.setVideoEncoderConfiguration(configuration);
  }

  @Override
  public void onAudioFrame(AudioFrame audioFrame) {
    mRtcEngine.pushExternalAudioFrame(audioFrame.bytes, audioFrame.timestamp);
  }

  /**
   * io.agora.streaming.VideoFrameObserver
   */
  @Override
  public void onVideoFrame(VideoFrame videoFrame) {
    mVideoSource.onVideoFrame(videoFrame);
  }

  static class MyVideoSource implements IVideoSource {
    static final String TAG = MyVideoSource.class.getSimpleName();
    IVideoFrameConsumer mVideoFrameConsumer;
    TextureBufferHelper mTextureBufferHelper;
    ByteBuffer mI420ByteBuffer = null;
    int mI420BufferWidth = 0;
    int mI420BufferHeight = 0;

    public void onVideoFrame(VideoFrame videoFrame) {
      if (mVideoFrameConsumer == null) {
        return;
      }
      VideoFrame.Buffer buffer = videoFrame.getBuffer();
      if (!(buffer instanceof VideoFrame.TextureBuffer)) {
        return;
      }
      VideoFrame.TextureBuffer textureBuffer = (VideoFrame.TextureBuffer) buffer;

      int rotation = videoFrame.getRotation();
      long timestampMs = videoFrame.getTimestampNs() / 1000000;
      consumeTextureBuffer(textureBuffer, rotation, timestampMs);
    }

    private void consumeTextureBuffer(final VideoFrame.TextureBuffer buffer, final int rotation,
        final long timestampMs) {
      final int format = buffer.getType() == VideoFrame.TextureBuffer.Type.OES ?
          AgoraVideoFrame.FORMAT_TEXTURE_OES : AgoraVideoFrame.FORMAT_TEXTURE_2D;
      final float[] matrix4x4 = RendererCommon.convertMatrixFromAndroidGraphicsMatrix(
          buffer.getTransformMatrix());

      if (mTextureBufferHelper == null) {
        mTextureBufferHelper = TextureBufferHelper.create("rtc-texture-consumer",
            buffer.getEglBaseContext());
        if (mTextureBufferHelper == null) {
          Log.e(TAG, "Failed to create texture buffer helper!");
          return;
        }
      }

      mTextureBufferHelper.invoke(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          mVideoFrameConsumer.consumeTextureFrame(buffer.getTextureId(), format, buffer.getWidth(),
              buffer.getHeight(), rotation, timestampMs, matrix4x4);
          return null;
        }
      });
    }

    private void consumeI420Buffer(VideoFrame.I420Buffer buffer, int rotation, long timestampMs) {
      // Allocates a new direct byte buffer if not yet, or resolution changed.
      if (mI420ByteBuffer == null || buffer.getWidth() != mI420BufferWidth
          || buffer.getHeight() != mI420BufferHeight) {
        mI420BufferWidth = buffer.getWidth();
        mI420BufferHeight = buffer.getHeight();
        final int chromaWidth = (mI420BufferWidth + 1) / 2;
        final int chromaHeight = (mI420BufferHeight + 1) / 2;
        final int capacity = mI420BufferWidth * mI420BufferHeight + chromaWidth * chromaHeight * 2;
        mI420ByteBuffer = ByteBuffer.allocateDirect(capacity);
      }

      // Copies I420 to a tightly packed destination buffer.
      YuvHelper.I420Copy(buffer.getDataY(), buffer.getStrideY(), buffer.getDataU(),
          buffer.getStrideU(), buffer.getDataV(), buffer.getStrideV(), mI420ByteBuffer, mI420BufferWidth, mI420BufferHeight);

      // Pushes the video frame data to RTC SDK
      mVideoFrameConsumer.consumeByteBufferFrame(mI420ByteBuffer, AgoraVideoFrame.FORMAT_I420, mI420BufferWidth, buffer.getHeight(), rotation, timestampMs);
    }

    @Override
    public boolean onInitialize(IVideoFrameConsumer consumer) {
      Log.i(TAG, "onInitialize consumer: " + consumer);
      mVideoFrameConsumer = consumer;
      return true;
    }

    @Override
    public boolean onStart() {
      Log.i(TAG, "onStart");
      return true;
    }

    @Override
    public void onStop() {
      Log.i(TAG, "onStop");
      mVideoFrameConsumer = null;
    }

    @Override
    public void onDispose() {
      Log.i(TAG, "onDispose");
      if (mTextureBufferHelper != null) {
        mTextureBufferHelper.dispose();
        mTextureBufferHelper = null;
      }
      mVideoFrameConsumer = null;
    }

    @Override
    public int getBufferType() {
      return MediaIO.BufferType.TEXTURE.intValue();
    }

    @Override
    public int getCaptureType() {
      return 0;
    }

    @Override
    public int getContentHint() {
      return 0;
    }
  }

  /*
  public boolean isCameraZoomSupported(){
    Log.d(TAG, "is Camera zoom support");
    return mRtcEngine.isCameraZoomSupported();
  }

  public boolean isCameraFocusSupported(){
    Log.d(TAG, "is Camera focus support");
    return mRtcEngine.isCameraFocusSupported();
  }

  public int setCameraZoomFactor(float factor){
    Log.d(TAG, "set camera zoom factor: " + factor);
    return mRtcEngine.setCameraZoomFactor(factor);
  }

  public float getCameraMaxZoomFactor(){
    float maxFactor;

    maxFactor = mRtcEngine.getCameraMaxZoomFactor();
    Log.d(TAG, "get camera max factor: " + maxFactor);
    return maxFactor;
  }

  public int setCameraFocusPositionInPreview(int positionX, int positionY){
    Log.d(TAG, "set focus positionX: " + positionX + " focus positionY: " + positionY);
    return mRtcEngine.setCameraFocusPositionInPreview(positionX, positionY);
  }
   */
}
