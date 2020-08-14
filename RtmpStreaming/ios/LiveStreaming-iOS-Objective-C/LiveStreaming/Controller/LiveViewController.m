//
//  LiveViewController.m
//  LiveStreaming
//
//  Created by LSQ on 2020/8/13.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "LiveViewController.h"
#import "LiveStreamingWrapper.h"
#import "RtcEngineWrapper.h"
#import "ToastTool.h"

@interface LiveViewController () <RtcEngineEventDelegate, LiveStreamingEventDelegate>

@property (weak, nonatomic) IBOutlet UIView *containerView;
@property (weak, nonatomic) IBOutlet UIButton *startStreamingBtn;

@property (nonatomic, strong) AgoraRtcVideoCanvas *remoteCanvas;
@property (nonatomic, strong) LiveStreamingWrapper *liveStreamingWrapper;
@property (nonatomic, strong) RtcEngineWrapper *rteEngineWrapper;
@property (nonatomic, weak)   UIView *remoteRenderView;
@property (nonatomic, strong) UIView *localRenderView;
@property (nonatomic, assign) BOOL isRtmpPublish;

@end

@implementation LiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupLiveStreaming];
    [self setupRtcEngine];
    [self addObserver];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}

- (void)setupLiveStreaming {
    self.localRenderView = [[UIView alloc] initWithFrame:self.containerView.bounds];
    self.liveStreamingWrapper = [[LiveStreamingWrapper alloc] initWithEventDelegate:self streamingModel:self.streamingModel];
    [self.containerView insertSubview:self.localRenderView atIndex:0];
    [self.liveStreamingWrapper setView:self.localRenderView];
}

- (void)setupRtcEngine {
    NSUInteger sampleRate = 44100;
    switch (self.streamingModel.streamingContext.audioStreamConfiguration.sampleRateHz) {
        case SampleRate11025:
            sampleRate = 11025;
            break;
        case SampleRate22050:
            sampleRate = 22050;
            break;
        case SampleRate44100:
            sampleRate = 44100;
            break;
        default:
            break;
    }
    NSUInteger channels = 1; // mono
    switch (self.streamingModel.streamingContext.audioStreamConfiguration.soundType) {
        case AudioSoundTypeMono:
            channels = 1;
            break;
        case AudioSoundTypeStereo:
            channels = 2;
            break;
        default:
            break;
    }
    self.rteEngineWrapper = [[RtcEngineWrapper alloc] initWithEventDelegate:self sampleRate:sampleRate channelsPerFrame:channels];
}

#pragma mark - IB Actions
- (IBAction)startStreamingBtnDidClicked:(UIButton *)sender {
    if (self.isRtmpPublish) {
        NSLog(@"start rtc.");
        [self.liveStreamingWrapper stopStreaming];
        [self.rteEngineWrapper joinChannelWithChannelId:self.channelName uid:0];
    } else {
        NSLog(@"start rtmp streaming.");
        [self.rteEngineWrapper leaveChannel];
        [self.liveStreamingWrapper startStreaming];
        [self onUserOffline];
    }
}

- (IBAction)closeBtnDidClicked:(UIButton *)sender {
    [self.liveStreamingWrapper stopStreaming];
    [self.rteEngineWrapper leaveChannel];
    [self.liveStreamingWrapper releaseStreaming];
    [self.rteEngineWrapper releaseEngine];
    self.liveStreamingWrapper = nil;
    [self.localRenderView removeFromSuperview];
    self.localRenderView = nil;
    [self.remoteRenderView removeFromSuperview];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)switchCameraBtnDidClicked:(UIButton *)sender {
    [self.liveStreamingWrapper switchCamera];
}

- (IBAction)muteAudioBtnDidClicked:(UIButton *)sender {
    sender.selected = !sender.selected;
    [self.liveStreamingWrapper muteAudioStream:sender.selected];
    [self.rteEngineWrapper muteLocalAudioStream:sender.selected];
}

- (IBAction)muteVideoBtnDidClicked:(UIButton *)sender {
    sender.selected = !sender.selected;
    [self.liveStreamingWrapper muteVideoStream:sender.selected];
    [self.rteEngineWrapper muteLocalVideoStream:sender.selected];
}

