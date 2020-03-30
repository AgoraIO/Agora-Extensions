//
//  UITextViewWorkaround.m
//  player_demo_iOS
//
//  Created by zhanxiaochao on 2019/12/13.
//  Copyright © 2019 agora. All rights reserved.
//

#import "UITextViewWorkaround.h"
#include <UIKit/UIKit.h>
#import  <objc/runtime.h>

@implementation UITextViewWorkaround
+ (void)executeWorkaround{
     if (@available(iOS 13.2, *)) {
        }
        else {
            const char *className = "_UITextLayoutView";
            Class cls = objc_getClass(className);
            if (cls == nil) {
                cls = objc_allocateClassPair([UIView class], className, 0);
                objc_registerClassPair(cls);
    #if DEBUG
                printf("added %s dynamically\n", className);
    #endif
            }
        }
}

@end
