//
//  ViewController.m
//  player_demo_iOS
//
//  Created by zhanxiaochao on 2019/12/2.
//  Copyright © 2019 agora. All rights reserved.
//

#import "ViewController.h"
#import <AgoraMediaPlayer/AgoraMediaPlayerKit.h>
#import "AgoraRtcChannelPublishHelper.h"
#include <AgoraRtcEngineKit/AgoraRtcEngineKit.h>

@interface ViewController ()<AgoraMediaPlayerDelegate,AgoraRtcChannelPublishHelperDelegate,AgoraRtcEngineDelegate>
{
    BOOL isPublishAudio;
    BOOL isPublishVideo;
    BOOL isAttach;
}
@property (nonatomic, strong) AgoraMediaPlayer *mediaPlayerKit;
@property (weak, nonatomic) IBOutlet UIView *containerView;
@property (weak, nonatomic) IBOutlet UISlider *seekSlider;
@property (weak, nonatomic) IBOutlet UISlider *volumeSlider;
@property (weak, nonatomic) IBOutlet UILabel *durationLabel;
@property (weak, nonatomic) IBOutlet UITextView *logTextView;
@property (weak, nonatomic) IBOutlet UIButton *publishAudio;
@property (weak, nonatomic) IBOutlet UIButton *publishVideo;
@property (weak, nonatomic) IBOutlet UITextView *inputTextView;


@property (nonatomic, strong) NSMutableString *logStr;
@property (nonatomic, assign) bool isDispatchMainQueue;

@property (nonatomic, strong)AgoraRtcEngineKit *rtcEnginekit;

@end

@implementation ViewController




- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self configUI];
    [self initMediaKit];
    [self initAgoraSdk];
    [_mediaPlayerKit setView:self.containerView];

}
- (void)initAgoraSdk{
    _rtcEnginekit = [AgoraRtcEngineKit sharedEngineWithAppId:@"" delegate:self];
    [_rtcEnginekit setChannelProfile:AgoraChannelProfileLiveBroadcasting];
    [_rtcEnginekit setClientRole:AgoraClientRoleBroadcaster];
    [_rtcEnginekit enableAudio];
    [_rtcEnginekit enableVideo];
    AgoraVideoEncoderConfiguration *config = [[AgoraVideoEncoderConfiguration alloc] init];
    config.bitrate = 800;
    config.dimensions = CGSizeMake(640, 360);
    config.frameRate = AgoraVideoBitrateStandard;
    config.orientationMode = AgoraVideoOutputOrientationModeAdaptative;
    [_rtcEnginekit setVideoEncoderConfiguration:config];
    [[AgoraRtcChannelPublishHelper shareInstance] attachPlayerToRtc:_mediaPlayerKit RtcEngine:_rtcEnginekit enableVideoSource:true];
    [[AgoraRtcChannelPublishHelper shareInstance] registerRtcChannelPublishHelperDelegate:self];
    [_rtcEnginekit joinChannelByToken:@"" channelId:@"123456" info:@"" uid:0 joinSuccess:NULL];
}
- (void)initMediaKit{
    _mediaPlayerKit = [[AgoraMediaPlayer alloc] initWithDelegate:self];
    isPublishAudio = false;
    isPublishVideo = false;
    isAttach = true;
}
- (void)configUI{
    self.isDispatchMainQueue = true;
    [self.seekSlider addTarget:self action:@selector(valueOfChange:) forControlEvents:UIControlEventTouchUpInside];
    [self.volumeSlider addTarget:self action:@selector(valueOfChange:) forControlEvents:UIControlEventTouchUpInside];
}
- (IBAction)publishAudio:(UIButton *)sender {
    
    if (!isPublishAudio) {
        [[AgoraRtcChannelPublishHelper shareInstance] publishAudio];
        [sender setTitle:@"unpublishAudio" forState:UIControlStateNormal];
    }else{
        [[AgoraRtcChannelPublishHelper shareInstance] unpublishAudio];
        [sender setTitle:@"publishAudio" forState:UIControlStateNormal];
    }
    isPublishAudio =! isPublishAudio;

}
- (IBAction)attach:(UIButton *)sender {
    if (isAttach) {
        [[AgoraRtcChannelPublishHelper shareInstance] attachPlayerToRtc:_mediaPlayerKit RtcEngine:_rtcEnginekit];
        [sender setTitle:@"detach" forState:UIControlStateNormal];
        [self.publishAudio setTitle:@"publishAudio" forState:UIControlStateNormal];
        [self.publishVideo setTitle:@"publishVideo" forState:UIControlStateNormal];
        isPublishVideo = false;
        isPublishAudio = false;
    }else{
        [[AgoraRtcChannelPublishHelper shareInstance] detachPlayerFromRtc];
        [sender setTitle:@"attach" forState:UIControlStateNormal];
    }
    isAttach =! isAttach;
}
- (IBAction)publishVideo:(UIButton *)sender {
    
    if (!isPublishVideo) {
        [[AgoraRtcChannelPublishHelper shareInstance] publishVideo];
        [sender setTitle:@"unpublishVideo" forState:UIControlStateNormal];
    }else{
        [[AgoraRtcChannelPublishHelper shareInstance] unpublishVideo];
        [sender setTitle:@"publishVideo" forState:UIControlStateNormal];
    }
    isPublishVideo =! isPublishVideo;


}

