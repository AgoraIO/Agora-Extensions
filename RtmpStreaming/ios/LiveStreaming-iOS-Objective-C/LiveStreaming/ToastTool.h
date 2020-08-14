//
//  ToastTool.h
//  LiveStreaming
//
//  Created by LSQ on 2020/7/16.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SVProgressHUD.h>

NS_ASSUME_NONNULL_BEGIN

@interface ToastTool : NSObject
+ (void)showText:(NSString *)text;
@end

NS_ASSUME_NONNULL_END
