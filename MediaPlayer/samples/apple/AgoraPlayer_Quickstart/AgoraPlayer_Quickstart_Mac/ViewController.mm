//
//  ViewController.m
//  player_demo_apple
//
//  Created by zhanxiaochao on 2019/11/29.
//  Copyright © 2019 agora. All rights reserved.
//

#import "ViewController.h"
#include <AgoraMediaPlayer/AgoraMediaPlayerKit.h>
#include <AgoraRtcEngineKit/AgoraRtcEngineKit.h>
#import "AgoraRtcChannelPublishHelper.h"
@interface ViewController()<AgoraMediaPlayerDelegate,AgoraRtcChannelPublishHelperDelegate,AgoraRtcEngineDelegate>
{
    int64_t duration01;
    int64_t duration02;
}
@property (weak) IBOutlet NSView *videoViewOne;
@property (weak) IBOutlet NSView *videoViewSecond;
@property (weak) IBOutlet NSTextField *video1_duration;
@property (weak) IBOutlet NSTextField *video2_duration;
@property (unsafe_unretained) IBOutlet NSTextView *textView01;
@property (unsafe_unretained) IBOutlet NSTextView *textView02;
@property (weak) IBOutlet NSSlider *seekSlider01;
@property (weak) IBOutlet NSSlider *seekSlider02;

@property (weak) IBOutlet NSSlider *volumeSlider01;
@property (weak) IBOutlet NSSlider *volumeSlider02;


@property (nonatomic, strong)AgoraMediaPlayer *mediaPlayerKitOC;
@property (nonatomic, strong)AgoraMediaPlayer *mediaPlayerKitOC_;

@property (nonatomic, strong)NSMutableString *logStr01;
@property (nonatomic, strong)NSMutableString *logStr02;

@property (nonatomic, strong)AgoraRtcEngineKit *rtcEnginekit;


@end
@implementation ViewController
-(NSMutableString *)logStr01{
    if (_logStr01 == NULL) {
        _logStr01 = [NSMutableString string];
    }
    return _logStr01;
}
-(NSMutableString *)logStr02
{
    if (_logStr02 == NULL) {
        _logStr02 = [NSMutableString string];
    }
    return _logStr02;
}


- (IBAction)openFile:(NSButton *)sender {
    
    switch (sender.tag) {
        case 0:
            _seekSlider01.intValue = 0;
            [self openMediaFile:_mediaPlayerKitOC];
            break;
        case 1:
            _seekSlider02.intValue = 0;
            [self openMediaFile:_mediaPlayerKitOC_];
            break;
        default:
            break;
    }
}
- (IBAction)getDurationName:(NSButton *)sender {
    
    switch (sender.tag) {
        case 0:
        {
            
            duration01 = [_mediaPlayerKitOC getDuration];
            [self.logStr01 appendString:[NSString stringWithFormat:@"duration == %lld \n",duration01]];
            [self writeLog:_textView01 string:self.logStr01];
            long hour = duration01 / 3600;
            long min = duration01 / 60;
            long ss = duration01 % 60;
            _video1_duration.stringValue = [NSString stringWithFormat:@"%ld:%ld:%ld",hour,min,ss];
            _seekSlider01.maxValue = (int)duration01;
            break;
        }
        case 1:
        {
            duration02 =  [_mediaPlayerKitOC_ getDuration];
            [self.logStr02 appendString:[NSString stringWithFormat:@"duration == %lld \n",duration02]];
            [self writeLog:_textView02 string:self.logStr02];
            long hour = duration02 / 3600;
            long min = duration02 / 60;
            long ss = duration02 % 60;
            _video2_duration.stringValue = [NSString stringWithFormat:@"%ld:%ld:%ld",hour,min,ss];
            _seekSlider02.maxValue = (int)duration02;
            break;
        }
        default:
            break;
    }
    
}
- (IBAction)getStreamCount:(NSButton *)sender {
    switch (sender.tag) {
        case 0:
        {
            NSInteger stream_count = [_mediaPlayerKitOC getStreamCount];
            [self.logStr01 appendString:[NSString stringWithFormat:@"stream_count == %ld \n",(long)stream_count]];
            [self writeLog:_textView01 string:self.logStr01];
            break;
        }
        case 1:{
            NSInteger stream_count = [_mediaPlayerKitOC getStreamCount];
            [NSString stringWithFormat:@"stream_count == %ld \n",(long)stream_count];
            [self.logStr02 appendString:[NSString stringWithFormat:@"stream_count == %ld \n",(long)stream_count]];
             [self writeLog:_textView02 string:self.logStr02];
            break;
        }
        default:
            break;
    }
    
    
}

