//
//  RtcEngineWrapper.m
//  LiveStreaming
//
//  Created by LSQ on 2020/5/21.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "RtcEngineWrapper.h"
#import "KeyCenter.h"

@interface RtcEngineWrapper () <AgoraRtcEngineDelegate>
@property (nonatomic, strong) AgoraRtcEngineKit *rtcEngine;
@property (nonatomic, weak) id<RtcEngineEventDelegate> delegate;
@property (nonatomic, assign) BOOL isJoin;
@property (nonatomic, assign) NSUInteger channelsPerFrame;
@end

@implementation RtcEngineWrapper

- (instancetype)initWithEventDelegate:(id<RtcEngineEventDelegate>)delegate
                           sampleRate:(NSUInteger)sampleRate
                     channelsPerFrame:(NSUInteger)channelsPerFrame{
    self = [super init];
    if (self) {
        self.delegate = delegate;
        _rtcEngine = [AgoraRtcEngineKit sharedEngineWithAppId:[KeyCenter AppId] delegate:self];
        // Warning: only enable dual stream mode if there will be more than one broadcaster in the channel
        [_rtcEngine enableDualStreamMode:YES];
        [_rtcEngine setChannelProfile:AgoraChannelProfileLiveBroadcasting];
        AgoraVideoEncoderConfiguration *configuration =
            [[AgoraVideoEncoderConfiguration alloc] initWithSize:CGSizeMake(480, 640)
                                                       frameRate:AgoraVideoFrameRateFps15
                                                         bitrate:AgoraVideoBitrateStandard
                                                 orientationMode:AgoraVideoOutputOrientationModeAdaptative];
        [_rtcEngine setVideoEncoderConfiguration:configuration];
        [_rtcEngine setClientRole:AgoraClientRoleBroadcaster];
        [_rtcEngine setParameters:@"{\"che.audio.external.to.apm\":true}"];
        self.channelsPerFrame = channelsPerFrame;
        [_rtcEngine enableExternalAudioSourceWithSampleRate:44100 channelsPerFrame:self.channelsPerFrame];
        [_rtcEngine setExternalVideoSource:YES useTexture:YES pushMode:YES];
        [_rtcEngine enableVideo];
        self.rtcEngine.delegate = self;
    }
    return self;
}

- (void)joinChannelWithChannelId:(NSString *)channelId uid:(NSInteger)uid {
    if (!self.isJoin) {
        [self.rtcEngine setExternalVideoSource:YES useTexture:YES pushMode:YES];
        [self.rtcEngine enableExternalAudioSourceWithSampleRate:44100 channelsPerFrame:self.channelsPerFrame];
        [self.rtcEngine joinChannelByToken:[KeyCenter Token] channelId:channelId info:nil uid:uid joinSuccess:nil];
        self.isJoin = YES;
    } else {
        NSLog(@"It's already in the channel.");
    }
}

- (void)leaveChannel {
    if (self.isJoin) {
        [self.rtcEngine setExternalVideoSource:NO useTexture:NO pushMode:NO];
        [self.rtcEngine disableExternalAudioSource];
        [self.rtcEngine leaveChannel:^(AgoraChannelStats * _Nonnull stat) {
            NSLog(@"rtc leave channel:%ld", (long)stat.userCount);
        }];
        self.isJoin = NO;
    }
}

- (void)releaseEngine {
    if (_rtcEngine) {
        [AgoraRtcEngineKit destroy];
        _rtcEngine = nil;
    }
}

- (void)setupRemoteVideo:(AgoraRtcVideoCanvas*)canvas {
    [self.rtcEngine setupRemoteVideo:canvas];
}

- (void)muteLocalAudioStream:(BOOL)mute {
    [self.rtcEngine muteLocalAudioStream:mute];
}

- (void)muteLocalVideoStream:(BOOL)mute {
    [self.rtcEngine muteLocalVideoStream:mute];
}

- (void)switchCamera {
    [self.rtcEngine switchCamera];
}

- (void)pushExternalVideoFrame:(AgoraVideoFrame *)videoFrame {
    if (self.isJoin) {
        [self.rtcEngine pushExternalVideoFrame:videoFrame];
    }
}

- (void)pushExternalAudioSampleBuffer:(CMSampleBufferRef)sampleBuffer {
    if (self.isJoin) {
        [self.rtcEngine pushExternalAudioFrameSampleBuffer:sampleBuffer];
    }
}

#pragma mark - AgoraRtcEngineDelegate
- (void)rtcEngine:(AgoraRtcEngineKit *)engine didOccurError:(AgoraErrorCode)errorCode {
    NSLog(@"didOccurError");
    if ([self.delegate respondsToSelector:@selector(rtcEngineKit:didOccurError:)]) {
        [self.delegate rtcEngineKit:self.rtcEngine didOccurError:errorCode];
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed {
    if ([self.delegate respondsToSelector:@selector(rtcEngineKit:didJoinedOfUid:elapsed:)]) {
        [self.delegate rtcEngineKit:self.rtcEngine didJoinedOfUid:uid elapsed:elapsed];
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine didJoinChannel:(NSString*)channel withUid:(NSUInteger)uid elapsed:(NSInteger)elapsed {
    if ([self.delegate respondsToSelector:@selector(rtcEngineKit:didJoinChannel:withUid:elapsed:)]) {
        [self.delegate rtcEngineKit:self.rtcEngine didJoinChannel:channel withUid:uid elapsed:elapsed];
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit * _Nonnull)engine didRejoinChannel:(NSString * _Nonnull)channel withUid:(NSUInteger)uid elapsed:(NSInteger) elapsed {
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine didOfflineOfUid:(NSUInteger)uid reason:(AgoraUserOfflineReason)reason {
    if ([self.delegate respondsToSelector:@selector(rtcEngineKit:didOfflineOfUid:reason:)]) {
        [self.delegate rtcEngineKit:self.rtcEngine didOfflineOfUid:uid reason:reason];
    }
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine firstRemoteVideoFrameOfUid:(NSUInteger)uid size:(CGSize)size elapsed:(NSInteger)elapsed {
    NSLog(@"firstRemoteVideoFrameOfUid:%ld", uid);
}

- (void)rtcEngine:(AgoraRtcEngineKit *)engine firstRemoteVideoDecodedOfUid:(NSUInteger)uid size:(CGSize)size elapsed:(NSInteger)elapsed {
    NSLog(@"firstRemoteVideoDecodedOfUid:%ld", uid);
}

@end