- (void)valueOfChange:(UISlider *)sender{
    if ([sender isEqual:self.seekSlider]) {
        NSLog(@"seekChange");
        if(self.seekSlider.value < 0){
            return;
        }
        [_mediaPlayerKit seekToPosition:self.seekSlider.value];
    }else{
        NSLog(@"volumeChange");
        [[AgoraRtcChannelPublishHelper shareInstance] adjustPlayoutSignalVolume:sender.value];
    }
}
- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [self.view endEditing:true];
}
- (IBAction)openMediaFile:(UIButton *)sender {
    if ([self.inputTextView.text isEqualToString:@""]) {
        return;
    }
    NSString *filePath = [[[NSBundle mainBundle] pathForResource:@"resources" ofType:@"bundle"] stringByAppendingPathComponent:@"83.mp4"];
    [_mediaPlayerKit open:filePath startPos:0];
    
}
- (NSMutableString *)logStr
{
    if (_logStr == NULL) {
        _logStr = [NSMutableString string];
    }
    return _logStr;
}
- (IBAction)play:(UIButton *)sender {
    [_mediaPlayerKit play];
}
- (IBAction)stop:(UIButton *)sender {
    [_mediaPlayerKit stop];
}
- (IBAction)pause:(UIButton *)sender {
    [_mediaPlayerKit pause];
}
- (IBAction)resume:(UIButton *)sender {
    [_mediaPlayerKit setView:NULL];
    [_mediaPlayerKit setView:self.containerView];
    
}
- (IBAction)getPostion:(id)sender {
    NSLog(@"%ld",[_mediaPlayerKit getPosition]);
}
- (IBAction)getduration:(UIButton *)sender {
    __weak typeof(self) weakSelf = self;
    
    [self executeBlock:^{
        int64_t duration = [weakSelf.mediaPlayerKit getDuration];
        long hour = duration / 3600;
        long min = duration / 60;
        long ss = duration % 60;
        weakSelf.durationLabel.text  = [NSString stringWithFormat:@"%ld:%ld:%ld",hour,min,ss];
        if (duration < 0) {
            return;
        }
        weakSelf.seekSlider.maximumValue = duration;
    }];

}
- (IBAction)getstreamCount:(UIButton *)sender {
    NSInteger count = [_mediaPlayerKit getStreamCount];
    [self.logStr appendString:[NSString stringWithFormat:@"streamcount  == %ld",count]];
    __weak typeof(self) weakSelf = self;
    [self executeBlock:^{
        [weakSelf writeLogStr:weakSelf.logStr];
    }];
}
- (IBAction)mute:(UIButton *)sender {
    bool mute = [_mediaPlayerKit getMute];
    [_mediaPlayerKit mute:!mute];

}
#pragma mark MediaPlayerKitDelegateMethods

- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error
{
    
    switch (state) {
                case AgoraMediaPlayerStateOpenCompleted:{
                        [self.logStr appendString:@"AgoraMediaPlayerStateOpenCompleted \n"];
                    
                    break;
                }
                case AgoraMediaPlayerStatePlaying:
                {
                        [self.logStr appendString:@"AgoraMediaPlayerStatePlaying \n"];
                    
                    break;
                }
                case AgoraMediaPlayerStateStopped:
                {
                        [self.logStr appendString:@"AgoraMediaPlayerStateStopped \n"];
                               
                    break;
                }
                case AgoraMediaPlayerStateOpening:
                    {
                        [self.logStr appendString:@"AgoraMediaPlayerStateOpening \n"];
                        break;
                    }
                case AgoraMediaPlayerStatePaused:
                    {
                        [self.logStr appendString:@"AgoraMediaPlayerStatePaused \n"];
                        break;
                    }
                case AgoraMediaPlayerStatePlayBackCompleted:
                    {
                        [self.logStr appendString:@"AgoraMediaPlayerStatePlayBackCompleted \n"];
                        break;
                    }
        case AgoraMediaPlayerStateFailed:
                    {
                        [self.logStr appendString:@"AgoraMedidPlayerStateFailedStateFailed \n"];
                    }
                case AgoraMediaPlayerStateIdle:
                    {
                        [self.logStr appendString:@"AgoraMediaPlayerStateIdle \n"];
                    }
                default:
                        [self.logStr appendString:@"AgoraMedidPlayerStateFailedUnknow \n"];
                    break;
    }

    switch (error) {
        case AgoraMediaPlayerErrorNone:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorNone \n"];
            break;
        }
        case AgoraMediaPlayerErrorInvalidArguments:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorInvalidArguments \n"];
                    break;
        }
        case AgoraMediaPlayerErrorUnknowStreamType:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorUnknowStreamType \n"];
                   break;
        }
        case AgoraMediaPlayerErrorInvalidState:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorInvalidState \n"];
            break;
        }
        case AgoraMediaPlayerErrorInternal:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorInternal \n"];
            break;
        }
        case AgoraMediaPlayerErrorUrlNotFound:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorUrlNotFound \n"];
            break;
        }
        case AgoraMediaPlayerErrorInvalidConnectState:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorInvalidConnectState \n"];
            break;
        }
        case AgoraMediaPlayerErrorCodecNotSupported:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorCodecNotSupported \n"];
                               break;
        }
        case AgoraMediaPlayerErrorInvalidMediaSource:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorInvalidMediaSource \n"];
                         break;
        }
        case AgoraMediaPlayerErrorVideoRenderFailed:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorVideoRenderFailed \n"];
                         break;
        }
        case AgoraMediaPlayerErrorObjNotInitialized:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorObjNotInitiallized \n"];
                    break;
        }
        case AgoraMediaPlayerErrorSrcBufferUnderflow:
        {
            [self.logStr appendString:@"AgoraMediaPlayerErrorSrcBufferUnderflow \n"];
            break;
        }
        default:
            break;
    }
    __weak typeof(self) weakSelf = self;
    [self executeBlock:^{
    [weakSelf writeLogStr:weakSelf.logStr];
    }];
    
}

/// callback of position
/// @param playerKit AgoraMediaPlayer
/// @param position position
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
                        didChangedToPosition:(NSInteger)position{
 
    __weak typeof(self) weakSelf = self;
    [self executeBlock:^{
        
        self.seekSlider.value = position < 0 ? 0:position;
        [weakSelf.logStr appendString:[NSString stringWithFormat:@"current position == %ld",(long)position]];
    }];
    [self executeBlock:^{
          [weakSelf writeLogStr:weakSelf.logStr];
    }];
    
}
- (IBAction)getStreamByIndex:(id)sender {
    AgoraMediaStreamInfo *info = [_mediaPlayerKit getStreamByIndex:0];
    if (info == NULL) {
        NSLog(@"receive info failed!");
    }else{
        NSLog(@"%ld",(long)info.audioSampleRate);;
    }
}
/// callback of seek state
/// @param playerkit AgoraMediaPlayer
/// @param state Description of seek state
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
                              didOccureEvent:(AgoraMediaPlayerEvent)state{
    __weak typeof(self) weakSelf = self;
    if (playerKit == _mediaPlayerKit) {
        switch (state) {
            case AgoraMediaPlayerEventSeekBegin:
                [self.logStr appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_BEGIN \n"]];

                break;
            case AgoraMediaPlayerEventSeekComplete:
                [self.logStr appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END \n"]];

                break;
            default:
                [self.logStr appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END_ERROR"]];

                break;
        }
        [self executeBlock:^{
              [weakSelf writeLogStr:weakSelf.logStr];
        }];;

    }
}

/// callback of SEI
/// @param playerkit AgoraMediaPlayer
/// @param data SEI's data
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
didReceiveData:(NSString *)data
        length:(NSInteger)length{
    NSLog(@"%@ ===  %ld",data,(long)length);
}

- (void)executeBlock:(void(^)()) block
{
    if (self.isDispatchMainQueue) {
        dispatch_async(dispatch_get_main_queue(), ^{
            block();
        });
    }else{
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
            block();
        });
        
    }
}
- (void)writeLogStr:(NSMutableString *)str
{
    __weak typeof(self) weakSelf = self;
    [self executeBlock:^{
        [weakSelf.logTextView setText:str];
    }];
    
}








@end