- (IBAction)play_or_pause:(NSButton *)sender {
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC play];
            break;
        case 1:
            [_mediaPlayerKitOC_ play];
            break;
            
        default:
            break;
    }
}
- (IBAction)volume_change:(NSSlider *)sender {
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC adjustVolume:sender.intValue];
            [[AgoraRtcChannelPublishHelper shareInstance]adjustPublishSignalVolume:sender.intValue];
            
            break;
        case 1:
            [_mediaPlayerKitOC_ adjustVolume:sender.intValue];
            break;
            
        default:
            break;
    }
}

- (IBAction)play_pos_change:(NSSlider *)sender {
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC seekToPosition:sender.intValue];
            break;
        case 1:
            [_mediaPlayerKitOC_ seekToPosition:sender.intValue];
            break;
            
        default:
            break;
    }
}

- (IBAction)stop:(NSButton *)sender {
    
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC stop];
            break;
        case 1:
            [_mediaPlayerKitOC_ stop];
            break;
            
        default:
            break;
    }
}
- (IBAction)pause:(NSButton *)sender {
    
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC pause];
            break;
        case 1:
            [_mediaPlayerKitOC_ pause];
            break;
            
        default:
            break;
    }
    
    
    
}
- (IBAction)mute:(NSButton *)sender {
    switch (sender.tag) {
        case 0:
        {
            
            bool mute = _mediaPlayerKitOC.mute;
            [_mediaPlayerKitOC mute:!mute];
            
            break;
        }
        case 1:
        {
            bool mute = _mediaPlayerKitOC_.mute;
            [_mediaPlayerKitOC_ mute:!mute];
            break;
        }
        default:
            break;
    }
    
    
}
- (IBAction)publishVideo:(id)sender {
    
    [[AgoraRtcChannelPublishHelper shareInstance] publishVideo];

}
- (IBAction)unpublishVideo:(id)sender {
    
    [[AgoraRtcChannelPublishHelper shareInstance] unpublishVideo];
    
}
- (IBAction)publishAudio:(id)sender {
    
    [[AgoraRtcChannelPublishHelper shareInstance] publishAudio];
    
}
- (IBAction)unPublishAudio:(id)sender {
    
    [[AgoraRtcChannelPublishHelper shareInstance] unpublishAudio];
    
}


