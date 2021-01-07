//
//  LiveStreamingWrapper.h
//  LiveStreaming
//
//  Created by LSQ on 2020/5/21.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraStreamingKit/AgoraStreamingKit.h>
#import <UIKit/UIKit.h>
#import "StreamingModel.h"

NS_ASSUME_NONNULL_BEGIN
@protocol LiveStreamingEventDelegate <NSObject>
@optional
- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputVideoFrame:(CVPixelBufferRef)pixelBuffer;
- (void)agoraStreamingKit:(AgoraStreamingKit *)streamingKit didOutputAudioFrame:(CMSampleBufferRef)sampleBuffer;
- (CVPixelBufferRef)process:(CVPixelBufferRef)inputPixelBuffer;
- (void)onStartStreamingSuccess;
- (void)onStartStreamingFailure:(AgoraStartStreamingError)error message:(NSString *)msg;
- (void)onMediaStreamingFailure:(AgoraMediaStreamingError)error message:(NSString *)msg;
- (void)onStreamingConnectionStateChanged:(AgoraStreamingConnectionState)state;
@end

@interface LiveStreamingWrapper : NSObject

- (instancetype)initWithEventDelegate:(id<LiveStreamingEventDelegate>)delegate streamingModel:(StreamingModel *)model;

- (void)setView:(UIView *)view;
- (int)startStreaming;
- (void)stopStreaming;
- (void)releaseStreaming;

- (void)enableVideoCapturing:(BOOL)enabled;
- (void)enableAudioRecording:(BOOL)enabled;
- (int)muteAudioStream:(BOOL)muted;
- (int)muteVideoStream:(BOOL)muted;
- (void)snapshot;
- (void)switchResolution:(CGSize)resolution;
- (void)setCameraFocusPositionInPreview:(CGPoint)position;
- (BOOL)setCameraAutoFocusFaceModeEnabled:(BOOL)enable;
- (float)getMaxZoom;
- (BOOL)setCameraZoomFactor:(CGFloat)zoomFactor;
- (void)addVideoFilter:(id<AgoraVideoFilter>)filter;
- (void)removeVideoFilter:(id<AgoraVideoFilter>)filter;

- (void)switchCamera;

- (void)setLogFilter:(AgoraLogFilterType)filterType;

@end

NS_ASSUME_NONNULL_END
