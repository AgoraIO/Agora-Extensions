//
//  RtcEngineWrapper.h
//  LiveStreaming
//
//  Created by LSQ on 2020/5/21.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol RtcEngineEventDelegate <NSObject>

- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didJoinChannel:(NSString*)channel withUid:(NSUInteger)uid elapsed:(NSInteger)elapsed;
- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didOccurError:(AgoraErrorCode)errorCode;
- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed;
- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didOfflineOfUid:(NSUInteger)uid reason:(AgoraUserOfflineReason)reason;

@end

@interface RtcEngineWrapper : NSObject

- (instancetype)initWithEventDelegate:(id<RtcEngineEventDelegate>)delegate
                       sampleRate:(NSUInteger)sampleRate
                 channelsPerFrame:(NSUInteger)channelsPerFrame;

- (void)joinChannelWithChannelId:(NSString *)channelId uid:(NSInteger)uid;
- (void)leaveChannel;
- (void)releaseEngine;

- (void)setupRemoteVideo:(AgoraRtcVideoCanvas*)canvas;

- (void)muteLocalAudioStream:(BOOL)mute;
- (void)muteLocalVideoStream:(BOOL)mute;

- (void)switchCamera;

- (void)pushExternalVideoFrame:(AgoraVideoFrame *)videoFrame;
- (void)pushExternalAudioSampleBuffer:(CMSampleBufferRef)sampleBuffer;

@end

NS_ASSUME_NONNULL_END
