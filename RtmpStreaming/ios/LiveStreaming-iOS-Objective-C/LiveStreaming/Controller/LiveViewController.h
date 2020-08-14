//
//  LiveViewController.h
//  LiveStreaming
//
//  Created by LSQ on 2020/8/13.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "StreamingModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface LiveViewController : UIViewController
@property (nonatomic, copy) NSString *channelName;
@property (nonatomic, strong) StreamingModel *streamingModel;
@end

NS_ASSUME_NONNULL_END
