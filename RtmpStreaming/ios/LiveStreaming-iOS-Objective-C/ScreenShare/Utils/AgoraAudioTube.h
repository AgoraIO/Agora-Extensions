//
//  AgoraAudioTube.h
//  Agora-Screen-Sharing-iOS-Broadcast
//
//  Created by CavanSu on 2019/12/4.
//  Copyright © 2019 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraStreamingKit/AgoraStreamingKit.h>

typedef NS_OPTIONS(NSUInteger, AudioType) {
    AudioTypeApp = 1,
    AudioTypeMic = 2
};

@interface AgoraAudioTube : NSObject
+ (void)streamingKit:(AgoraStreamingKit * _Nonnull)kit pushAudioCMSampleBuffer:(CMSampleBufferRef _Nonnull)sampleBuffer resampleRate:(NSUInteger)resampleRate type:(AudioType)type;
@end