#pragma mark - LiveStreamingEventDelegate
- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputVideoFrame:(CVPixelBufferRef)pixelBuffer {
    AgoraVideoFrame *videoFrame = [[AgoraVideoFrame alloc] init];
    videoFrame.format = 12;
    videoFrame.time = CMTimeMakeWithSeconds([NSDate date].timeIntervalSince1970, 1000);
    videoFrame.textureBuf = pixelBuffer;
    videoFrame.rotation = 0;
    [self.rteEngineWrapper pushExternalVideoFrame:videoFrame];
}

- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputAudioFrame:(CMSampleBufferRef)sampleBuffer {
    [self.rteEngineWrapper pushExternalAudioSampleBuffer:sampleBuffer];
}

- (void)onStartStreamingSuccess {
}

- (void)onStartStreamingFailure:(AgoraStartStreamingError)error message:(NSString *)msg {
    [self showMessage:msg];
}

- (void)onMediaStreamingFailure:(AgoraMediaStreamingError)error message:(NSString *)msg {
    [self showMessage:msg];
}

- (void)onStreamingConnectionStateChanged:(AgoraStreamingConnectionState)state {
    dispatch_async(dispatch_get_main_queue(), ^{
        switch (state) {
            case MediaStreamingStateConnected: {
                [self showMessage:@"Rtmp State Connected."];
                [self.startStreamingBtn setTitle:@"Switch to Rtc" forState:UIControlStateNormal];
                self.isRtmpPublish = YES;
            }
                break;
            case MediaStreamingStateDisconnected: {
                [self showMessage:@"Rtmp State Disconnected."];
            }
            case MediaStreamingStateReconnecting: {
                [self showMessage:@"Rtmp State Reconnecting."];
            }
            default:
                break;
        }
    });

}

#pragma mark - RtcEngineEventDelegate
- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didJoinChannel:(NSString*)channel withUid:(NSUInteger)uid elapsed:(NSInteger)elapsed {
    NSLog(@"Join Channel Success");
    [self showMessage:@"Join Channel Success"];
    [self.startStreamingBtn setTitle:@"Switch to Rtmp" forState:UIControlStateNormal];
    self.isRtmpPublish = NO;
}

- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didOccurError:(AgoraErrorCode)errorCode {
    NSLog(@"didOccurError : %ld", (long)errorCode);
    [self showMessage:[NSString stringWithFormat:@"Rtc Did Occur Error :%ld", (long)errorCode]];
}

- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didJoinedOfUid:(NSUInteger)uid elapsed:(NSInteger)elapsed {
    UIView *renderView = [[UIView alloc] initWithFrame:self.view.frame];
    [self.containerView insertSubview:renderView atIndex:0];
    if (self.remoteCanvas == nil) {
        self.remoteCanvas = [[AgoraRtcVideoCanvas alloc] init];
    }
    self.remoteCanvas.uid = uid;
    self.remoteCanvas.view = renderView;
    self.remoteCanvas.renderMode = AgoraVideoRenderModeHidden;
    [self.rteEngineWrapper setupRemoteVideo:self.remoteCanvas];
    self.remoteRenderView = renderView;
    
    [UIView animateWithDuration:0.3 animations:^{
        CGRect newFrame = CGRectMake(self.view.frame.size.width * 0.7 - 10, 20, self.view.frame.size.width * 0.3, self.view.frame.size.width * 0.3 * 16.0 / 9.0);
        self.localRenderView.frame = newFrame;
    }];
}

- (void)rtcEngineKit:(AgoraRtcEngineKit *)engine didOfflineOfUid:(NSUInteger)uid reason:(AgoraUserOfflineReason)reason {
    [self onUserOffline];
}

- (void)onUserOffline {
    [self.remoteCanvas.view removeFromSuperview];
    self.remoteCanvas.view = nil;
    [self.remoteRenderView removeFromSuperview];
    
    [UIView animateWithDuration:0.3 animations:^{
        CGRect newFrame = self.view.frame;
        self.localRenderView.frame = newFrame;
    }];
}

#pragma mark - Uitls
- (void)showMessage:(NSString *)message {
    dispatch_async(dispatch_get_main_queue(), ^{
        [ToastTool showText:message];
    });
}

#pragma mark - Observer
- (void)addObserver {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didChangeRotate:) name:UIApplicationDidChangeStatusBarFrameNotification object:nil];
}

- (void)didChangeRotate:(NSNotification*)notice {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        self.localRenderView.frame = self.containerView.bounds;
    });
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
