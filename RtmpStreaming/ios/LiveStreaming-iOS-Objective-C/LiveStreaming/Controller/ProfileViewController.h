//
//  ProfileViewController.h
//  LiveStreaming
//
//  Created by LSQ on 2020/6/9.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "StreamingModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface ProfileViewController : UITableViewController

@property (nonatomic, strong) StreamingModel *streamingModel;

@end

NS_ASSUME_NONNULL_END
