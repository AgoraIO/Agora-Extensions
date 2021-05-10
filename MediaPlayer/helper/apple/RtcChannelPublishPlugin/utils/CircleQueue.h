//
// Created by zhanxiachao on 2021/5/6.
//

#ifndef LINKLIST_CIRCLEQUEUE_H
#define LINKLIST_CIRCLEQUEUE_H
#include <iostream>
///
/// 动态数组实现循环队列 支持单数据和多数据
///

#define ELEMENT_NOT_FOUND -1
#define DEFAULT_ELEMENT_COUNT 5

template <typename T>
class CircleQueue {
public:
    explicit CircleQueue(int size)
    {
        capacity =  size < DEFAULT_ELEMENT_COUNT ? DEFAULT_ELEMENT_COUNT:size;
        elements =  new T[capacity]();
    }
    ///
    /// \return 数据空间数据大小
    int getSize()
    {
        return size;
    }
    ///
    /// \return 存储容量
    int getCapacity()
    {
        return capacity;
    }
    ///
    /// \return 是否为空
    bool isEmpty()
    {
        return size == 0;
    }
    ///
    /// \param value 从对头插入数据
    void enQueue(T & value)
    {
        ensureCapacity(size + 1);
        elements[index(size)] = value;
        size ++;
    }
    ///
    /// \return 从队尾弹出数据
    T deQueue()
    {
        T frontElemnt = elements[front_];
        elements[front_] = NULL;
        front_ = index(1);
        size --;
        return frontElemnt;
    }
    ///
    /// \param value 需要插入数据的存储空间
    /// \param count 插入数据空间数据的大小
    void Push( T *value,int count)
    {
        ensureMoreCapacity(count);
        memcpy(reinterpret_cast<void *>(&elements[front_ + size]),reinterpret_cast<void *>(&value[0]),count * sizeof(T));
        size += count;
    }
    ///
    /// \param value 需要取出数据的存储空间
    /// \param count 取出数据的大小
    void Pop(T *value,int count)
    {
        if (count > size)
            return;
        int tmpSize = count * sizeof(T);//计算取出数据的实际空间的数据大小
        memcpy(reinterpret_cast<void *>(&value[0]),reinterpret_cast<void *>(&elements[front_]),tmpSize);
        memset(reinterpret_cast<void *>(&elements[front_]),0,tmpSize );//取出数据的存储空间数据清零
        front_ += count;
        size -= count;
    }
    ///
    /// \return 从头获取数据
    T front()
    {
        return elements[front_];
    }
    ///
    /// \return 从尾部获取数据
    T rear()
    {
        return elements[size - 1];
    }
    ~CircleQueue()
    {
        clear();
    }
    void clear()
    {
        if(elements)
            delete[] elements;
        capacity = 0;
        size = 0;
        front_ = 0;
        elements = nullptr;
    }
private:
    int index(int index) //模运算优化
    {
        index += front_;                //15
        return index - (index >= getElementsCount() ? getElementsCount() : 0);
    }

    //插入多个元素check
    void ensureMoreCapacity(int size)
    {
        int oldCapacity = getElementsCount();
        int  availableSize = oldCapacity - front_ - this->size;
        if (  availableSize  >= size)
            return;
        if (front_ >= size) //判断前面空余的空间够不够塞入新的数据
        {
            memmove(reinterpret_cast<void *>(&elements[0]),reinterpret_cast<void *>(&elements[front_]),this->size *
                    sizeof(T)); // 如果空余的数据够存储新数据 则重置数据存储空间将已存储的数据整体右移 以便塞入新的数据
        }else
        {
            int oldCapacity = getElementsCount();
            int newSize = oldCapacity + size + (size >> 1); // 数据扩容确保新存储空间能够容纳下需要添加的数据
            T *newElements = new T[newSize]();
            memset(reinterpret_cast<void *>(&newElements[0]),0,newSize * sizeof(T));//初始化新内存
            memmove(reinterpret_cast<void *>(&newElements[0]),reinterpret_cast<void *>(&elements[front_]),this->size *
                    sizeof(T));//将旧有的内存中的数据整体移动拷贝新内存空间中去
            if (elements)
                delete[] elements; //旧内存删除
            elements = newElements;//成员变量指向新内存的存储空间
            capacity = newSize;//存储容量更新为新的容量

        }
        front_ = 0; //重置头部index位置
    }
    //插入单个元素check
    void ensureCapacity(int capacity)
    {
        int oldCapacity = getElementsCount();
        if(oldCapacity >= capacity) return;
        //新容量为旧容量的1.5 倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        T *newElements = new T[newCapacity]();
        for (int i = 0; i < size; ++i) {
            newElements[i] = elements[index(i)];//这一步将之前队列中的数据 取模赋值到新的队列中去 注意此时新队列 新元素已经从index为0 开始计数了
        }
        if (elements)
            delete[] elements;
        elements = newElements;
        this->capacity = newCapacity;//先赋值完再重置队列空间大小
        front_ = 0;//将对头指向index 为0的元素
    }

private:
    int getElementsCount()
    {
        return capacity;
    }
    T *elements = {0};
    int capacity;
    int size = 0;
    // 首个元素索引
    int front_ = 0;
};


#endif //LINKLIST_CIRCLEQUEUE_H
