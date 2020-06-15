#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include "AgoraMediaBase.h"
#include "IAgoraMediaPlayer.h"
#include <thread>
#include <iostream>

#include <stdio.h>
#include <stdlib.h>

#include <mutex>

#ifdef ANDROID
#include <android/log.h>
#define XLOGD(...) __android_log_print(ANDROID_LOG_DEBUG,"[player_cpp]",__VA_ARGS__)
#define XLOGI(...) __android_log_print(ANDROID_LOG_INFO,"[player_cpp]",__VA_ARGS__)
#define XLOGW(...) __android_log_print(ANDROID_LOG_WARN,"[player_cpp]",__VA_ARGS__)
#define XLOGE(...) __android_log_print(ANDROID_LOG_ERROR,"[player_cpp]",__VA_ARGS__)
#else
#include <stdio.h>
#define XLOGD(format, ...) printf("[player_cpp][DEBUG][%s][%d]: " format "\n", __FUNCTION__,\
                            __LINE__, ##__VA_ARGS__)
#define XLOGI(format, ...) printf("[player_cpp][INFO][%s][%d]: " format "\n", __FUNCTION__,\
                            __LINE__, ##__VA_ARGS__)
#define XLOGW(format, ...) printf("[player_cpp][WARN][%s][%d]: " format "\n", __FUNCTION__,\
                            __LINE__, ##__VA_ARGS__)
#define XLOGE(format, ...) printf("[player_cpp][ERROR][%s][%d]: " format "\n", __FUNCTION__,\
                            __LINE__, ##__VA_ARGS__)
#endif


static JavaVM *gJVM = nullptr;

class AndroidAgoraPlayerObserver : public agora::rtc::IMediaPlayerObserver,
                           public agora::media::base::IVideoFrameObserver,
                           public agora::media::base::IAudioFrameObserver {
 public:
  void onPlayerStateChanged(agora::media::MEDIA_PLAYER_STATE state,
                            agora::media::MEDIA_PLAYER_ERROR ec) {
        XLOGI("onPlayerStateChanged %d,%d",state,ec);
  };

  void onPositionChanged(const int64_t position) {
    XLOGI("onPositionChanged %lld",position);
  }

  void onPlayerEvent(agora::media::MEDIA_PLAYER_EVENT event) {
    XLOGI("onPlayerEvent %d",event);
  }

  void onMetadata(agora::media::MEDIA_PLAYER_METADATA_TYPE type, const uint8_t* data, uint32_t length) {
    XLOGI("onMetadata %d %d %s",type,length,data);
  }


 public:
  // IVideoFrameObserver
  void onFrame(const agora::media::base::VideoFrame* frame) {
   //XLOGI("onFrame video %d,%d,%d,%d,%lld", frame->type, frame->yStride,
   //               frame->height, frame->width, frame->renderTimeMs);
  }
  // IAudioFrameObserver
  void onFrame(const agora::media::base::AudioPcmFrame* frame) {
    //XLOGI("onFrame audio %d,%d,%d,%d,%d", frame->samples_per_channel_,
    //                 frame->sample_rate_hz_, frame->num_channels_, frame->bytes_per_sample,
    //                 frame->capture_timestamp);
  }
};

AndroidAgoraPlayerObserver *observer_ = new AndroidAgoraPlayerObserver();
agora::rtc::IMediaPlayer* media_player_ = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_initMediaPlayerCpp(JNIEnv *env,jobject thiz, jobject context) {
    XLOGI("initMediaPlayerCpp");
    jobject m_app_context = (jobject)env->NewGlobalRef(context);
    media_player_ = createAgoraMediaPlayer();
    agora::rtc::MediaPlayerContext context_android;
    context_android.context = m_app_context;
    media_player_->initialize(context_android);
    media_player_->registerPlayerObserver(observer_);
    media_player_->registerVideoFrameObserver(observer_);
    media_player_->registerAudioFrameObserver(observer_);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_openMediaPlayerCpp(JNIEnv *env,jobject thiz,  jstring src, jlong startPos) {
    XLOGI("openMediaPlayerCpp");
  const char *video_path = nullptr;
  video_path = env->GetStringUTFChars(src, 0);
  XLOGI("nativeOpen %s,%lld ", video_path, startPos);
  int ret = media_player_->open(video_path, startPos);
  env->ReleaseStringUTFChars(src, video_path);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_playMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    XLOGI("playMediaPlayerCpp");
    media_player_->play();
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_setViewMediaPlayerCpp(JNIEnv *env,jobject thiz,jobject video_view) {
    XLOGI("setViewMediaPlayerCpp");
    jobject m_view = env->NewGlobalRef(video_view);
    media_player_->setView(m_view);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_pauseMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    XLOGI("pauseMediaPlayerCpp");
    media_player_->pause();
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_stopMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    XLOGI("stopMediaPlayerCpp");
    media_player_->stop();
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_muteMediaPlayerCpp(JNIEnv *env,jobject thiz,jboolean mute) {
    XLOGI("muteMediaPlayerCpp");
    media_player_->mute(mute);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_durationMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    int64_t duration;
    media_player_->getDuration(duration);
    XLOGI("durationMediaPlayerCpp %lld",duration);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_streamsMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    int count;
    media_player_->getStreamCount(count);
    XLOGI("streamsMediaPlayerCpp %d",count);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_infosMediaPlayerCpp(JNIEnv *env,jobject thiz) {
    int count;
    media_player_->getStreamCount(count);
    agora::media::MediaStreamInfo si;
    for (int i = 0;i<count;i++) {
        media_player_->getStreamInfo(i, &si);
        XLOGI("infosMediaPlayerCpp %d,%d,%s,%s,%d,%d,%d,%d,%d,%d,%d,%lld", si.streamIndex,si.streamType, si.codecName,
                    si.language, si.videoFrameRate, si.videoBitRate,si.videoWidth, si.videoHeight, si.videoRotation, si.audioSampleRate, si.audioChannels, si.duration);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_adjustPlayoutVolumeMediaPlayerCpp(JNIEnv *env,jobject thiz, jint volume) {
    media_player_->adjustPlayoutVolume(volume);
    XLOGI("adjustPlayoutVolumeMediaPlayerCpp %d",volume);
}

extern "C"
JNIEXPORT void JNICALL
Java_io_agora_mediaplayer_PlayerCppFragment_seekMediaPlayerCpp(JNIEnv *env,jobject thiz, jlong position) {
    media_player_->seek(position);
    XLOGI("seekMediaPlayerCpp %lld",position);
}


extern "C" jint JNIEXPORT JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved) {
    XLOGI("TJY JNI_OnLoad");
    return JNI_VERSION_1_4;
}