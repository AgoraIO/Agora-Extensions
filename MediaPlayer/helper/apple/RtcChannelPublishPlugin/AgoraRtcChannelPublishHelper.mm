//
//  AgoraRtcChannelPublishHelper.m
//  player_demo_apple
//
//  Created by zhanxiaochao on 2020/1/13.
//  Copyright © 2020 agora. All rights reserved.
//

#import "AgoraRtcChannelPublishHelper.h"
#import <AgoraRtcEngineKit/IAgoraRtcEngine.h>
#import <AgoraRtcEngineKit/IAgoraMediaEngine.h>
#import "AudioCircularBuffer.h"
#import "scoped_ptr.h"
#import <mutex>
using namespace AgoraRTC;
static scoped_ptr<AudioCircularBuffer<char>> agoraAudioBuf(new AudioCircularBuffer<char>(2048,true));
static scoped_ptr<AudioCircularBuffer<char>> agoraPlayoutBuf(new AudioCircularBuffer<char>(2048,true));
static NSObject *threadLockPush = [[NSObject alloc] init];
static NSObject *threadLockPlay = [[NSObject alloc] init];

class AgoraAudioFrameObserver:public agora::media::IAudioFrameObserver
{
public:
    std::atomic<float>  publishSignalValue_{1.0f};
    std::atomic<float>  playOutSignalValue_{1.0f};
    void setPublishSignalVolume(int volume){
        @synchronized (threadLockPush) {
            publishSignalValue_ = volume/100.0f;
        }
    }
    void setPlayoutSignalVolume(int volume){
         @synchronized (threadLockPlay) {
             playOutSignalValue_ = volume/100.0f;
         }
     }
    void pushData(char *data,int length){
        @synchronized (threadLockPush) {
           agoraAudioBuf->Push(data, length);
        }
        @synchronized (threadLockPlay) {
            agoraPlayoutBuf->Push(data, length);
        }

    }
    virtual bool onRecordAudioFrame(AudioFrame& audioFrame){
        @synchronized (threadLockPush) {
                int bytes = audioFrame.samples * audioFrame.channels * audioFrame.bytesPerSample;
                int16_t *tmpBuf = (int16_t *)malloc(sizeof(int16_t)*bytes);
                memcpy(tmpBuf, audioFrame.buffer, bytes);
                if (agoraAudioBuf->mAvailSamples < bytes) {
                    free(tmpBuf);
                    return true;
                }
                //计算重采样钱的数据大小 重采样的采样率 * SDK回调时间 * 声道数 * 字节数
                int mv_size = bytes;
                char *data = (char *)malloc(sizeof(char)*mv_size);
                agoraAudioBuf->Pop(data, mv_size);
                int16_t* p16 = (int16_t*) data;
                int16_t *audioBuf = (int16_t *)malloc(bytes);
                memcpy(audioBuf, tmpBuf, bytes);
                for (int i = 0; i < bytes / 2; ++i) {
                    tmpBuf[i] += (p16[i] * publishSignalValue_);
                    //audio overflow
                    if (tmpBuf[i] > 32767) {
                        audioBuf[i] = 32767;
                    }
                    else if (tmpBuf[i] < -32768) {
                        audioBuf[i] = -32768;
                    }
                    else {
                        audioBuf[i] = tmpBuf[i];
                    }
                }
                memcpy(audioFrame.buffer, audioBuf,bytes);
                free(audioBuf);
                free(tmpBuf);
                free(p16);
        }
        return true;
    }
    /**
     * Occurs when the playback audio frame is received.
     * @param audioframe The reference to the audio frame: AudioFrame.
     * @return
     * - true: The playback audio frame is valid and is encoded and sent.
     * - false: The playback audio frame is invalid and is not encoded or sent.
     */
    virtual bool onPlaybackAudioFrame(AudioFrame& audioFrame){
    @synchronized (threadLockPlay) {
        int bytes = audioFrame.samples * audioFrame.channels * audioFrame.bytesPerSample;
        int16_t *tmpBuf = (int16_t *)malloc(bytes);
        memcpy(tmpBuf, audioFrame.buffer, bytes);
        if (agoraPlayoutBuf->mAvailSamples < bytes) {
            memcpy(audioFrame.buffer, tmpBuf,bytes);
            free(tmpBuf);
            return true;
        }
        //计算重采样钱的数据大小 重采样的采样率 * SDK回调时间 * 声道数 * 字节数
        int mv_size = bytes;
        char *data = (char *)malloc(mv_size);
        agoraPlayoutBuf->Pop(data, mv_size);
        int16_t* p16 = (int16_t*) data;
        int16_t *audioBuf = (int16_t *)malloc(bytes);
        memcpy(audioBuf, tmpBuf, bytes);
        for (int i = 0; i < bytes / 2; ++i) {
            tmpBuf[i] += (p16[i] * playOutSignalValue_);
            //audio overflow
            if (tmpBuf[i] > 32767) {
                audioBuf[i] = 32767;
            }
            else if (tmpBuf[i] < -32768) {
                audioBuf[i] = -32768;
            }
            else {
                audioBuf[i] = tmpBuf[i];
            }
        }
        memcpy(audioFrame.buffer, audioBuf,bytes);
        free(audioBuf);
        free(tmpBuf);
        free(p16);
    }
        return true;
    }
    /**
     * Occurs when the mixed audio data is received.
     * @param audioframe The reference to the audio frame: AudioFrame.
     * @return
     * - true: The mixed audio data is valid and is encoded and sent.
     * - false: The mixed audio data is invalid and is not encoded or sent.
     */
    virtual bool onMixedAudioFrame(AudioFrame& audioFrame){
        return false;
    }
    /**
     * Occurs when the playback audio frame before mixing is received.
     * @param audioframe The reference to the audio frame: AudioFrame.
     * @return
     * - true: The playback audio frame before mixing is valid and is encoded and sent.
     * - false: The playback audio frame before mixing is invalid and is not encoded or sent.
     */
    virtual bool onPlaybackAudioFrameBeforeMixing(unsigned int uid, AudioFrame& audioFrame){
        return false;
    }
};
@interface AgoraRtcChannelPublishHelper()<AgoraMediaPlayerDelegate,AgoraVideoSourceProtocol>
{
    std::unique_ptr<AgoraAudioFrameObserver> audioFrameObserver;
    BOOL isPublishVideo;
}
@property (nonatomic, weak)AgoraMediaPlayer *playerKit;
@property (nonatomic, weak)AgoraRtcEngineKit *rtcEngineKit;
@property (nonatomic, weak)id<AgoraRtcChannelPublishHelperDelegate> delegate;
@property (nonatomic, assign)bool isDispatchMainQueue;

