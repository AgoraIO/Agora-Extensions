//
//  AudioCircleBuffer.hpp
//  AgoraPlayer_Quickstart
//
//  Created by zhanxiaochao on 2021/5/10.
//  Copyright © 2021 agora. All rights reserved.
//

#ifndef AudioCircleBuffer_hpp
#define AudioCircleBuffer_hpp

#include "CircleQueue.h"
#include <mutex>

template <typename T>
///  Thread safe
class AudioCircularBuffer
{
public:
    AudioCircularBuffer(int size):audioCacheBuffer(size)
    {
        
    }
    void Push(T *value,int length)
    {
        std::lock_guard<std::mutex> _(mtx_);
        audioCacheBuffer.Push(value, length);
    }
    void Pop(T *value,int length)
    {
        std::lock_guard<std::mutex> _(mtx_);
        audioCacheBuffer.Pop(value, length);
    }
    void Reset()
    {
        std::lock_guard<std::mutex> _(mtx_);
        audioCacheBuffer.clear();
    }
    void isEmpty()
    {
        std::lock_guard<std::mutex> _(mtx_);
        return audioCacheBuffer.isEmpty();
    }
    int getSize()
    {
        std::lock_guard<std::mutex> _(mtx_);
        return audioCacheBuffer.getSize();
    }
    
private:
    CircleQueue<T> audioCacheBuffer;
    std::mutex mtx_;
};



#endif /* AudioCircleBuffer_hpp */
