//
//  SampleHandler.m
//  ScreenShare
//
//  Created by LSQ on 2020/11/20.
//  Copyright © 2020 Agora. All rights reserved.
//


#import "SampleHandler.h"
#import <AgoraStreamingKit/AgoraStreamingKit.h>
#import "AgoraAudioTube.h"

@interface SampleHandler () <AgoraStreamingDelegate>
@property (nonatomic, strong) AgoraStreamingKit *streamingKit;
@end

#define kResampleRateHz 44100

@implementation SampleHandler

- (void)broadcastStartedWithSetupInfo:(NSDictionary<NSString *,NSObject *> *)setupInfo {
    // User has requested to start the broadcast. Setup info from the UI extension can be supplied but optional.
	AgoraStreamingContext *context = [[AgoraStreamingContext alloc] init];
	context.delegate = self;
	self.streamingKit = [AgoraStreamingKit sharedStreamingKitWithContext:context];
	[self.streamingKit setExternalVideoSource:YES];
	[self.streamingKit setExternalAudioSource:YES sampleRate:SampleRate44100 channels:AudioSoundTypeStereo];

	[self.streamingKit startStreaming:@"rtmp://xxxxxxxx"];

//	NSURL *recUrl = [[NSFileManager defaultManager] containerURLForSecurityApplicationGroupIdentifier:@"group.io.agora.LiveStreaming"];
//	NSString *recPath = [recUrl URLByAppendingPathComponent:@"Library/Caches/SDKLogs"].path;
//	[[NSFileManager defaultManager] removeItemAtPath:recPath error:nil];
//	if (![[NSFileManager defaultManager] fileExistsAtPath:recPath]) {
//		[[NSFileManager defaultManager] createDirectoryAtPath:recPath withIntermediateDirectories:YES attributes:nil error:nil];
//	} else {
//	}
//	NSString *pcmPath = [recPath stringByAppendingPathComponent:@"/test.pcm"];
//	NSLog(@"pcm path: %@", pcmPath);
//
//	NSString *logPath = [recPath stringByAppendingPathComponent:@"/testStreamingKit.log"];
//	NSLog(@"log path: %@", logPath);
//	[self.streamingKit setLogFile:logPath];
}

- (void)broadcastPaused {
    // User has requested to pause the broadcast. Samples will stop being delivered.
}

- (void)broadcastResumed {
    // User has requested to resume the broadcast. Samples delivery will resume.
}

- (void)broadcastFinished {
    // User has requested to finish the broadcast.
	[self.streamingKit stopStreaming];
	[self.streamingKit releaseStreamingKit];
	self.streamingKit = nil;
}

- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType {
	switch (sampleBufferType) {
		case RPSampleBufferTypeVideo: {
			// Handle video sample buffer
			CVPixelBufferRef pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
			[self.streamingKit pushExternalVideoFrame:pixelBuffer];
		}
			break;
		case RPSampleBufferTypeAudioApp:
			// Handle audio sample buffer for app audio
			[AgoraAudioTube streamingKit:self.streamingKit pushAudioCMSampleBuffer:sampleBuffer resampleRate:kResampleRateHz type:AudioTypeApp];
			break;
		case RPSampleBufferTypeAudioMic:
			// Handle audio sample buffer for mic audio
			[AgoraAudioTube streamingKit:self.streamingKit pushAudioCMSampleBuffer:sampleBuffer resampleRate:kResampleRateHz type:AudioTypeMic];
			break;
		default:
			break;
	}
}

#pragma mark - AgoraStreamingDelegate
- (void)onStartStreamingSuccess {
	NSLog(@"onStartStreamingSuccess");
}

- (void)onStartStreamingFailure:(AgoraStartStreamingError)error message:(NSString *)msg {
	NSLog(@"StartStreamingFailure: %@", msg);
}

- (void)onStreamingConnectionStateChanged:(AgoraStreamingConnectionState)state {
	NSLog(@"connection state change: %lu", state);
}

@end