@end
@implementation AgoraRtcChannelPublishHelper

static AgoraRtcChannelPublishHelper *instance = NULL;
+ (instancetype)shareInstance{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if (instance == NULL) {
            instance = [[AgoraRtcChannelPublishHelper alloc] init];
        }
    });
    return instance;
}
// 连接 MediaPlayer 到主版本 RTC SDK
- (void)attachPlayerToRtc:(AgoraMediaPlayer *)playerKit RtcEngine:(AgoraRtcEngineKit *)rtcEngine{
    audioFrameObserver = std::make_unique<AgoraAudioFrameObserver>();
    isPublishVideo = false;
    audioFrameObserver->setPublishSignalVolume(0);
    self.isDispatchMainQueue = false;
    playerKit.delegate = self;
    [rtcEngine setVideoSource:self];
    [rtcEngine setParameters:@"{\"che.audio.keep.audiosession\":true}"];
    [rtcEngine setAudioProfile:AgoraAudioProfileMusicStandardStereo scenario:AgoraAudioScenarioGameStreaming];
    [rtcEngine setRecordingAudioFrameParametersWithSampleRate:48000 channel:2 mode:AgoraAudioRawFrameOperationModeReadWrite samplesPerCall:960];
    [rtcEngine setPlaybackAudioFrameParametersWithSampleRate:48000 channel:2 mode:AgoraAudioRawFrameOperationModeReadWrite samplesPerCall:960];

    [self registerRtcEngine:rtcEngine];
    _playerKit = playerKit;
    _rtcEngineKit = rtcEngine;
    [self resetAudioBuf];
}
// 启动/停止推送音频流到频道
- (void)publishAudio{
    @synchronized (self) {
        audioFrameObserver->setPublishSignalVolume(100);
    }
}
- (void)unpublishAudio{
    @synchronized (self) {
        audioFrameObserver->setPublishSignalVolume(0);
        [self resetAudioBuf];
    }

}
// 启动/停止推送视频流到频道
- (void)publishVideo{
    @synchronized (self) {
        isPublishVideo = true;
    }
}
- (void)unpublishVideo{
    
    @synchronized (self) {
        isPublishVideo = false;
    }
}
// 调节推送到频道内音频流的音量
- (void)adjustPublishSignalVolume:(int)volume{
    
    @synchronized (self) {
        audioFrameObserver->setPublishSignalVolume(volume);
    }
}
-(void)adjustPlayoutSignalVolume:(int)volume
{   @synchronized (self) {
         audioFrameObserver->setPlayoutSignalVolume(volume);
     }
}
// 断开 MediaPlayer 和 RTC SDK 的关联
- (void)detachPlayerFromRtc{
    @synchronized (self) {
        isPublishVideo=false;
        audioFrameObserver->setPublishSignalVolume(0);
        _playerKit.delegate = NULL;
        [_rtcEngineKit setVideoSource:NULL];
        [self unregisterRtcEngine:_rtcEngineKit];

    }
}
- (void)resetAudioBuf{
    @synchronized (self) {
        agoraAudioBuf.reset(new AudioCircularBuffer<char>(2048,true));
        agoraPlayoutBuf.reset(new AudioCircularBuffer<char>(2048,true));
    }
}
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *_Nonnull)playerKit
    didReceiveVideoFrame:(CVPixelBufferRef)pixelBuffer{
    @synchronized (self) {
        if (!isPublishVideo) {
            return;
        }
        //pushExternalCVPixelBuffer
        [self.consumer consumePixelBuffer:pixelBuffer withTimestamp:CMTimeMake(CACurrentMediaTime()*1000, 1000) rotation:AgoraVideoRotationNone];

    }

}
- (void)registerRtcEngine:(AgoraRtcEngineKit *)rtcEngine
{
    agora::rtc::IRtcEngine* rtc_engine = (agora::rtc::IRtcEngine*)rtcEngine.getNativeHandle;
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtc_engine, agora::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        mediaEngine->registerAudioFrameObserver(audioFrameObserver.get());
    }
}
- (void)unregisterRtcEngine:(AgoraRtcEngineKit *)rtcEngine
{
    agora::rtc::IRtcEngine* rtc_engine = (agora::rtc::IRtcEngine*)rtcEngine.getNativeHandle;
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtc_engine, agora::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        mediaEngine->registerAudioFrameObserver(NULL);
    }
}

