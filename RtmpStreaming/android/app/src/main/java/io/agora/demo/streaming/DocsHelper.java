package io.agora.demo.streaming;

import android.content.Context;
import android.view.SurfaceView;

import java.util.ArrayList;

import io.agora.base.AudioFrame;
import io.agora.base.VideoFrame;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.video.AgoraImage;
import io.agora.streaming.AudioFrameObserver;
import io.agora.streaming.StreamingContext;
import io.agora.streaming.StreamingEventHandler;
import io.agora.streaming.StreamingKit;
import io.agora.streaming.VideoFilter;
import io.agora.streaming.VideoFrameObserver;
import io.agora.streaming.VideoMirrorMode;
import io.agora.streaming.VideoPreviewRenderer;
import io.agora.streaming.VideoRenderMode;
import io.agora.streaming.VideoStreamConfiguration;

public class DocsHelper {
  private RtcEngine rtcEngine;
  private StreamingKit streamingKit;
  private VideoPreviewRenderer previewRenderer;

  private String appId = "";
  private String publishUrl = "";
  public int width = 360;
  public int height = 640;
  public int videoBitrate = 400;
  public LiveTranscoding.VideoCodecProfileType videoCodecProfile = LiveTranscoding.VideoCodecProfileType.HIGH;
  public int videoGop = 30;
  public int videoFramerate = 15;
  public AgoraImage watermark = new AgoraImage();
  public AgoraImage backgroundImage = new AgoraImage();

  public int audioSampleRate = LiveTranscoding.AudioSampleRateType.getValue(LiveTranscoding.AudioSampleRateType.TYPE_44100);
  public int audioBitrate = 48;
  public int audioChannels = 1;
  public LiveTranscoding.AudioCodecProfileType audioCodecProfile = LiveTranscoding.AudioCodecProfileType.LC_AAC;

