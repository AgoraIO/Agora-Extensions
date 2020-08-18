//
//  StreamingModel.m
//  LiveStreaming
//
//  Created by LSQ on 2020/6/10.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "StreamingModel.h"

@implementation StreamingModel
- (instancetype)init {
    self = [super init];
    if (self) {
        self.streamingContext = [[AgoraStreamingContext alloc] init];
        self.logFilterType = LogFilterError;
    }
    return self;
}
- (instancetype)initWithCoder:(NSCoder *)coder {
    self = [super init];
    if (self) {
        [coder decodeObjectForKey:@"streamingContext"];
        [coder decodeObjectForKey:@"logFilterType"];
        [coder decodeObjectForKey:@"rtmpUrl"];
    }
    return self;
}
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:self.streamingContext forKey:@"streamingContext"];
    [coder encodeObject:@(self.logFilterType) forKey:@"logFilterType"];
    [coder encodeObject:self.rtmpUrl forKey:@"rtmpUrl"];
}
@end
