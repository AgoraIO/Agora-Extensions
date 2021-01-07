//
//  FaceUnityVideoFilter.h
//  LiveStreamingWithFU
//
//  Created by LSQ on 2020/8/18.
//  Copyright © 2020 Agora. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AgoraStreamingKit/AgoraStreamingKit.h>


NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, FUNamaHandleType) {
    FUNamaHandleTypeBeauty = 0,   /* items[0] ------ Place beauty prop handle */
    FUNamaHandleTypeItem = 1,     /* items[1] ------ Place common prop handles (contain many, such as: stickers, Aoimoji... If there is not a single existence, you can put the handle set elsewhere.) */
    FUNamaHandleTypeFxaa = 2,     /* items[2] ------ Fxaa anti-aliasing prop handle */
    FUNamaHandleTypeGesture = 3,    /* items[3] ------ Gesture recognition prop handle */
    FUNamaHandleTypeChangeface = 4, /* items[4] ------ Handle for face-changing prop for posters */
    FUNamaHandleTypeComic = 5,      /* items[5] ------ Animation prop handle */
    FUNamaHandleTypeMakeup = 6,     /* items[6] ------ Beauty makeup prop handle */
    FUNamaHandleTypePhotolive = 7,  /* items[7] ------ A handle to a heterograph prop */
    FUNamaHandleTypeAvtarHead = 8,  /* items[8] ------ Avtar head*/
    FUNamaHandleTypeAvtarHiar = 9,  /* items[9] ------ Avtar hair */
    FUNamaHandleTypeAvtarbg = 10,  /* items[10] ------ Avtar background */
    FUNamaHandleTypeBodySlim = 11,  /* items[11] ------ Beautiful body props */
    FUNamaHandleTypeBodyAvtar = 12,  /* The whole body avtar */
    FUNamaHandleTotal = 13,
};

@interface FaceUnityVideoFilter : NSObject <AgoraVideoFilter>

@property (nonatomic, strong) dispatch_queue_t asyncLoadQueue;
@property (nonatomic, assign) BOOL enabledFilter;

@end

NS_ASSUME_NONNULL_END