  // 创建 Streaming Kit
  public void createStreamingKit(Context appContext) {
    // 视频参数设置
    VideoStreamConfiguration videoStreamConfig = new VideoStreamConfiguration(
        VideoStreamConfiguration.VD_640x360,
        VideoStreamConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
        VideoStreamConfiguration.STANDARD_BITRATE,
        VideoStreamConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT);

    StreamingContext streamingContext = new StreamingContext(
        streamingEventHandler, appId, appContext, videoStreamConfig);
    try {
      streamingKit = StreamingKit.create(streamingContext);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 设置日志
  public void setupLog(String filePath) {
    // 设置日志文件路径，默认为 /sdcard/Android/data/<packageName>/files/streaming-kit.log
    streamingKit.setLogFile(filePath);

    // 设置日志文件大小，默认为 512KB
    streamingKit.setLogFileSize(512);

    // 设置日志过滤级别，默认为 INFO
    streamingKit.setLogFilter(StreamingKit.LogFilter.LOG_FILTER_INFO);
  }

  // 设置本地预览
  public void setupPreview(SurfaceView view) {
    // 获取 preview renderer 对象用于管理所有与渲染相关的操作
    if (previewRenderer == null) {
      previewRenderer = streamingKit.getVideoPreviewRenderer();
    }

    // 设置本地预览视图
    previewRenderer.setView(view);

    // 设置本地预览镜像模式，默认前置摄像头开启镜像，后置摄像头不开启镜像
    previewRenderer.setMirrorMode(VideoMirrorMode.VIDEO_MIRROR_MODE_AUTO);

    // 设置本地预览渲染模式, 默认为 FIT 模式
    previewRenderer.setRenderMode(VideoRenderMode.RENDER_MODE_FIT);
  }

  public void enableAudioRecording() {
    // 开启录音
    streamingKit.enableAudioRecording(true);
  }

  public void enableVideoCapturing() {
    // 开启摄像头采集
    streamingKit.enableVideoCapturing(true);
  }

  public void addOrRemoveVideoFilter() {
    // 添加自定义美颜
    streamingKit.addVideoFilter(videoFilter);

    // 删除自定义美颜
    streamingKit.removeVideoFilter(videoFilter);
  }

  public void startOrStopStreaming() {
    // 开始单主播直推 CDN
    streamingKit.startStreaming(publishUrl);

    // 停止单主播直推 CDN
    streamingKit.stopStreaming();
  }

  public void destroyStreamingKit() {
    // 销毁 Streaming Kit 单例
    StreamingKit.destroy();
  }

  public void switchToAgoraChannel() {
    // 停止单主播直推 CDN
    streamingKit.stopStreaming();

    // 设置 RTC SDK 自定义音频源
    rtcEngine.setExternalAudioSource(true, audioSampleRate, audioChannels);

    // 注册 Streaming Kit 的音频帧观测器，音频帧作为 RTC SDK 的自定义音频源
    streamingKit.registerAudioFrameObserver(audioFrameObserver);

    // 设置 RTC SDK 自定义视频源
    rtcEngine.setVideoSource(videoSource);

    // 注册 Streaming Kit 的视频帧观测器，视频帧作为 RTC SDK 的自定义视频源
    streamingKit.registerVideoFrameObserver(videoFrameObserver);

    // 加入 Agora 频道
    rtcEngine.joinChannel(null, "channelName", "", 0);

    // 在 onJoinChannelSuccess 和 onUserJoined 回调之后设置和开始旁路推流
  }

  public void switchToRtmpStreaming() {
    // 停止旁路推流
    rtcEngine.removePublishStreamUrl(publishUrl);

    // 退出 Agora 频道
    rtcEngine.leaveChannel();

    // 取消注册 Streaming Kit 的视频帧观测器
    streamingKit.unregisterVideoFrameObserver(videoFrameObserver);

    // 取消注册 Streaming Kit 的音频帧观测器
    streamingKit.unregisterAudioFrameObserver(audioFrameObserver);

    // 开始单主播直推 CDN
    streamingKit.startStreaming(publishUrl);
  }

  public int startServerStreaming() {
    LiveTranscoding liveTranscoding = new LiveTranscoding();
    liveTranscoding.width = width;
    liveTranscoding.height = height;
    liveTranscoding.videoBitrate = videoBitrate;
    liveTranscoding.videoFramerate = videoFramerate;
//    ArrayList<LiveTranscoding.TranscodingUser> transcodingUsers = cdnLayout(uidList, width, height);
//    liveTranscoding.setUsers(transcodingUsers);
    return rtcEngine.setLiveTranscoding(liveTranscoding);
  }

  private StreamingEventHandler streamingEventHandler = new StreamingEventHandler() {
    @Override
    public void onStartStreamingSuccess() {

    }

    @Override
    public void onStartStreamingFailure(int i, String s) {

    }

    @Override
    public void onMediaStreamingError(int i, String s) {

    }

    @Override
    public void onStreamingConnectionStateChanged(int i) {

    }
  };

  private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
      startServerStreaming();
    }
  };

  private AudioFrameObserver audioFrameObserver = new AudioFrameObserver() {
    @Override
    public void onAudioFrame(AudioFrame audioFrame) {

    }
  };

  private VideoFrameObserver videoFrameObserver = new VideoFrameObserver() {
    @Override
    public void onVideoFrame(VideoFrame videoFrame) {

    }
  };

  private IVideoSource videoSource = new IVideoSource() {
    @Override
    public boolean onInitialize(IVideoFrameConsumer consumer) {
      return false;
    }

    @Override
    public boolean onStart() {
      return false;
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDispose() {

    }

    @Override
    public int getBufferType() {
      return 0;
    }

    @Override
    public int getCaptureType() {
      return 0;
    }

    @Override
    public int getContentHint() {
      return 0;
    }
  };

  private VideoFilter videoFilter = new VideoFilter() {
    @Override
    public VideoFrame process(VideoFrame videoFrame) {
      return null;
    }
  };
}
