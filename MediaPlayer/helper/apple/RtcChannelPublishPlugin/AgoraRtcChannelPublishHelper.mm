//
//  AgoraRtcChannelPublishHelper.m
//  player_demo_apple
//
//  Created by zhanxiaochao on 2020/1/13.
//  Copyright © 2020 agora. All rights reserved.
//

#import "AgoraRtcChannelPublishHelper.h"
#import <AgoraRtcKit/IAgoraRtcEngine.h>
#import <AgoraRtcKit/IAgoraMediaEngine.h>
#import "AudioCircleBuffer.h"
#import <mutex>

namespace {
typedef std::numeric_limits<int16_t> limits_int16;

// The conversion functions use the following naming convention:
// S16:      int16_t [-32768, 32767]
// Float:    float   [-1.0, 1.0]
// FloatS16: float   [-32768.0, 32767.0]
// Dbfs: float [-20.0*log(10, 32768), 0] = [-90.3, 0]
// The ratio conversion functions use this naming convention:
// Ratio: float (0, +inf)
// Db: float (-inf, +inf)
static inline int16_t FloatToS16(float v) {
  if (v > 0)
    return v >= 1 ? limits_int16::max()
                  : static_cast<int16_t>(v * limits_int16::max() + 0.5f);
  return v <= -1 ? limits_int16::min()
                 : static_cast<int16_t>(-v * limits_int16::min() - 0.5f);
}
const float kFourOddHarmonicsZeroGainTable[] = {
    0.057728029f, 0.057896007f, 0.058399724f, 0.059238520f, 0.060411299f, 0.061916529f,
    0.063752244f, 0.065916046f, 0.068405114f, 0.071216201f, 0.074345646f, 0.077789374f,
    0.081542909f, 0.085601374f, 0.089959506f, 0.094611658f, 0.099551813f, 0.104773591f,
    0.110270261f, 0.116034750f, 0.122059658f, 0.128337265f, 0.134859550f, 0.141618196f,
    0.148604613f, 0.155809946f, 0.163225092f, 0.170840713f, 0.178647255f, 0.186634963f,
    0.194793895f, 0.203113942f, 0.211584843f, 0.220196207f, 0.228937523f, 0.237798187f,
    0.246767512f, 0.255834755f, 0.264989129f, 0.274219827f, 0.283516038f, 0.292866968f,
    0.302261860f, 0.311690010f, 0.321140794f, 0.330603677f, 0.340068243f, 0.349524208f,
    0.358961441f, 0.368369984f, 0.377740070f, 0.387062142f, 0.396326872f, 0.405525180f,
    0.414648249f, 0.423687547f, 0.432634839f, 0.441482209f, 0.450222071f, 0.458847188f,
    0.467350688f, 0.475726074f, 0.483967240f, 0.492068487f, 0.500024529f, 0.507830510f,
    0.515482012f, 0.522975062f, 0.530306146f, 0.537472213f, 0.544470682f, 0.551299447f,
    0.557956882f, 0.564441843f, 0.570753669f, 0.576892186f, 0.582857699f, 0.588650995f,
    0.594273337f, 0.599726455f, 0.605012545f, 0.610134253f, 0.615094669f, 0.619897310f,
    0.624546108f, 0.629045392f, 0.633399870f, 0.637614607f, 0.641695003f, 0.645646765f,
    0.649475883f, 0.653188598f, 0.656791368f, 0.660290835f, 0.663693789f, 0.667007121f,
    0.670237789f, 0.673392764f, 0.676478986f, 0.679503310f, 0.682472451f, 0.685392930f,
    0.688271007f, 0.691112619f, 0.693923315f, 0.696708179f, 0.699471760f, 0.702217993f,
    0.704950112f, 0.707670573f, 0.710380954f, 0.713081870f, 0.715772871f, 0.718452340f,
    0.721117388f, 0.723763743f, 0.726385637f, 0.728975684f, 0.731524758f, 0.734021866f,
    0.736454009f, 0.738806050f, 0.741060570f, 0.743197717f, 0.745195054f, 0.747027403f,
    0.748666679f, 0.750081719f, 0.751238112f};

#define ANTICLIP_MAX_INPUT_VALUE 4.0f  // The maximum input value to SRS Anticlip
#define ANTICLIP_TBL_SIZE (sizeof(kFourOddHarmonicsZeroGainTable) / sizeof(float) - 1)  // 128
#define OPERATION_THRESHOLD 0.02f

void UniversalGainProcess(int16_t* audioIO, int blocksize, float gain){
  int idx1, idx2;
  float v, tmp, scale, scaleIdx1, scaleIdx2;
  // A small upward correction for the gain factor
  float apply_gain = gain * 1.1f * 3.0517578125E-5f;

  for (blocksize--; blocksize >= 0; blocksize--) {
    v = audioIO[blocksize] * apply_gain;
    tmp = v >= 0 ? v : -v;
    tmp *= (ANTICLIP_TBL_SIZE / ANTICLIP_MAX_INPUT_VALUE);
    idx1 = (int)tmp;
    idx2 = idx1 + 1;
    if (idx1 >= (int)ANTICLIP_TBL_SIZE) {
      idx1 = idx2 = ANTICLIP_TBL_SIZE;
    }

    scaleIdx1 = kFourOddHarmonicsZeroGainTable[idx1];
    scaleIdx2 = kFourOddHarmonicsZeroGainTable[idx2];
    scale = scaleIdx1 + (scaleIdx2 - scaleIdx1) * (tmp - idx1);  // 1st order linear interpolation
    audioIO[blocksize] = FloatToS16(v * (1 - scale)) ;
  }
}

}


