//
//  ToastTool.m
//  LiveStreaming
//
//  Created by LSQ on 2020/7/16.
//  Copyright © 2020 Agora. All rights reserved.
//

#import "ToastTool.h"

@implementation ToastTool

+ (void)showText:(NSString *)text {
    [SVProgressHUD showImage:nil status:text];
    [SVProgressHUD setMaximumDismissTimeInterval:0.5];
    [SVProgressHUD setMinimumDismissTimeInterval:0.3];
    [SVProgressHUD setDefaultStyle:SVProgressHUDStyleDark];
}

@end