- (void)viewDidLoad {
    
    [self initUI];
    [self initAgoraMediaPlayer];
    [self initAgoraSdk];
    
    // Do any additional setup after loading the view.
}
- (void)initAgoraSdk{
#error input your Appid
    _rtcEnginekit = [AgoraRtcEngineKit sharedEngineWithAppId:<#YOUR_APPID#> delegate:self];
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
    [[AgoraRtcChannelPublishHelper shareInstance] attachPlayerToRtc:_mediaPlayerKitOC RtcEngine:_rtcEnginekit enableVideoSource:true];
    [[AgoraRtcChannelPublishHelper shareInstance] registerRtcChannelPublishHelperDelegate:self];
    [_rtcEnginekit joinChannelByToken:@"" channelId:@"agora" info:@"" uid:0 joinSuccess:NULL];
}
-(void)initUI{
    self.textView01.textColor = NSColor.whiteColor;
    self.textView02.textColor = NSColor.whiteColor;
}
-(void)initAgoraMediaPlayer{
    NSView *view_one = [[NSView alloc]init];
    view_one.frame = self.videoViewOne.bounds;
    view_one.wantsLayer = YES;
    view_one.layer.backgroundColor = [NSColor whiteColor].CGColor;
    [self.videoViewOne addSubview:view_one positioned:NSWindowBelow relativeTo:self.videoViewOne];
    
    NSView *view_sencond = [[NSView alloc]init];
    view_sencond.frame = self.videoViewSecond.bounds;
    view_sencond.wantsLayer = YES;
    view_sencond.layer.backgroundColor = [NSColor whiteColor].CGColor;
    [self.videoViewSecond addSubview:view_sencond positioned:NSWindowBelow relativeTo:self.videoViewSecond];
    _mediaPlayerKitOC = [[AgoraMediaPlayer alloc] initWithDelegate:self];
    _mediaPlayerKitOC_ = [[AgoraMediaPlayer alloc] initWithDelegate:self];
    [_mediaPlayerKitOC setView:view_one];
    [_mediaPlayerKitOC_ setView:view_sencond];

}


- (void)setRepresentedObject:(id)representedObject {
    [super setRepresentedObject:representedObject];
    
    // Update the view, if already loaded.
}
-(void )openMediaFile:(AgoraMediaPlayer *)kit{
    NSOpenPanel *panel = [NSOpenPanel openPanel];
    [panel setAllowsMultipleSelection:false];
    [panel beginWithCompletionHandler:^(NSModalResponse result) {
        if (result == NSModalResponseOK) {
            for (NSURL *element in [panel URLs]) {
                [kit open:[element path] startPos:0];
            }
        }
    }];
}
-(void)writeLog:(NSTextView *)textView string:(NSMutableString *)logStr{
    dispatch_async(dispatch_get_main_queue(), ^{
         [[[textView textStorage] mutableString] setString:logStr];
    });
}
#pragma mark mediaplayerkit delegate methods
- (void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit didChangedToState:(AgoraMediaPlayerState)state error:(AgoraMediaPlayerError)error{
    if (playerKit == _mediaPlayerKitOC) {
        switch (state) {
            case AgoraMediaPlayerStateOpenCompleted:{
                    [_logStr01 appendString:@"MEDIA_PLAYER_STATE_OPEN_COMPLETE \n"];
                int volume = 0;
                __weak typeof(self) weakSelf = self;
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.volumeSlider01.intValue  = (int)weakSelf.mediaPlayerKitOC.volume;
                });
                break;
            }
            case AgoraMediaPlayerStatePlaying:
            {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_PLAYING \n"];
                
                break;
            }
            case AgoraMediaPlayerStateStopped:
            {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_STOPPED \n"];
                           
                break;
            }
            case AgoraMediaPlayerStateOpening:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_OPENING \n"];
                    break;
                }
            case AgoraMediaPlayerStatePaused:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_PAUSED \n"];
                    break;
                }
            case AgoraMediaPlayerStatePlayBackCompleted:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                    break;
                }
            default:
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                break;
            }
        
        [self writeLog:self.textView01 string:self.logStr01];
    }else{
        switch (state) {
            case AgoraMediaPlayerStateOpenCompleted:
               {
                    [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_OPEN_COMPLETE \n"];
//                    int volume;
                   __weak typeof(self) weakSelf = self;
                    dispatch_async(dispatch_get_main_queue(), ^{
                    self.volumeSlider02.intValue = (int)weakSelf.mediaPlayerKitOC_.volume;
                    });
                    break;
                }
            case AgoraMediaPlayerStatePlaying:
                {
                    [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_PLAYING \n"];
                    break;
                }
            case AgoraMediaPlayerStateStopped:
                {
                    [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_STOPPED \n"];
                    break;
            }
            case AgoraMediaPlayerStateOpening:
                {
                    [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_OPENING \n"];
                    break;
                }
            case AgoraMediaPlayerStatePaused:
                {
                    [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_PAUSED \n"];
                    break;
                }
            case AgoraMediaPlayerStatePlayBackCompleted:
                {
                        [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                        break;
                }
            default:
                [self.logStr02 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                break;
        }
        [self writeLog:self.textView02 string:self.logStr02];
    }
}
-(void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit didChangedToPosition:(NSInteger)position
{
    if (playerKit == _mediaPlayerKitOC) {
        [_logStr01 appendString:[NSString stringWithFormat:@"current position == %ld \n",(long)position]];
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.seekSlider01.intValue = (int)position;
        });
        [self writeLog:self.textView01 string:self.logStr01];
    }else{
        [_logStr02 appendString:[NSString stringWithFormat:@"current position == %ld \n",(long)position]];
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.seekSlider02.intValue = (int)position;
        });
        [self writeLog:self.textView02 string:self.logStr02];
    }
}
-(void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit didOccureEvent:(AgoraMediaPlayerEvent)state
{
    if (playerKit == _mediaPlayerKitOC) {
        switch (state) {
            case AgoraMediaPlayerEventSeekBegin:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_BEGIN \n"]];

                break;
            case AgoraMediaPlayerEventSeekComplete:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END \n"]];

                break;
            default:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END_ERROR"]];

                break;
        }
        [self writeLog:self.textView01 string:self.logStr01];

    }else{
        switch (state) {
            case AgoraMediaPlayerEventSeekBegin:
                [_logStr02 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_BEGIN \n"]];

                break;
            case AgoraMediaPlayerEventSeekComplete:
                [_logStr02 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END \n"]];

                break;
            default:
                [_logStr02 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END_ERROR"]];

                break;
        }
        [self writeLog:self.textView02 string:self.logStr02];
    }
}
-(void)AgoraMediaPlayer:(AgoraMediaPlayer *)playerKit didReceiveData:(void *)data length:(NSInteger)length
{
    if (playerKit == _mediaPlayerKitOC) {
        //todo
        
    }else{
        //todo
    
    }
}

- (IBAction)setRenderMode:(NSButton *)sender {
    switch (sender.tag) {
        case 0:
            [_mediaPlayerKitOC setRenderMode:AgoraMediaPlayerRenderModeFit];
            break;
        case 1:
            [_mediaPlayerKitOC_ setRenderMode:AgoraMediaPlayerRenderModeFit];
            break;
        default:
            break;
    }
    
    
    
    
}

/// Description of state of Mediaplayer's state
/// @param playerKit AgoraMediaPlayer
/// @param state AgoraMediaPlayerState
/// @param reason AgoraMediaPlayerStateReason
/// @param error AgoraMediaPlayerError
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
       didChangedToState:(AgoraMediaPlayerState)state
                                       error:(AgoraMediaPlayerError)error{
    
    if (playerKit == _mediaPlayerKitOC) {
        switch (state) {
            case AgoraMediaPlayerStateOpenCompleted:{
                    [_logStr01 appendString:@"MEDIA_PLAYER_STATE_OPEN_COMPLETE \n"];
                int volume = 0;
                __weak typeof(self) weakSelf = self;
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.volumeSlider01.intValue  = (int)weakSelf.mediaPlayerKitOC.volume;
                });
                break;
            }
            case AgoraMediaPlayerStatePlaying:
            {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_PLAYING \n"];
                
                break;
            }
            case AgoraMediaPlayerStateStopped:
            {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_STOPPED \n"];
                           
                break;
            }
            case AgoraMediaPlayerStateOpening:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_OPENING \n"];
                    break;
                }
            case AgoraMediaPlayerStatePaused:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_PAUSED \n"];
                    break;
                }
            case AgoraMediaPlayerStatePlayBackCompleted:
                {
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                    break;
                }
            default:
                    [self.logStr01 appendString:@"MEDIA_PLAYER_STATE_UNKNOW \n"];
                break;
            }
        
        [self writeLog:self.textView01 string:self.logStr01];
    }
    
    
    
    
}

/// callback of position
/// @param playerKit AgoraMediaPlayer
/// @param position position
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
                        didChangedToPosition:(NSInteger)position{
 
    if (playerKit == _mediaPlayerKitOC) {
        [_logStr01 appendString:[NSString stringWithFormat:@"current position == %ld \n",(long)position]];
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.seekSlider01.intValue = (int)position;
        });
        [self writeLog:self.textView01 string:self.logStr01];
    }
    
}

/// callback of seek state
/// @param playerkit AgoraMediaPlayer
/// @param state Description of seek state
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *_Nonnull)playerKit
                              didOccureEvent:(AgoraMediaPlayerEvent)state{
    
    if (playerKit == _mediaPlayerKitOC) {
        switch (state) {
            case AgoraMediaPlayerEventSeekBegin:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_BEGIN \n"]];

                break;
            case AgoraMediaPlayerEventSeekComplete:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END \n"]];

                break;
            default:
                [_logStr01 appendString:[NSString stringWithFormat:@"MEDIA_PLAYER_SEEK_STATE_END_ERROR"]];

                break;
        }
        [self writeLog:self.textView01 string:self.logStr01];

    }
    
    
}

/// callback of SEI
/// @param playerkit AgoraMediaPlayer
/// @param data SEI's data
- (void)AgoraRtcChannelPublishHelperDelegate:(AgoraMediaPlayer *)playerKit didReceiveData:(NSString *)data length:(NSInteger)length{
    NSLog(@"%@ #### size = %ld",data,length);
}

@end



