//
//  StreamingModel.h
//  LiveStreaming
//
//  Created by LSQ on 2020/6/10.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraStreamingKit/AgoraStreamingKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface StreamingModel : NSObject <NSCoding>
@property (nonatomic, strong) AgoraStreamingContext *streamingContext;
@property (nonatomic, assign) AgoraLogFilterType logFilterType;
@property (nonatomic, strong) NSString *rtmpUrl;
@end

NS_ASSUME_NONNULL_END