- (void)AgoraMediaPlayer:(AgoraMediaPlayer *_Nonnull)playerKit
    didReceiveAudioFrame:(CMSampleBufferRef)audioFrame{
    //pushExternalAudioBuffer
    CMBlockBufferRef audioBuffer  = CMSampleBufferGetDataBuffer(audioFrame);
    OSStatus err;
    size_t lengthAtOffSet;
    size_t totalBytes;
    char *samples;
    err = CMBlockBufferGetDataPointer(audioBuffer, 0, &lengthAtOffSet, &totalBytes, &samples);
    if (totalBytes == 0) {
        return;
    }
    audioFrameObserver->pushData(samples, (int)totalBytes);

}
@synthesize consumer;

- (AgoraVideoBufferType)bufferType {
    return AgoraVideoBufferTypePixelBuffer;
}

- (void)shouldDispose {
    
}

- (BOOL)shouldInitialize {
    return true;
}

- (void)shouldStart {
    
}

- (void)shouldStop {
    
}

/// Description of state of Mediaplayer's state
/// @param playerKit AgoraMediaPlayer
/// @param state AgoraMediaPlayerState
/// @param reason AgoraMediaPlayerStateReason
/// @param error AgoraMediaPlayerError
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *_Nonnull)playerKit
       didChangedToState:(AgoraMediaPlayerState)state
                   error:(AgoraMediaPlayerError)error
{
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(AgoraRtcChannelPublishHelperDelegate:didChangedToState:error:)]) {
        __weak typeof(self) weakSelf = self;
        [self executeBlock:^{
            if (state == AgoraMediaPlayerStateOpenCompleted) {
                [weakSelf.playerKit mute:true];
                [weakSelf resetAudioBuf];
            }
            [self.delegate AgoraRtcChannelPublishHelperDelegate:weakSelf.playerKit didChangedToState:state error:error];
        }];
    }
    
}

/// callback of position
/// @param playerKit AgoraMediaPlayer
/// @param position position
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *_Nonnull)playerKit
    didChangedToPosition:(NSInteger)position
{
    if (self.delegate && [self.delegate respondsToSelector:@selector(AgoraRtcChannelPublishHelperDelegate:didChangedToPosition:)]) {
        __weak typeof(self) weakSelf = self;
        [self executeBlock:^{
            [self.delegate AgoraRtcChannelPublishHelperDelegate:weakSelf.playerKit didChangedToPosition:position];
        }];
    }
}

/// callback of seek state
/// @param playerkit AgoraMediaPlayer
/// @param state Description of seek state
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit didOccurEvent:(AgoraMediaPlayerEvent)event
{
    if (self.delegate && [self.delegate respondsToSelector:@selector(AgoraRtcChannelPublishHelperDelegate:didOccureEvent:)]) {
        __weak typeof(self) weakSelf = self;
        [self executeBlock:^{
            if (event == AgoraMediaPlayerEventSeekComplete) {
                [weakSelf resetAudioBuf];
            }
            [self.delegate AgoraRtcChannelPublishHelperDelegate:weakSelf.playerKit didOccureEvent:event];
        }];
    }
    
}

/// callback of SEI
/// @param playerkit AgoraMediaPlayer
/// @param data SEI's data
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit metaDataType:(AgoraMediaPlayerMetaDataType)type didReceiveData:(NSString *)data length:(NSInteger)length{
    if (self.delegate && [self.delegate respondsToSelector:@selector(AgoraRtcChannelPublishHelperDelegate:didReceiveData:length:)]) {
        __weak typeof(self) weakSelf = self;
        [self executeBlock:^{
            [self.delegate AgoraRtcChannelPublishHelperDelegate:weakSelf.playerKit didReceiveData:data length:length];
        }];
    }
    
}
- (void)registerRtcChannelPublishHelperDelegate:(id<AgoraRtcChannelPublishHelperDelegate>)delegate{
    @synchronized (self) {
        self.delegate = delegate;
    }
}
- (void)executeBlock:(void (^)())block {
  if (self.isDispatchMainQueue) {
    dispatch_async(dispatch_get_main_queue(), ^{
      block();
    });
  } else {
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
      block();
    });
  }
}

@end