class AgoraAudioFrameObserver:public agora::media::IAudioFrameObserver
{
private:
    int16_t * record_buf_tmp_ = nullptr;
    char *    record_audio_mix_ = nullptr;
    int16_t * record_send_buf_ = nullptr;
    
    int16_t * play_buf_tmp_ = nullptr;
    char  *   play_audio_mix_ = nullptr;
    int16_t * play_send_buf_ = nullptr;
    std::shared_ptr<AudioCircularBuffer<char>> record_audio_buf_;
    std::shared_ptr<AudioCircularBuffer<char>> play_audio_buf_;
public:
    std::atomic<float>  publishSignalValue_{1.0f};
    std::atomic<float>  playOutSignalValue_{1.0f};
    std::atomic<bool>   isOnlyAudioPlay_{false};
    AgoraAudioFrameObserver(){
        record_audio_buf_.reset(new AudioCircularBuffer<char>(2048));
        play_audio_buf_.reset(new AudioCircularBuffer<char>(2048));
    }
    ~AgoraAudioFrameObserver()
    {
        if (record_buf_tmp_) {
            free(record_buf_tmp_);
        }
        if(record_audio_mix_){
            free(record_audio_mix_);
        }
        if(record_send_buf_){
            free(record_send_buf_);
        }
        
        if (play_buf_tmp_) {
            free(play_buf_tmp_);
        }
        if(play_audio_mix_){
            free(play_audio_mix_);
        }
        if (play_send_buf_) {
            free(play_send_buf_);
        }
    }
    void resetAudioBuffer(){
        
        record_audio_buf_.reset(new AudioCircularBuffer<char>(2048));
        play_audio_buf_.reset(new AudioCircularBuffer<char>(2048));
    }
    void setPublishSignalVolume(int volume){
        publishSignalValue_ = volume/100.0f;
    }
    void enableOnlyAudioPlay(bool isEnable){
        isOnlyAudioPlay_ = isEnable;
    }
    void setPlayoutSignalVolume(int volume){
             playOutSignalValue_ = volume/100.0f;
     }
    void pushData(char *data,int length){
        {
            if (!isOnlyAudioPlay_) {
                record_audio_buf_->Push(data, length);
            }
        }
        {
            play_audio_buf_->Push(data, length);
        }

    }
    virtual bool onRecordAudioFrame(AudioFrame& audioFrame){
        int bytes = audioFrame.samples * audioFrame.channels * audioFrame.bytesPerSample;
        int ret = record_audio_buf_->getSize() - bytes;
        if ( ret < 0) {
            return true;
        }
        //计算重采样钱的数据大小 重采样的采样率 * SDK回调时间 * 声道数 * 字节数
        if (!record_buf_tmp_) {
            record_buf_tmp_ = (int16_t *)malloc(bytes);
        }
        if(!record_audio_mix_){
            record_audio_mix_ = (char *)malloc(bytes);
        }
        if(!record_send_buf_){
            record_send_buf_ = (int16_t *)malloc(bytes);
        }
        record_audio_buf_->Pop(record_audio_mix_, bytes);
        int16_t* p16 = (int16_t*) record_audio_mix_;
        memcpy(record_buf_tmp_, audioFrame.buffer, bytes);
        for (int i = 0; i < bytes / 2; ++i) {
            record_buf_tmp_[i] += (p16[i] * publishSignalValue_);
            //audio overflow
            if (record_buf_tmp_[i] > 32767) {
                record_send_buf_[i] = 32767;
            }
            else if (record_buf_tmp_[i] < -32768) {
                record_send_buf_[i] = -32768;
            }
            else {
                record_send_buf_[i] = record_buf_tmp_[i];
            }
        }
        memcpy(audioFrame.buffer, record_send_buf_,bytes);
    
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
        
        int bytes = audioFrame.samples * audioFrame.channels * audioFrame.bytesPerSample;
        if(play_audio_buf_->getSize() > (960 * 80)){//队列中的数据淤积大于800ms时 就丢弃数据
            play_audio_buf_->Reset();
        }
        int ret = play_audio_buf_->getSize() - bytes;
        if (ret < 0) {
            return true;
        }
        //计算重采样钱的数据大小 重采样的采样率 * SDK回调时间 * 声道数 * 字节数
        if(!play_buf_tmp_){
            play_buf_tmp_ = (int16_t *)malloc(bytes);
        }
        if(!play_audio_mix_){
            play_audio_mix_ = (char *)malloc(bytes);
        }
        if(!play_send_buf_){
            play_send_buf_ = (int16_t *)malloc(bytes);
        }
        play_audio_buf_->Pop(play_audio_mix_, bytes);
        int16_t* p16 = (int16_t*) play_audio_mix_;
        memcpy(play_buf_tmp_, audioFrame.buffer, bytes);

        for(int i = 0; i < bytes/2 ; i++){
          int tmp1 = (play_buf_tmp_[i] / 2);
          int tmp2 = (p16[i] * playOutSignalValue_ / 2);
          play_buf_tmp_[i] = tmp1 + tmp2;
        }

        // doLimiter
        ::UniversalGainProcess(play_buf_tmp_,bytes/2, 2.0f);
        
        memcpy(audioFrame.buffer, play_buf_tmp_,bytes);
    
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
- (void)attachPlayerToRtc:(AgoraMediaPlayer *)playerKit RtcEngine:(AgoraRtcEngineKit *)rtcEngine enableVideoSource:(bool)enable{
    audioFrameObserver = std::make_unique<AgoraAudioFrameObserver>();
    isPublishVideo = false;
    audioFrameObserver->setPublishSignalVolume(0);
    self.isDispatchMainQueue = false;
    playerKit.delegate = self;
    if (enable) {
        [rtcEngine setVideoSource:self];
    }
    [rtcEngine setParameters:@"{\"che.audio.keep.audiosession\":true}"];
    [rtcEngine setAudioProfile:AgoraAudioProfileDefault scenario:AgoraAudioScenarioGameStreaming];
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
- (void)enableOnlyLocalAudioPlay:(bool)isEnable

{
    @synchronized (self) {
        audioFrameObserver->enableOnlyAudioPlay(isEnable);
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
        [self unregisterRtcEngine:_rtcEngineKit];
        [_rtcEngineKit setVideoSource:NULL];
        _playerKit.delegate = NULL;
        
    }
}
- (void)resetAudioBuf{
    @synchronized (self) {
        audioFrameObserver->resetAudioBuffer();
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

/** Gets the capture type of the custom video source.

 @since v3.1.0

 Before you initialize the custom video source, the SDK triggers this callback to query the capture type
 of the video source. You must specify the capture type in the return value and then pass it to the SDK.
 The SDK enables the corresponding video processing algorithm according to the capture type after
 receiving the video frame.

 @return AgoraVideoCaptureType
 */
- (AgoraVideoCaptureType)captureType{
    return AgoraVideoCaptureTypeUnknown;
}
/** Gets the content hint of the custom video source.

 @since v3.1.0

 If you specify the custom video source as a screen-sharing video, the SDK triggers this callback to query
 the content hint of the video source before you initialize the video source. You must specify the content
 hint in the return value and then pass it to the SDK. The SDK enables the corresponding video processing
 algorithm according to the content hint after receiving the video frame.

 @return AgoraVideoContentHint
 */
- (AgoraVideoContentHint)contentHint
{
    return AgoraVideoContentHintNone;
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



