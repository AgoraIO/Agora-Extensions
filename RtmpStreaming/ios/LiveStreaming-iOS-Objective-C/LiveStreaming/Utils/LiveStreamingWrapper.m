//
//  LiveStreamingWrapper.m
//  LiveStreaming
//
//  Created by LSQ on 2020/5/21.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "LiveStreamingWrapper.h"

@interface LiveStreamingWrapper () <AgoraStreamingDelegate, AgoraVideoFilter>
@property (nonatomic, strong) AgoraStreamingKit *streamingKit;
@property (nonatomic, weak) id<LiveStreamingEventDelegate> delegate;
@property (nonatomic, strong) AgoraStreamingContext *context;
@property (nonatomic, assign) CGSize videoSize;
@property (nonatomic, strong) AgoraVideoPreviewRenderer *previewRenderer;
@property (nonatomic, assign) BOOL isStartStreaming;
@property (nonatomic, assign) NSString *rtmpUrl;
@property (nonatomic, strong) StreamingModel *streamingModel;
@end

@implementation LiveStreamingWrapper
- (instancetype)initWithEventDelegate:(id<LiveStreamingEventDelegate>)delegate streamingModel:(StreamingModel *)model{
    self = [super init];
    if (self) {
        self.streamingModel = model;
        self.delegate = delegate;
        self.videoSize = CGSizeMake(640, 480);
        self.context = self.streamingModel.streamingContext;
        self.context.delegate = self;
        self.rtmpUrl = self.streamingModel.rtmpUrl;
        self.streamingKit = [AgoraStreamingKit sharedStreamingKitWithContext:self.context];
        [self.streamingKit enableAudioRecording:YES];
        [self.streamingKit enableVideoCapturing:YES];
        [self.streamingKit setLogFilter:self.streamingModel.logFilterType];
        self.previewRenderer = [self.streamingKit getVideoPreviewRenderer];
        [self.previewRenderer setMirrorMode:MirrorModeAuto];
        [self.previewRenderer setRenderMode:RenderModeHidden];
    }
    return self;
}

- (AgoraStreamingContext *)context {
    if (!_context) {
        _context = [[AgoraStreamingContext alloc] init];
        _context.audioStreamConfiguration = [AgoraAudioStreamConfiguration defaultConfiguration];
        _context.videoStreamConfiguration = [AgoraVideoStreamConfiguration defaultConfiguration];
        _context.videoStreamConfiguration.width = self.videoSize.width;
        _context.videoStreamConfiguration.height = self.videoSize.height;
        _context.appId = @"abcdef";
        _context.delegate = self;
    }
    return _context;
}

- (void)setView:(UIView *)view {
    [self.previewRenderer setView:view];
}

- (int)startStreaming {
    int ret = [self.streamingKit startStreaming:self.rtmpUrl];
    if (ret != 0) {
        NSLog(@"start streaming failed : %d", ret);
    } else {
        self.isStartStreaming = YES;
    }
    return ret;
}

- (void)stopStreaming {
    if (self.isStartStreaming) {
        [self.streamingKit stopStreaming];
        self.isStartStreaming = NO;
    }
}

- (void)releaseStreaming {
    [self.streamingKit releaseStreamingKit];
    self.streamingKit = nil;
}

- (void)enableVideoCapturing:(BOOL)enabled {
    [self.streamingKit enableVideoCapturing:enabled];
}

- (void)enableAudioRecording:(BOOL)enabled {
    [self.streamingKit enableAudioRecording:enabled];
}

- (int)muteAudioStream:(BOOL)muted {
   return [self.streamingKit muteAudioStream:muted];
}

- (int)muteVideoStream:(BOOL)muted {
    return [self.streamingKit muteVideoStream:muted];
}

- (void)addVideoFilter:(id<AgoraVideoFilter>)filter {
    [self.streamingKit addVideoFilter:filter];
}

- (void)removeVideoFilter:(id<AgoraVideoFilter>)filter {
    [self.streamingKit removeVideoFilter:filter];
}

- (void)switchCamera {
    [self.streamingKit switchCamera];
}

- (void)setLogFilter:(AgoraLogFilterType)filterType {
    [self.streamingKit setLogFilter:filterType];
}

#pragma mark - AgoraStreamingDelegate
- (void)onStartStreamingSuccess {
    NSLog(@"onStartStreamingSuccess");
    if ([self.delegate respondsToSelector:@selector(onStartStreamingSuccess)]) {
        [self.delegate onStartStreamingSuccess];
    }
}
- (void)onStartStreamingFailure:(AgoraStartStreamingError)error message:(NSString *)msg {
    NSLog(@"onStartStreamingFailure : %ld message : %@", (long)error, msg);
    if ([self.delegate respondsToSelector:@selector(onStartStreamingFailure:message:)]) {
        [self.delegate onStartStreamingFailure:error message:msg];
    }
}
- (void)onMediaStreamingFailure:(AgoraMediaStreamingError)error message:(NSString *)msg {
    NSLog(@"onMediaStreamingFailure : %ld message : %@", (long)error, msg);
    if ([self.delegate respondsToSelector:@selector(onMediaStreamingFailure:message:)]) {
        [self.delegate onMediaStreamingFailure:error message:msg];
    }
}
- (void)onStreamingConnectionStateChanged:(AgoraStreamingConnectionState)state {
    NSLog(@"onStreamingConnectionStateChanged : %ld", (long)state);
    if ([self.delegate respondsToSelector:@selector(onStreamingConnectionStateChanged:)]) {
        [self.delegate onStreamingConnectionStateChanged:state];
    }
}

- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputVideoFrame:(CVPixelBufferRef)pixelBuffer {
    if ([self.delegate respondsToSelector:@selector(agoraStreamingKit:didOutputVideoFrame:)]) {
        [self.delegate agoraStreamingKit:self.streamingKit didOutputVideoFrame:pixelBuffer];
    }
}

- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputAudioFrame:(CMSampleBufferRef)sampleBuffer {
    if ([self.delegate respondsToSelector:@selector(agoraStreamingKit:didOutputAudioFrame:)]) {
        [self.delegate agoraStreamingKit:self.streamingKit didOutputAudioFrame:sampleBuffer];
    }
}

- (void)onInitializedKitError:(AgoraInitializeError)error {
    
}

#pragma mark - AgoraVideoFilter
- (CVPixelBufferRef)process:(CVPixelBufferRef)inputPixelBuffer {
    if ([self.delegate respondsToSelector:@selector(process:)]) {
        return [self.delegate process:inputPixelBuffer];
    }
    return nil;
}

@end
